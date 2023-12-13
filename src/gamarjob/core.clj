(ns gamarjob.core
  (:require [gamarjob.sources.bog :as bog]
            [gamarjob.sources.tbc :as tbc]
            [gamarjob.sources.tinek :as tinek]
            [gamarjob.transaction :as tr]
            [gamarjob.exchange :as exchange]
            [gamarjob.algo :as algo]))

(defn category [tr]
  (or
    (:category (meta tr))
    (algo/categorize algo/category-keywords tr)
    (and (tbc/tbc? tr) (get tbc/category-map (tr/category tr)))
    (and (bog/bog? tr) (get bog/category-map (tr/category tr)))
    (and (tinek/tinek? tr) (get tinek/category-map (tr/category tr)))
    "OTHER"))

(defn usd-wealth [balances]
  (reduce + (for [[[_ currency] amount] balances]
              (exchange/usd amount currency))))
