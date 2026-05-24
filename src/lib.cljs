(ns lib
  (:require
   [state :refer [RATIO]]))

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

(defn with-ctx [ctx f]
  (.save ctx)
  (let [ret (f ctx)]
    (.restore ctx)
    ret))

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

    (lib/with-ctx ctx (fn [ctx]
                        (.translate ctx offset-x offset-y)
                        (.scale ctx scale scale)
                        (f ctx scale)))))

; (defn get-user-times [])
; 

; persists across hot-reloads
; calculate the maximum canvas-size in order to contain the entire canvas in the provided window
; dimensions while preserving the canvas ratio
; also returns the left and top offsets to center the resulting canvas in the window
;
; returns [canvas-width canvas-height offset-left offset-top]
(defn calc-size [ww wh]
  (let [est-width (* RATIO wh)

        [gh gx gy] (if (> est-width ww)
                     (let [gh (/ ww RATIO)]
                       [gh, 0, (/ (- wh gh) 2)])
                     [wh, (/ (- ww est-width) 2) 0])

        gw (* RATIO gh)]
    (map float [gw gh gx gy])))

(defn update-canvas-size [canvas]
  (let [wh js/window.innerHeight
        ww js/window.innerWidth
        [gw gh gx gy] (calc-size ww wh)]
    ; (js/console.log "CNV SIZE UPDATE " (str "(" gx ", " gy ") (" gw ", " gh ")"))
    (set! (.-width canvas) gw)
    (set! (.-height canvas) gh)
    (set! (.-left (.-style canvas)) (str gx "px"))
    (set! (.-top (.-style canvas)) (str gy "px"))
    ; attributes unused by html but used by us
    (set! (.-left canvas) gx)
    (set! (.-top canvas) gy)))

