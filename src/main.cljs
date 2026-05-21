(ns main (:require
          [assets]
          [game]
          [lib]
          [libgame]
          [listeners]
          [paths]
          [state :refer [canvas clicked-mouse ctx FRAME-DURATION INIT-GAME
                         raf-id running state]]))

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
  (when-not (:game @state)
    (let [amt (/ (.-height cnv) 50)]
      (set! (.-filter ctx) (str "blur(" amt "px)"))))

  ; (set! (.-fillStyle ctx) "rgba(0, 0, 0, 0.1)")
  (set! (.-fillStyle ctx) "grey")
  ; (.fillRect ctx 0 0 (.-width cnv) (.-height cnv))
  (.drawImage ctx assets/BG 0 0 (.-width cnv) (.-height cnv))

  ; draw game
  (if-some [game (:game @state)]

    (game/draw game cnv ctx)

    (do
      (set! (.-filter ctx) "none")

      (if-some [user-score (:user-score @state)]

        ()

        ; main menu
        (if (:main-menu @state)
          (.drawImage ctx assets/MENU
                      0 0
                      (.-width cnv) (.-height cnv))
          ; (set! (.-fillStyle ctx) "yellow")
          ; (.fillRect ctx (* (.-width cnv) 0.375) (* (.-height cnv) 0.5)
          ;            (* (.-width cnv) 0.25) (/ (* (.-height cnv) 2) 15))

          (.drawImage ctx assets/HOW-TO-PLAY
                      0 0
                      (.-width cnv) (.-height cnv))))))

; mouse-click animation
  (when-let [cm @clicked-mouse]
    (let [[x y] (:start cm)
          [x y] (lib/ezileamron cnv x y)]
      (.beginPath ctx)
      (set! (.-lineWidth ctx) 1.8)
      (set! (.-strokeStyle ctx) "rgba(100%, 100%, 100%, 70%)")
      ; TODO HERE ^ will this affect future strokes?
      (.arc ctx
            x y
            10
            0 (* 2 js/Math.PI))
      (.closePath ctx)
      (.stroke ctx))))

; main tick/update function
(defn game-update [cnv ctx]
  ; update mouse press (used for mouse-click anim)
  (if-some [cm @clicked-mouse]
    (swap! state #(assoc % :pressed (:start cm)))

    (when (:pressed @state)
      (swap! state #(assoc % :pressed nil))))

  (if-some [game (:game @state)]
    ; game
    (game/update_ game cnv ctx)

    (if-some [user-score (:user-score @state)]

      ; user score
      ()

      (if-some [main-menu (:main-menu @state)]

        ; main menu
        (if-some [cm @clicked-mouse]

          (let [new-main-menu
                (if-some [cm (:now cm)]
                  {:clicked cm}
                  {:clicked (:start cm)})]
            (swap! state assoc :main-menu new-main-menu))

          ; clicked
          (when-some [[x y] (:clicked main-menu)]
            (let [[x y] (lib/ezileamron cnv x y)]
              (cond
                ; start
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.35) (* (.-height cnv) 0.5)
                                   (* (.-width cnv) 0.3) (* (.-height cnv) 0.1))
                  (.isPointInPath ctx x y))
                (swap! state assoc :main-menu nil :user-score nil :how-to-play nil
                       :game (INIT-GAME))

                ; how to play
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.35) (* (.-height cnv) (+ 0.5 0.1 0.05))
                                   (* (.-width cnv) 0.3) (* (.-height cnv) 0.1))
                  (.isPointInPath ctx x y))
                (swap! state assoc :main-menu nil :user-score nil :game nil
                       :how-to-play {:clicked nil})

                ; quit
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.375) (* (.-height cnv) (+ 0.5 0.1 0.05 0.1 0.05))
                                   (* (.-width cnv) 0.25) (* (.-height cnv) 0.1))
                  (.isPointInPath ctx x y))
                (do
                  (set! (.-fillStyle ctx) "white")
                  (.fillRect ctx 0 0 (.-width cnv) (.-height cnv))
                  (.drawImage ctx
                              assets/MEME
                              0 (/ (- (.-height cnv) (/ (.-width cnv) 3.033)) 2)
                              (.-width cnv) (/ (.-width cnv) 3.033))
                  (listeners/destroy)
                  (reset! running false)
                  (when-some [raf-id @raf-id]
                    (js/cancelAnimationFrame raf-id)))

                ; click something other than button
                :else
                (let [new-main-menu {:clicked nil}]
                  (swap! state assoc :main-menu new-main-menu))))))

        ; how to play
        (let [how-to-play (:how-to-play @state)] ; safe
          (if-some [cm @clicked-mouse]

            (let [new-how-to-play
                  (if-some [cm (:now cm)]
                    {:clicked cm}
                    {:clicked (:start cm)})]
              (swap! state assoc :how-to-play new-how-to-play))

            ; clicked
            (when-some [[x y] (:clicked how-to-play)]
              (let [[x y] (lib/ezileamron cnv x y)]
                (cond

                  (do
                    (paths/rect-path ctx
                                     (* (.-width cnv) 0.35) (* (.-height cnv) 0.8)
                                     (* (.-width cnv) 0.3) (* (.-height cnv) 0.1))
                    (.isPointInPath ctx x y))
                  (swap! state assoc :user-score nil :game nil :how-to-play nil
                         :main-menu {:clicked nil})

                  :else
                  (let [new-how-to-play {:clicked nil}]
                    (swap! state assoc :how-to-play new-how-to-play)))))))))))

(def lag (atom 0))
(def start- (atom (js/Date.now)))

; thank you https://stackoverflow.com/a/25627639
(defn game-loop [_dt]
  (when-let [cnv @canvas]
    (when @running
      (reset! raf-id (js/requestAnimationFrame game-loop cnv))
      (let [current (js/Date.now)
            elapsed (- current @start-)]
        (reset! start- current)
        (reset! lag elapsed)

        (while (>= @lag FRAME-DURATION)
          (game-update cnv @ctx)
          (swap! lag - FRAME-DURATION))
        (let [lag-offset (/ @lag FRAME-DURATION)]
          (when @running
            (draw cnv @ctx lag-offset)))))))

;;
;; game entrypoint
;;

; initial/main function
; called once on page load/refresh, not called again on hot-reloads
(defn init []
  (attach-canvas)

  ; listen to resize event to adjust the canvas-size to fit
  (lib/update-canvas-size @canvas)
  (listeners/create)
  (game-loop nil))

(defonce promise-window-loaded
  (new js/Promise
       #(if (= js/document.readyState "complete")
          (%)
          (.addEventListener js/window "load" % #js {:once true}))))

(.then
 (js/Promise.all [promise-window-loaded assets/promise-all-loaded])
 init)

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
