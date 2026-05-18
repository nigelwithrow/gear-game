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

(defn with-scale [f x y]
  (fn [ctx]
    (.save ctx)
    (.scale ctx x y)
    (f ctx)
    (.restore ctx)))

(defn with-gear-transform [ctx cnv f]
  (let [x (/ (.-height cnv) 1000) y (/ (.-height cnv) 1000)]
    ((lib/with-ctx (fn [ctx]
                     (.scale ctx x y)
                     ; (.translate ctx 100 100)
                     (f ctx))) ctx)))

