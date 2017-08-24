(ns wintership.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.data.json :as json]
            [clojure.core.async :refer [<!! go chan]]
            [wintership.customers :refer [customers-chan]]
            [wintership.validate :refer [gen-validator]]))

(defn format-invalid-customer [v customer]
  (let [invalid-fields (v customer)
        id (:id customer)]
    (if-not (empty? invalid-fields)
      {:id id :invalid_fields invalid-fields}
      nil)))

(defn get-invalid-customers []
  (let [c (customers-chan)]
    (loop [validator nil
           invalid-customers []]
      (let [raw-customers (<!! c)
            schema (:validations raw-customers)
            customers (:customers raw-customers)]
        (cond
          (nil? validator) (let [v (gen-validator schema)]
                             (recur v (->> customers
                                           (keep #(format-invalid-customer v %))
                                           (into invalid-customers))))
          (empty? customers) invalid-customers
          true (recur validator (->> customers
                                     (keep #(format-invalid-customer validator %))
                                     (into invalid-customers))))))))

(defroutes app-routes
  (GET "/" [] (json/write-str (get-invalid-customers)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
