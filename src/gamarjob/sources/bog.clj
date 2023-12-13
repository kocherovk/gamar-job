(ns gamarjob.sources.bog
  (:require [gamarjob.transaction :refer [Transaction raw category source]]
            [clojure.data.json :as json]
            [clojure.string :refer [lower-case]]))

(defrecord Bog [id account currency amount date]
  Transaction
  (raw [tr]  (-> tr meta :raw))
  (source [tr] (-> tr meta :source))
  (category [tr]  (-> tr raw (get "pfmCatName")))
  (desc [tr] (-> tr raw (get "nomination")))
  (to-myself? [tr]
    (or
      (= (category tr) "text.pfm.child.category.internal.money.transfer")
      (and
        (= (category tr) "text.pfm.child.category.transfer.out")
        (= (lower-case (get (raw tr) "beneficiary")) "my tbc"))))
  (conversion? [tr] (= (category tr) "text.pfm.child.category.exchange"))
  (salary? [tr] (= (category tr) "text.pfm.child.category.salary")))

(defn read-bog-source-transactions [file-path]
  (->> file-path
       slurp
       json/read-str
       vals
       (map json/read-str)
       (mapcat #(get % "data"))
       distinct))

(defn bog-transaction [tr]
  (->
    (zipmap
      [:id :account :currency :amount :date]
      (vals (select-keys tr ["entryId" "accountKey" "ccy" "oppositeAmount" "operationDate"])))
    (update-in [:amount] bigdec)
    map->Bog
    (with-meta {:raw tr :source :bog})))

(defn read-from [path]
  (let [dir (clojure.java.io/file path)]
    (flatten (for [file (file-seq dir) :when (.isFile file)]
               (map bog-transaction (read-bog-source-transactions file))))))


(defn bog? [tr] (= (source tr) :bog))

(def category-map
  {"text.pfm.child.category.toys" "SHOPPING"
   "text.pfm.child.category.vacation.travel" "TRAVEL"
   "text.pfm.child.category.cinema.theater.concerts" "ENTERTAINMENT"
   "text.pfm.child.category.internet" "RENT"
   "text.pfm.child.category.other.transport" "TAXI"
   "text.pfm.child.category.hotels" "RENT"
   "text.pfm.child.category.other.entertainment" "ENTERTAINMENT"
   "text.pfm.child.category.supermarkets" "GROCERIES"
   "text.pfm.child.category.pharmacy" "HEALTH"
   "text.pfm.child.category.doctor" "HEALTH"
   "text.pfm.child.category.other.shopping" "SHOPPING"
   "text.pfm.child.category.taxi" "TAXI"
   "text.pfm.child.category.mobile" "SERVICES"
   "text.pfm.child.category.dentist" "HEALTH"
   "text.pfm.child.category.other.home" "RENT"
   "text.pfm.child.category.furniture" "RENT"
   "text.pfm.child.category.personal.care" "SHOPPING"
   "text.pfm.child.category.cosmetic.stores.and.perfumerry" "SHOPPING"
   "text.pfm.child.category.clothing.accessories" "SHOPPING"
   "text.pfm.child.category.cash.withdrawal" "CASH"
   "text.pfm.child.category.other.food.dining" "CAFES"
   "text.pfm.child.category.tv" "RENT"
   "text.pfm.child.category.eating.out" "CAFES"
   "text.pfm.child.category.alcohol.shops" "GROCERIES"
   "text.pfm.child.category.clubs" "ENTERTAINMENT"
   "text.pfm.child.category.bank.fees" "SERVICES"
   "text.pfm.child.category.home.maintenance" "RENT"
   "text.pfm.child.category.electronics" "SHOPPING"
   "text.pfm.child.category.electricity" "RENT"
   "text.pfm.child.category.online.shopping" "SHOPPING"
   "text.pfm.child.category.water" "RENT"
   "text.pfm.child.category.fast.food" "CAFES"
   "text.pfm.child.category.utility.gas" "RENT"})
