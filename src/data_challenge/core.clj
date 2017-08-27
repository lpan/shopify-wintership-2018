(ns data-challenge.core
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.data.json :as json]))

(def ^:private read-file
  #(-> % slurp (json/read-str :key-fn ->kebab-case-keyword)))

(defn ^:private to-map-by-key
  "O(n) convert a vector to a map"
  [lst k]
  (reduce #(assoc %1 (get %2 k) %2) {} lst))

; order inner join customer on order.order-key = customer.customer-key
(defn order-inner-join-customer [order-key customer-key]
  (let [orders (read-file "resources/data/orders.json")
        customers (read-file "resources/data/customers.json")
        customers-map (to-map-by-key customers customer-key)]
    (keep
      (fn [order]
        (let [foreign (get order order-key)]
          (if-let [customer (get customers-map foreign)]
            (merge customer order)
            nil)))
      orders)))

(defn calculate-total
  "calculate the total for orders placed by a given person"
  [name orders]
  (reduce #(if (= name (:name %2)) (+ %1 (:price %2)) %1) 0 orders))

(defn -main []
  (let [result (order-inner-join-customer :customer-id :cid)]
    (println (str "Total placed by Steve: " (calculate-total "Steve" result)))
    (println (str "Total placed by Barry: " (calculate-total "Barry" result)))
    (println (str "Inner join result length: " (count result)))))
