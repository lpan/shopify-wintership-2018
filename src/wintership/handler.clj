(ns wintership.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.data.json :as json]
            [clojure.core.async :refer [<!!]]
            [wintership.customers :refer [customers-fetch]]
            [wintership.validate :refer [gen-get-invalid-fields]]))

(defn ^:private format-invalid-customer
  [get-invs customer]
  (let [invalid-fields (get-invs customer)
        id (:id customer)]
    (if-not (empty? invalid-fields)
      {:id id :invalid_fields invalid-fields}
      nil)))

(defn ^:private get-invalid-customers-sync []
  (let [{:keys [<customers <customer-count <validations]} (customers-fetch)
        validations (<!! <validations)
        get-invalid-fields (gen-get-invalid-fields validations)
        customer-count (<!! <customer-count)]
    (loop [customer-remaning customer-count
           invalid-customers []]
      (if (= customer-remaning 0)
        invalid-customers
        (let [customer (<!! <customers)]
          (if-let [invalid-customer (format-invalid-customer get-invalid-fields customer)]
            (recur (dec customer-remaning) (conj invalid-customers invalid-customer))
            (recur (dec customer-remaning) invalid-customers)))))))

(defroutes app-routes
  (GET "/" [] (->> (get-invalid-customers-sync)
                   (sort-by :id)
                   json/write-str))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
