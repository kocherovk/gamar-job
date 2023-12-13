(ns gamarjob.sources.tinek
  (:require [gamarjob.transaction :refer [Transaction source]]
            [gamarjob.utils :refer [timestamp-to-datetime]]
            [clojure.data.json :as json]))

(defrecord Tinek [id account currency amount date]
  Transaction
  (raw [tr] (-> tr meta :raw))
  (source [tr] (-> tr meta :source))
  (category [tr] (-> tr meta :raw (get-in ["spendingCategory" "name"])))
  (desc [tr] (-> tr meta :raw (get "description")))
  (to-myself? [_] false)
  (conversion? [_] false)
  (salary? [tr] (= (-> tr meta :raw (get-in ["merchant" "name"])) "Зарплата")))

(defn read-tinek-source-transactions [file-path]
  (->> file-path
       slurp
       json/read-str
       vals
       (map json/read-str)
       (mapcat #(get % "payload"))
       distinct))

(defn tinek-transaction [tr]
  (-> {:id (get tr "id") :account (get tr "account")}
      (assoc :currency (get-in tr ["accountAmount" "currency" "name"]))
      (assoc :amount (bigdec (get-in tr ["accountAmount" "value"])))
      (assoc :date (timestamp-to-datetime (get-in tr ["operationTime" "milliseconds"])))
      (update :amount #(if (= (get tr "type") "Credit") % (- %)))
      map->Tinek
      (with-meta {:raw tr :source :tinek})))

(defn read-from [path]
  (let [dir (clojure.java.io/file path)]
    (flatten (for [file (file-seq dir) :when (.isFile file)]
               (map tinek-transaction (read-tinek-source-transactions file))))))

(defn tinek? [tr] (= (source tr) :tinek))

(def category-map
  {"Отели" "RENT"
   "Рестораны" "CAFES"
   "Одежда и обувь" "SHOPPING"
   "Аптеки" "HEALTH"
   "Авиабилеты" "TRAVEL"
   "Фастфуд" "CAFES"
   "Наличные" "CASH"
   "Супермаркеты" "GROCERIES"
   "Услуги банка" "SERVICES"
   "Развлечения" "ENTERTAINMENT"
   "Турагентства" "TRAVEL"
   "Медицина" "HEALTH"
   "Кино" "ENTERTAINMENT"
   "Электроника и техника" "SHOPPING"
   "Связь" "SERVICES"
   "Мобильная связь" "SERVICES"})
