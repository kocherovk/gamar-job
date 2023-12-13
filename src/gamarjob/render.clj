(ns gamarjob.render)

(defn datapoint-fn [time-fn item-fn quantity-fn]
  (fn [source]
    {:time (time-fn source)
     :item (item-fn source)
     :quantity (quantity-fn source)}))

(defn step-line [data]
  {:data {:values data}
   :encoding {:x {:field "time" :type "temporal"}
              :y {:field "quantity" :type "quantitative"}
              :color {:field "item" :type "nominal"}}
   :mark {:type "line" :interpolate "step-after"}})

(defn linear-line [data]
  {:data {:values data}
   :encoding {:x {:field "time" :type "temporal"}
              :y {:field "quantity" :type "quantitative"}
              :color {:field "item" :type "nominal"}}
   :mark {:type "line" :point {:filled false} :interpolate "linear"}})

(defn expenses-per-month-area [data]
  {:data {:values data}
   :params [{:name "category"
             :bind "legend"
             :select {:type "point" :fields ["item"]}}]
   :encoding {:x {:field "time"
                  :timeUnit "yearmonth"
                  :type "temporal"}
              :y {:aggregate "sum"
                  :field "quantity"
                  :type "quantitative"}
              :color {:field "item" :type "nominal"}
              :opacity {:condition {:param "category"
                                    :value 1}
                        :value 0.2}}
   :mark "area"})

(defn expenses-arc [data]
  {:data {:values data}
   :encoding {:theta
              {:aggregate "sum"
               :field "quantity"
               :type "quantitative"}
              :color {:field "item" :type "nominal"}}
   :mark {:type "arc" :tooltip true}})

(defn layered-plot [& layers]
  {:layer layers
   :width 600 :height 400})

(defn total+salary+expenses [total salary expenses]
  (layered-plot (step-line total)
                (linear-line salary)
                (linear-line expenses)))

(defn expenses-per-month [expenses]
  (layered-plot (expenses-per-month-area expenses)))

(defn expenses-categorised [expenses]
  (layered-plot (expenses-arc expenses)))

(defn render-table-row [row]
  [:tr (for [val row] [:td val])])

(defn render-table [rows]
  [:table (map render-table-row rows)])
