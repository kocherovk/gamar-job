(ns gamarjob.sources.eth
  (:require [gamarjob.transaction :refer [Transaction]]
           [clojure.data.csv :as csv]
           [clojure.java.io :as io]))

(defrecord Eth [id account currency amount date]
  Transaction
  (raw [tr] (-> tr meta :raw))
  (source [tr] (-> tr meta :source))
  (category [_] "OTHER")
  (desc [_] "eth")
  (to-myself? [_] false)
  (conversion? [_] false)
  (salary? [_] false))

(defn read-eth-source-transactions [source-path]
  (with-open [reader (io/reader source-path)]
    (doall (next (csv/read-csv reader)))))

(defn eth-transaction [transaction]
  (with-meta
    (->Eth (transaction 0)
           "metamask-eth"
           "ETH"
           (- (bigdec (transaction 7))
              (bigdec (transaction 8)))
           (transaction 3))
    {:raw transaction :source :metamask}))

(defn read-from [file-path]
  (map eth-transaction (read-eth-source-transactions file-path)))

