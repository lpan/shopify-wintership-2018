(ns wintership.customers
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.core.async :refer [put! chan close!]]))

(def url "https://backend-challenge-winter-2017.herokuapp.com/customers.json")

(defn ^:private parse-page [raw-page]
  (-> raw-page
      :body
      (json/read-str :key-fn keyword)))

(defn ^:private fetch-page
  [{:keys [page-number on-success on-failure]}]
  (client/get url
              {:async? true
               :query-params {"page" page-number}}
              #(on-success (parse-page %))
              on-failure))

(defn ^:private get-page-count
  "parse pagination object to get the count of total pages"
  [{:keys [per_page total]}]
  (int (Math/ceil (/ total per_page))))

(defn ^:private put-all!
  [channel items]
  (doseq [i items]
    (put! channel i)))

(defn ^:private put-rest-customers!
  [channel pages]
  (doseq [page-number pages]
    (fetch-page {:page-number page-number
                 :on-success #(put-all! channel (:customers %))
                 :on-failure identity})))

; 1. fetch first page to get pagination info
; 2. fetch the rest in parallel
(defn customers-fetch []
  (let [<customers (chan)
        <validations (chan)
        <customer-count (chan)]
    (fetch-page {:page-number 1
                 :on-success (fn [page]
                               (let [{:keys [customers pagination validations]} page
                                     page-count (get-page-count pagination)
                                     page-range (range 2 (inc page-count))]
                                 (put! <validations validations)
                                 (close! <validations)

                                 (put! <customer-count (:total pagination))
                                 (close! <customer-count)

                                 (put-all! <customers customers)
                                 (put-rest-customers! <customers page-range)))
                 :on-failure #(println %)})
    {:<customers <customers
     :<customer-count <customer-count
     :<validations <validations}))
