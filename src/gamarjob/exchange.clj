(ns gamarjob.exchange)

(defn round [val]
  (bigdec (/ (int (* val 100)) 100)))

(def fixed-rates
  {["GEL" "USD"] 0.3687M
   ["USD" "USD"] 1M
   ["EUR" "USD"] 1.09M
   ["RUB" "USD"] 0.01119M
   ["ETH" "USD"] 2077.25M})

(def rand-rates
  {["UE" "UE"] 1M
   ["GEL" "UE"] (bigdec (rand 2))
   ["USD" "UE"] (bigdec (rand 2))
   ["EUR" "UE"] (bigdec (rand 2))
   ["RUB" "UE"] (bigdec (rand 2))
   ["ETH" "UE"] (bigdec (rand 2))})

(defn exchange [rates from to amount]
  (round (* amount (get rates [from to]))))

(defn convert [rates currency transaction]
  (let [new-amount (exchange rates (:currency transaction) currency (:amount transaction))]
    (assoc transaction :amount new-amount :currency currency)))

(def to-usd (partial convert fixed-rates "USD"))
(def obscure (partial convert rand-rates "UE"))
(defn usd [amount from] (exchange fixed-rates from "USD" amount))
