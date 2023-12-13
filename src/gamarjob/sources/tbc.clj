(ns gamarjob.sources.tbc
  (:require [gamarjob.transaction :refer [Transaction raw category desc source]]
            [clojure.data.json :as json]
            [gamarjob.utils :refer [timestamp-to-datetime]]
            [clojure.string :refer [lower-case]]))

(defrecord Tbc [id account currency amount date]
  Transaction
  (raw [tr]  (-> tr meta :raw))
  (source [tr] (-> tr meta :source))
  (category [tr]
    (let [source (raw tr)
          subcategory (get-in source ["subcategories" 0])]
      (or (get subcategory "subcategoryCode")
          (get subcategory "categoryCode"))))
  (desc [tr] (-> tr raw (get "description")))
  (to-myself? [tr]
    (let [account-name (get (raw tr) "partnerAccountName")
          category (category tr)
          description (lower-case (or (desc tr) ""))]
      (or
        (and (= account-name "ქოჩეროვ ქირილლ")
             (= description "private transfer")
             (= category "OTHER_INCOMES"))
        (= description "transfer between your accounts")
        (= description "transfer to own account"))))
  (conversion? [tr] (= (get (raw tr) "transactionType") "CURR_EXCHANGE"))
  (salary? [_] false))

(defn read-tbc-source-transactions [source-path]
  (->> source-path
       slurp
       json/read-str
       vals
       (mapcat json/read-str)
       distinct))

(defn tbc-transaction [transaction]
  (->
   (zipmap
   [:id :account :currency :amount :date]
   (vals
    (select-keys
     transaction
     ["id" "clientAccountNumber" "currency" "amount" "date"])))
   (update-in [:id] str)
   (update-in [:amount] bigdec)
   (update-in [:date] timestamp-to-datetime)
   map->Tbc
   (with-meta {:raw transaction :source :tbc})))

(defn read-from [path]
  (let [dir (clojure.java.io/file path)]
    (flatten (for [file (file-seq dir) :when (.isFile file)]
               (map tbc-transaction (read-tbc-source-transactions file))))))

(defn tbc? [tr] (= (source tr) :tbc))

(def category-map
  {"OTHER_ENTERTAINMENT" "ENTERTAINMENT"
   "CLOTHING_SHOES" "SHOPPING"
   "ENTERTAINMENT" "ENTERTAINMENT"
   "FAMILY" "RENT"
   "OTHER_SHOPPING" "SHOPPING"
   "EDUCATION" "SERVICES"
   "ELECTRONICS" "SHOPPING"
   "HOTEL" "RENT"
   "TV_INTERNET" "SERVICES"
   "FURNITURE_RENOVATION" "RENT"
   "DRUG" "HEALTH"
   "PUBLIC_TRANSPORT_TAXI" "TAXI"
   "CASHOUT" "CASH"
   "ELECTRICITY" "RENT"
   "RESTAURANT" "CAFES"
   "ACCESSORIES" "SHOPPING"
   "TRANSPORTATION" "TRAVEL"
   "CINEMA_THEATRE" "ENTERTAINMENT"
   "PERSONAL_CARE" "SHOPPING"
   "MEDICAL_EXPENSE" "HEALTH"
   "TELEPHONE_MOBILE" "SERVICES"
   "GAS" "RENT"
   "FEES" "SERVICES"
   "GROCERIES" "GROCERIES"
   "Rent" "RENT"
   "Services" "SERVICES"
   "DELIVERED_FOOD" "DELIVERED_FOOD"})
