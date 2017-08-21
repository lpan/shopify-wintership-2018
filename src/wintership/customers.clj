(ns wintership.customers
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.core.async :refer [>! <! <!! go chan]]))

(def url "https://backend-challenge-winter-2017.herokuapp.com/customers.json")

(defn customers []
  (let [out (chan)]
    (go (doseq [page (range)]
          (>! out (-> url
                      (client/get {:query-params {"page" (inc page)}})
                      :body
                      (json/read-str :key-fn keyword)))))
    out))
