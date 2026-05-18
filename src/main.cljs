(ns main (:require
          [assets]
          [lib]
          [paths]))

;;
;; constants
;;

; width / height
(def RATIO 1.5)

(def FPS 60)
(def FRAME-DURATION (/ 1000 FPS))

(def GEAR-STICK-RADIUS 13)

(def INIT-STATE
  {:level nil
   :gear-location [95, 60] ; relative to gear render location in gear-path scale
   :pressed nil})

;;
;; state and stateful values
;;

; persists across hot-reloads
(defonce state (atom INIT-STATE))

(def canvas (atom nil))
(def ctx (atom nil))

(def pressed-keys (atom #{}))
(def clicked-mouse (atom nil))

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

(defn draw-gear [cnv ctx cm?]
  (lib/with-gear-transform
    ctx
    cnv
    (fn [ctx]
      (let [path (new js/Path2D paths/gear-socket)]
        (set! (.-fillStyle ctx) "black")
        (when-let [cm cm?]
          (let [[x y] (:start cm)
                [x y] (lib/ezileamron cnv x y)]
            (when (.isPointInPath ctx path x y)
              (set! (.-fillStyle ctx) "red"))))
        (.fill ctx path)

        ; (set! (.-fillStyle ctx) "green")
        ; (doseq [s (vals paths/GEAR_AREAS)]
        ;   (.fill ctx (new js/Path2D s)))
        ))))

; main draw/render function
(defn draw [cnv ctx _lag-offset]
  ; (set! (.-fillStyle ctx) "rgba(0, 0, 0, 0.1)")
  (set! (.-fillStyle ctx) "grey")
  ; (.fillRect ctx 0 0 (.-width cnv) (.-height cnv))
  (.drawImage ctx assets/BG 0 0 (.-width cnv) (.-height cnv))

  (when-let [cm @clicked-mouse]
    (let [[x y] (:start cm)
          [x y] (lib/ezileamron cnv x y)]
      (.beginPath ctx)
      (set! (.-strokeStyle ctx) "rgba(100%, 100%, 100%, 50%)")
      (.arc ctx
            x y
            10
            0 (* 2 js/Math.PI))
      (.closePath ctx)
      (.stroke ctx)))

  ; check if mouse is on gear
  (when-some [cm @clicked-mouse]
    (when-some [[x y] (:now cm)]
      (let [[x y] (lib/ezileamron cnv x y)]
        (when-some [gear (paths/get-gear ctx cnv x y)]
          (set! (.-font ctx) "bold 48px serif")
          (.fillText ctx (name gear) 100 200)))))

  (draw-gear cnv ctx @clicked-mouse)

  ; draw gear stick
  (when-some [[x y] (:gear-location @state)]
    (lib/with-gear-transform ctx cnv
      (fn [ctx scale]
        ; rod
        (set! (.-fillStyle ctx) "red")
        (.beginPath ctx)
        (.arc ctx
              x y
              GEAR-STICK-RADIUS
              0 (* 2 js/Math.PI))
        (.closePath ctx)
        (.fill ctx)

        ; bulb
        (.beginPath ctx)
        (set! (.-fillStyle ctx) "red")
        (.arc ctx
              x (- y 240)
              (* 4 GEAR-STICK-RADIUS)
              0 (* 2 js/Math.PI))
        (.closePath ctx)
        (.fill ctx)))))

; main tick/update function
(defn game-update [cnv ctx]
  (if-some [cm @clicked-mouse]
    (swap! state #(assoc % :pressed (:start cm)))

    (when (:pressed @state)
      (swap! state #(assoc % :pressed nil)))))

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
