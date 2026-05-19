(ns main (:require
          [assets]
          [game]
          [lib]
          [paths]
          [state :refer [FRAME-DURATION
                         RATIO
                         canvas
                         clicked-mouse
                         ctx
                         pressed-keys
                         state]]))

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

(defn attach-canvas []
  (let [cnv (.getElementById js/document "canvas")
        context (.getContext cnv "2d")]
    (reset! canvas cnv)
    (reset! ctx context)))

;;
;; game loop
;;

; main draw/render function
(defn draw [cnv ctx _lag-offset]
  ; (set! (.-fillStyle ctx) "rgba(0, 0, 0, 0.1)")
  (set! (.-fillStyle ctx) "grey")
  ; (.fillRect ctx 0 0 (.-width cnv) (.-height cnv))
  (.drawImage ctx assets/BG 0 0 (.-width cnv) (.-height cnv))

  ; draw game
  (if-some [game (:game @state)]
    (game/draw game cnv ctx)
    ())

  ; mouse-click animation
  (when-let [cm @clicked-mouse]
    (let [[x y] (:start cm)
          [x y] (lib/ezileamron cnv x y)]
      (.beginPath ctx)
      (set! (.-lineWidth ctx) 1.8)
      (set! (.-strokeStyle ctx) "rgba(100%, 100%, 100%, 70%)")
      (.arc ctx
            x y
            10
            0 (* 2 js/Math.PI))
      (.closePath ctx)
      (.stroke ctx))))

; main tick/update function
(defn game-update [cnv ctx]
  ; update mouse press
  (if-some [cm @clicked-mouse]
    (swap! state #(assoc % :pressed (:start cm)))

    (when (:pressed @state)
      (swap! state #(assoc % :pressed nil))))

  (if-some [game (:game @state)]
    ; game
    (game/update_ game cnv ctx)

    ; main menu
    (if (:credits @state)
      ()
      ())))

(def lag (atom 0))
(def start- (atom (js/Date.now)))

; thank you https://stackoverflow.com/a/25627639
(defn game-loop [_dt]
  (when-let [cnv @canvas]
    (js/requestAnimationFrame game-loop cnv)
    (let [current (js/Date.now)
          elapsed (- current @start-)]
      (reset! start- current)
      (reset! lag elapsed)

      (while (>= @lag FRAME-DURATION)
        (game-update cnv @ctx)
        (swap! lag - FRAME-DURATION))
      (let [lag-offset (/ @lag FRAME-DURATION)]
        (draw cnv @ctx lag-offset)))))

;;
;; game entrypoint
;;

; initial/main function
; called once on page load/refresh, not called again on hot-reloads
(defn init []
  (attach-canvas)

  ; listen to resize event to adjust the canvas-size to fit
  (update-canvas-size @canvas)
  (js/addEventListener
   "resize"
  ; upon resize, reset any mouse clicks (for game dev's convenience)
   #(when-let [cnv @canvas]
      (reset! clicked-mouse nil)
      (update-canvas-size cnv)))

  ; listen to keyup and keydown events to maintain keys pressed at any instant
  (js/addEventListener
   "keydown"
   #(if-not (nil? @canvas)
      (swap! pressed-keys conj (.-code %))
      ()))
  (js/addEventListener
   "keyup"
   #(if-not (nil? @canvas)
      (swap! pressed-keys disj (.-code %))
      ()))

  ; listen to pointerdown and pointerup events to maintain the position at which the mouse was clicked
  (js/addEventListener
   "pointerdown"
   #(when-let [cnv @canvas]
      (reset! clicked-mouse
              {:start (lib/normalize cnv (.-clientX %) (.-clientY %))})))
  (js/addEventListener
   "pointerup"
   #(if-not (nil? @canvas)
      (reset! clicked-mouse nil)
      ()))

  ; listen to pointermove event to maintain the mouse position & movement delta while it is clicked
  (js/addEventListener
   "pointermove"
   #(when-let [cm @clicked-mouse]
      (let [cnv @canvas] ; safe because `cm`
        (reset!
         clicked-mouse
         (assoc cm
                :move (lib/normalize cnv (.-movementX %) (.-movementY %))
                :now (lib/normalize cnv (.-clientX %) (.-clientY %)))))))

  (game-loop nil))

(set! (.-onload js/window) (assets/once-all-loaded init))

;;
;; debug utilities
;;

; register hook to call before hot-reload happens
(defn ^:dev/before-load stop []
  (reset! canvas nil)
  (reset! ctx nil))

; register hook to call after hot-reload happens
(defn ^:dev/after-load start []
  (attach-canvas))
