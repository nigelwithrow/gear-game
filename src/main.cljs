(ns main)

(def canvas (atom nil))
(def ctx (atom nil))

;; canvas & ctx helpers

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

; width / height
(def RATIO 1.78)

(def FPS 60)
(def FRAME-DURATION (/ 1000 FPS))

(def INIT-STATE
  {:pressed nil})

(def pressed-keys (atom #{}))
(def clicked-mouse (atom nil))

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
    (js/console.log "Canvas size updated:" (str gx ", " gy))
    (set! (.-width canvas) gw)
    (set! (.-height canvas) gh)
    (set! (.-left (.-style canvas)) (str gx "px"))
    (set! (.-top (.-style canvas)) (str gy "px"))
    ; attributes unused by html but used by us
    (set! (.-left canvas) gx)
    (set! (.-top canvas) gy)))

; persists across hot-reloads
(defonce state (atom INIT-STATE))

(def lag (atom 0))
(def start- (atom (js/Date.now)))

(defn game-render [_lag-offset]
  (when-let [cnv @canvas]
    (set! (.-fillStyle @ctx) "white")
    (.fillRect @ctx 0 0 (.-width cnv) (.-height cnv))
    (set! (.-fillStyle @ctx) "black")
    ; (set! (.-lineWidth @ctx) 1)
    (when-let [cm @clicked-mouse]
      (let [[x y] (:start cm)
            [x y] (ezileamron cnv x y)]
        (.fillRect @ctx (- x 5) (- y 5) 10 10)))))

(defn game-update []
  (when-let [cm @clicked-mouse]
    (swap! state #(assoc % :pressed (:start cm))))
  (when-not @clicked-mouse
    (when (:pressed @state)
      (swap! state #(assoc % :pressed nil)))))

(defn game-loop [_dt]
  (when-let [cnv @canvas]
    (js/requestAnimationFrame game-loop cnv)
    (let [current (js/Date.now)
          elapsed (- current @start-)]
      (reset! start- current)
      (reset! lag elapsed)

      (while (>= @lag FRAME-DURATION)
        (game-update)
        (swap! lag - FRAME-DURATION))
      (let [lag-offset (/ @lag FRAME-DURATION)]
        (game-render lag-offset)))))

(defn attach-canvas []
  (reset! canvas (.getElementById js/document "canvas"))
  (reset! ctx (.getContext @canvas "2d")))

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
              {:start (normalize cnv (.-clientX %) (.-clientY %))})))
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
                :move (normalize cnv (.-movementX %) (.-movementY %))
                :now (normalize cnv (.-clientX %) (.-clientY %)))))))

  (game-loop nil))

(set! (.-onload js/window) init)

(defn ^:dev/before-load stop []
  (reset! canvas nil)
  (reset! ctx nil))

(defn ^:dev/after-load start []
  (attach-canvas))

; (defn foo []
;   (js/alert "foo"))
; (println "Hello world!")

;; ADDED
; (defn average [a b]
;   (/ (+ a b) 2.0))
