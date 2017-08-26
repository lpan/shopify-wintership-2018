(ns backend-challenge.validate)

;; helpers
(defn nullable [pred] (some-fn nil? pred))

(defn gen-preds
  "Generate predicates for a particular type"
  [{:keys [required type length]}]
  (let [min-length (:min length)
        max-length (:max length)]
    (cond-> '()
      (true? required) (conj some?)
      (= type "string") (conj (nullable string?))
      (= type "number") (conj (nullable integer?))
      (= type "boolean") (conj (nullable (some-fn true? false?)))
      (integer? min-length) (conj (nullable #(>= (count %) min-length)))
      (integer? max-length) (conj (nullable #(<= (count %) max-length))))))

(defn parse-validations
  "parses validations object to return a validator lookup map"
  [validations]
  (->> validations
       (map (fn [validation]
              (let [type-name (-> validation keys first)
                    criteria (get validation type-name)
                    is-valid? (->> criteria gen-preds (apply every-pred))]
                (assoc validation type-name is-valid?))))
       (into (sorted-map))))

(defn gen-get-invalid-fields
  "validations -> customer -> invalid-fields"
  [validations]
  (let [validators (parse-validations validations)]
    (fn [customer]
      (->> customer
           seq
           ; select only the invalid fields
           (filter (fn [[type-name value]]
                     (if-let [is-valid? (get validators type-name)]
                       (not (is-valid? value))
                       false)))
           ; convert keyword to string
           (map #(-> % first name str))))))
