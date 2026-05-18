(ns paths
  (:require
   [lib]))

(defonce GEAR_CIRCLE
  (str
   "m 15 0"
   "a 15 15, 0, 0, 1 15 15"
   "a 15 15, 0, 0, 1 -15 15"
   "a 15 15, 0, 0, 1 -15 -15"
   "a 15 15, 0, 0, 1 15 -15"
   "z"))

(defonce GEAR_POSITIONS
  {:1 [0 0] :3 [80 0] :5 [160 0]
   :2 [0 90] :4 [80 90] :R [160 90]})

(defonce GEAR_AREAS
  (reduce-kv
   (fn [obj k [x y]]
     (assoc
      obj
      k
      (str "m " x " " y " " GEAR_CIRCLE)))
   {}
   GEAR_POSITIONS))

(defonce GEAR_PATHS
  (reduce-kv
   (fn [obj k [x y]]
     (assoc
      obj
      k
      (new js/Path2D (str "m " x " " y " " GEAR_CIRCLE))))
   {}
   GEAR_POSITIONS))

(def gear-socket
  (str
   "m 15 0"
   "a 15 15, 0, 0, 1 15 15"
   "v 25"
   "a 5 5, 0, 0, 0, 5, 5"
   "h 40"
   "a 5, 5, 0, 0, 0, 5 -5"
   "v -25"
   "a 15, 15, 0, 0, 1, 15, -15"
   "a 15, 15, 0, 0, 1, 15, 15"
   "v 25"
   "a 5 5, 0, 0, 0, 5, 5"
   "h 40"
   "a 5, 5, 0, 0, 0, 5 -5"
   "v -25"
   "a 15, 15, 0, 0, 1, 15, -15"
   "a 15, 15, 0, 0, 1, 15, 15"
   "v 90"
   "a 15 15, 0, 0, 1 -15 15"
   "a 15 15, 0, 0, 1 -15 -15"
   "v -25"
   "a 5, 5, 0, 0, 0, -5, -5"
   "h -40"
   "a 5, 5, 0, 0, 0, -5, 5"
   "v 25"
   "a 15 15, 0, 0, 1 -15 15"
   "a 15 15, 0, 0, 1 -15 -15"
   "v -25"
   "a 5, 5, 0, 0, 0, -5, -5"
   "h -40"
   "a 5, 5, 0, 0, 0, -5, 5"
   "v 25"
   "a 15 15, 0, 0, 1 -15 15"
   "a 15 15, 0, 0, 1 -15 -15"
   "v -90"
   "a 15 15, 0, 0, 1 15 -15"
   "z"))

; returns { :1 Path2D :2 Path2D :3 Path2D :4 Path2D :5 Path2D :r Path2D }

(defn get-gear [ctx cnv x y]
  (lib/with-gear-transform ctx cnv
    (fn [ctx]
      (reduce-kv
       (fn [acc k path]
           ; (set! (.-fillStyle ctx) "blue")
           ; (.fill ctx path)
         (if acc
           acc
           (if (.isPointInPath ctx path x y) k nil)))
       nil
       GEAR_PATHS))))
