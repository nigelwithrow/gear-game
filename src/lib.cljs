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

(defn with-gear-transform [ctx cnv f]
  (let
   [scale (/ (.-height cnv) 750)
    real-x (* scale 190)
    real-y (* scale 120)
    offset-x (/ (- (.-width cnv) real-x) 2)
    offset-y (- (.-height cnv) (* 1.5 real-y))]

    ((lib/with-ctx (fn [ctx]
                     (.translate ctx offset-x offset-y)
                     (.scale ctx scale scale)
                     (f ctx scale))) ctx)))

