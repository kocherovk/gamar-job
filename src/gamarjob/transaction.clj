(ns gamarjob.transaction)

(defprotocol Transaction
  (raw [_])
  (source [_])
  (desc [_])
  (category [_])
  (to-myself? [_])
  (conversion? [_])
  (salary? [_]))

(defrecord Other [id account currency amount date]
  Transaction
  (raw [_] {})
  (source [_] :other)
  (category [_] "OTHER")
  (desc [_] "other")
  (to-myself? [_] false)
  (conversion? [_] false)
  (salary? [_] false))

(defn other-transaction [id account currency amount date]
  (->Other id account currency (bigdec amount) date))

(defn other? [tr] (= (source tr) :other))

(defn transaction-is [field value tr] (= (get tr field) value))
(def is-currency (partial transaction-is :currency))
(def gel? (partial is-currency "GEL"))
(def usd? (partial is-currency "USD"))
(def eth? (partial is-currency "ETH"))
(defn before? [tr time] (<= (compare (:date tr) time) 0))
(defn after? [tr time] (>= (compare (:date tr) time) 0))
(defn between? [tr start end] (and (after? tr start) (before? tr end)))
(defn credit? [tr] (neg? (:amount tr)))
(defn debit? [tr] (pos? (:amount tr)))
