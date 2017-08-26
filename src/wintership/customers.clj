(ns wintership.customers
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.core.async :refer [put! chan]]))

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
              on-success
              on-failure))

(defn put-page! [channel page-number]
  (fetch-page {:page-number page-number
               :on-success #(put! channel (parse-page %))
               :on-failure #(put! channel nil)}))

(defn ^:private get-page-count
  "parse pagination object to get the count of total pages"
  [{:keys [per_page total]}]
  (int (Math/ceil (/ total per_page))))

(defn ^:private fetch-rest-pages
  [channel {:keys [pagination] :as first-page}]
  (let [page-count (get-page-count pagination)]
    (put! channel first-page)
    (doseq [page-number (range 2 (inc page-count))]
      (put-page! channel page-number))))

(defn <customers []
  "fetch first page to get the pagination info then fetch rest in parallel"
  (let [out (chan)]
    (fetch-page {:page-number 1
                 :on-success #(fetch-all out (parse-page %))
                 :on-failure identity})
    out))
