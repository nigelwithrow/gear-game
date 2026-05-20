(ns lib)

;;
;; canvas, ctx & helpers
;;

; normalize coordinates with current canvas-size and offset from viewport
(defn normalize [cnv x y]
  (let [xx (.-width cnv)
        yy (.-height cnv)]
    [(/ (- x (.-left cnv)) xx) (/ (- y (.-top cnv)) yy)]))

; reverse normalize coordinates by multiplying with current canvas-size
(defn ezileamron [canvas x y]
  (let [xx (.-width canvas)
        yy (.-height canvas)]
    [(* x xx) (* y yy)]))

(defn with-ctx [f]
  (fn [ctx]
    (.save ctx)
    (let [ret (f ctx)
          _ (.restore ctx)]
      ret)))

(defn calc-scale [cnv] (/ (.-height cnv) 600))

(defn calc-transform
  ([cnv] (calc-transform cnv (calc-scale cnv)))
  ([cnv scale]
   (let [real-y (* scale 120)]
     [(* (/ 27.5 40) (.-width cnv)), (- (.-height cnv) (* 2.1 real-y))])))

(defn with-gear-transform [ctx cnv f]
  (let
   [scale (calc-scale cnv)
    [offset-x offset-y] (calc-transform cnv scale)]

    ((lib/with-ctx (fn [ctx]
                     (.translate ctx offset-x offset-y)
                     (.scale ctx scale scale)
                     (f ctx scale))) ctx)))

