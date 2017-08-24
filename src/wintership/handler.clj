(ns wintership.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.data.json :as json]
            [clojure.core.async :refer [<!! go chan]]
            [wintership.customers :refer [customers-chan]]
            [wintership.validate :refer [gen-get-invalid-fields]]))

(defn format-invalid-customer [get-invs customer]
  (let [invalid-fields (get-invs customer)
        id (:id customer)]
    (if-not (empty? invalid-fields)
      {:id id :invalid_fields invalid-fields}
      nil)))

(defn get-invalid-customers []
  (let [c (customers-chan)]
    (loop [get-invs nil
           invalid-customers []]
      (let [raw-customers (<!! c)
            {:keys [validations customers]} raw-customers]
        (cond
          (nil? get-invs) (let [get-invs (gen-get-invalid-fields validations)]
                            (recur get-invs (->> customers
                                                 (keep #(format-invalid-customer get-invs %))
                                                 (into invalid-customers))))
          (empty? customers) invalid-customers
          true (recur get-invs (->> customers
                                    (keep #(format-invalid-customer get-invs %))
                                    (into invalid-customers))))))))

(defroutes app-routes
  (GET "/" [] (json/write-str (get-invalid-customers)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
