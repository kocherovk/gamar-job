(ns gamarjob.utils
  (:import (java.time LocalDate LocalDateTime ZoneOffset)
           (java.time.format DateTimeFormatter)))

(defn zip [& seqs]
  (apply map vector seqs))

(def local-date-time-formatter
  (DateTimeFormatter/ISO_LOCAL_DATE_TIME))

(def local-date-formatter
  (DateTimeFormatter/ISO_LOCAL_DATE))

(defn parse-date [date-str]
  (.atStartOfDay
    (LocalDate/parse date-str local-date-formatter)))

(defn parse-datetime [date-str]
  (LocalDateTime/parse date-str local-date-time-formatter))

(defn timestamp-to-datetime [t]
  (str (LocalDateTime/ofEpochSecond (/ t 1000) 0 (ZoneOffset/UTC))))

(defn to-seconds [tr] (.getEpochSecond (.toInstant (get tr :date))))
(defn to-date-str [tr] (.format local-date-formatter (:date tr)))
