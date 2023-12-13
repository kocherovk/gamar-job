(ns gamarjob.algo
  (:require [clojure.string :refer [lower-case includes?]]
            [gamarjob.transaction :refer [between? before? desc]]))

(defn balance-after [balance transaction key-fn]
  (let [key (key-fn transaction)
        amount (:amount transaction)]
    (update-in balance [key] + amount)))

(defn enrich-transaction [transaction balance key-fn]
  (assoc transaction :balance (get balance (key-fn transaction))))

(defn enrich-transactions [key-fn balance transactions]
  (loop [balance balance
         transactions transactions
         enriched-transactions []]
    (if (seq transactions)
      (let [transaction (first transactions)
            new-balance (balance-after balance transaction key-fn)
            enriched-transaction (enrich-transaction transaction new-balance key-fn)]
        (recur new-balance
               (rest transactions)
               (conj enriched-transactions enriched-transaction)))
      [balance enriched-transactions])))

(defn balance-at [date transactions initial-balances]
  (first
    (enrich-transactions
      (fn [tr] [(:account tr) (:currency tr)])
      initial-balances
      (filter #(before? % date) transactions))))

(defn strip-time [t]
  (assoc t :date (subs (:date t) 0 10)))

(defn partition-by-periods [periods transactions]
  (for [period periods]
    [period (filter #(apply between? % period) transactions)]))

(defn aggregate-by [key-fn field op data]
  (map
    (fn [partition]
      (assoc
        (key-fn (first partition))
        field
        (reduce op (map field partition))))
    (partition-by
      key-fn
      data)))

(defn aggregate-per-period [transactions periods]
  (for [[period transactions]
        (partition-by-periods periods transactions)]
    [period (apply + (map :amount transactions))]))

(def category-keywords
  {"SERVICES"
   ["fee" "google storage" "spotify" "apple.com/bill"
    "jetbrains" "magti" "audible" "protonmail" "dtac"
    "proton" "netflix"]
   "RENT"
   ["eteri" "eiteri" "airbnb" "egorenkov igor"]
   "SHOPPING"
   ["wildberries"]
   "HEALTH"
   [" dent" " pharm"]
   "CASH"
   ["withdrawal"]
   "GROCERIES"
   ["yalcinmarket" "vine cellar" "goodwill" "carrefour" "market" "7-11"]
   "CAFES"
   ["burger king" "mcdonald" "sakhli"]
   "DELIVERED_FOOD"
   ["wolt" "glovo" "www.grab.com" "grab food" "bolt"]
   "GAMES"
   ["steam" "gog"]
   "ENTERTAINMENT"
   ["bird"]
   "TAXI"
   ["yandex.taxi" "yandex taxi" "grab rides" "www.grabtaxi.com"]})

(defn categorize [keywords tr]
  (let [description (lower-case (desc tr))]
    (first
      (filter
        some?
        (for [[c kw] keywords k kw]
          (when (includes? description k) c))))))
