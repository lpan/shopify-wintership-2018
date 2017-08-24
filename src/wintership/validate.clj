(ns wintership.validate)

;; helpers
(defn gen-preds
  "Generate predicates for to a particular type"
  [{:keys [required type length]}]
  (let [min-length (:min length)
        max-length (:max length)]
    (cond-> '()
      (true? required) (conj some?)
      (= type "string") (conj string?)
      (= type "number") (conj integer?)
      (= type "boolean") (conj (some-fn true? false?))
      (integer? min-length) (conj #(>= (count %) min-length))
      (integer? max-length) (conj #(<= (count %) max-length)))))

(defn parse-schema
  "parses schema and returns a map to lookup validators"
  [schema]
  (->> schema
       (map #(let [k (first (keys %))] (update % k gen-preds)))
       (into (sorted-map))))

(defn gen-validator
  "schema -> customer -> invalid-fields"
  [schema]
  (let [validators (parse-schema schema)]
    (fn [customer]
      (->> customer
           seq
           ; select only the invalid fields
           (filter (fn [[t value]]
                     (if-let [preds (get validators t)]
                       (let [is-valid? (apply every-pred preds)]
                         (not (is-valid? value)))
                       false)))
           ; convert keyword to string
           (map #(-> % first name str))))))
