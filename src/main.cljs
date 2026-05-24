(ns main (:require
          [assets]
          [game]
          [lib]
          [libgame]
          [listeners]
          [paths]
          [state :refer [cached-levels canvas clicked-mouse ctx FRAME-DURATION
                         INIT-GAME raf-id running state]]))

(defn attach-canvas []
  (let [cnv (.getElementById js/document "canvas")
        context (.getContext cnv "2d")]
    (reset! canvas cnv)
    (reset! ctx context)))

;;
;; game loop
;;

; main draw/render function
(defn draw [s cnv ctx _lag-offset]
  (when-not (:game s)
    (let [amt (/ (.-height cnv) 50)]
      (set! (.-filter ctx) (str "blur(" amt "px)"))))

  ; (set! (.-fillStyle ctx) "rgba(0, 0, 0, 0.1)")
  (set! (.-fillStyle ctx) "grey")
  ; (.fillRect ctx 0 0 (.-width cnv) (.-height cnv))
  (.drawImage ctx assets/BG 0 0 (.-width cnv) (.-height cnv))

  ; draw game
  (if-some [game (:game s)]

    (game/draw game cnv ctx)

    (do
      (set! (.-filter ctx) "none")

      (if (:user-score s)

        (do
          (.drawImage ctx assets/USER-SCORE-BG
                      0 0
                      (.-width cnv) (.-height cnv))

          (lib/with-ctx ctx
            (fn [ctx]
              (let [usable-height (* 0.5 (.-height cnv))
                    usable-width (* 0.8 (.-width cnv))
                    y-div (/ usable-height 20)
                    x-div (/ usable-width 4)]

                (.translate ctx (* 0.1 (.-width cnv)) (* 0.1625 (.-height cnv)))
                (set! (.-fillStyle ctx) "white")
                (set! (.-font ctx) (str "bold " (* (.-height cnv) 0.04) "px serif"))

                (run!
                 (fn [i]
                   (let [time ((keyword (str i)) @state/cached-levels)
                         text (and time (str i ":  " (quot time 1000) "." (mod time 1000) "s"))]
                     (when time
                       (.fillText ctx
                                  text
                                  0
                                  (* 2 i y-div)))))
                 (range 1 11))

                (run!
                 (fn [i]
                   (let [time ((keyword (str i)) @state/cached-levels)
                         text (and time (str i ":  " (quot time 1000) "." (mod time 1000) "s"))]
                     (when time
                       (.fillText ctx
                                  text
                                  x-div
                                  (* 2 (- i 10) y-div)))))
                 (range 11 21))

                (run!
                 (fn [i]
                   (let [time ((keyword (str i)) @state/cached-levels)
                         text (and time (str i ":  " (quot time 1000) "." (mod time 1000) "s"))]
                     (when time
                       (.fillText ctx
                                  text
                                  (* 2 x-div)
                                  (* 2 (- i 20) y-div)))))
                 (range 21 31))

                (run!
                 (fn [i]
                   (let [time ((keyword (str i)) @state/cached-levels)
                         text (and time (str i ":  " (quot time 1000) "." (mod time 1000) "s"))]
                     (when time
                       (.fillText ctx
                                  text
                                  (* 3 x-div)
                                  (* 2 (- i 30) y-div)))))
                 (range 31 41))))))

        (if (:main-menu s) ; main menu

          (do
            (.drawImage ctx assets/MENU
                        0 0
                        (.-width cnv) (.-height cnv))

            (when-not @state/cached-levels ; disable continue
              (let [start-y (* (.-height cnv) 0.5)
                    btn-height (/ (.-height cnv) 15.0)
                    btn-gap (/ (.-height cnv) 60.0)
                    bhbg (+ btn-height btn-gap)]

                (.save ctx)
                (set! (.-fillStyle ctx) "rgba(0%, 0%, 0%, 70%)")
                (.fillRect ctx
                           (* (.-width cnv) 0.35) (+ start-y bhbg)
                           (* (.-width cnv) 0.3) btn-height)

                (.fillRect ctx
                           (* (.-width cnv) 0.35) (+ start-y bhbg bhbg)
                           (* (.-width cnv) 0.3) btn-height)
                (.restore ctx))))

          (if (:how-to-play s)

            (.drawImage ctx assets/HOW-TO-PLAY ; how to play
                        0 0
                        (.-width cnv) (.-height cnv))

            (if (:game-over s)

              () ; game over

              (if-some [next-level (:next-level s)]
                (let [finished-lvl (dec (:lvl next-level))]

                  (.drawImage ctx assets/NEXT-LEVEL ; next level
                              0 0
                              (.-width cnv) (.-height cnv))

                  (.save ctx)
                  (set! (.-fillStyle ctx) "white")
                  (set! (.-font ctx) (str "bold " (* (.-height cnv) 0.0524) "px serif"))
                  (.fillText ctx
                             (str finished-lvl)
                             (/ (* (.-width cnv) 64) 135)
                             (* (.-height cnv) 0.4596))

                  (let [time-ms ((keyword (str finished-lvl)) (:times next-level))
                        time (str (quot time-ms 1000) "." (mod time-ms 1000) "s")]
                    (.fillText ctx
                               time
                               (/ (* (.-width cnv) 64) 135)
                               (* (.-height cnv) 0.5263)))
                  (.restore ctx))

                () ; unreachable
                )))))))

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
      (if-some [cm @clicked-mouse]

        (let [new-user-score
              (if-some [cm (:now cm)]
                {:clicked cm}
                {:clicked (:start cm)})]
          (swap! state assoc :user-score new-user-score))

        ; clicked
        (when-some [[x y] (:clicked user-score)]
          (let [[x y] (lib/ezileamron cnv x y)]
            (cond

              (do
                (paths/rect-path ctx
                                 (* (.-width cnv) 0.35) (* (.-height cnv) 0.8)
                                 (* (.-width cnv) 0.3) (* (.-height cnv) 0.1))
                (.isPointInPath ctx x y))
              (swap! state assoc
                     :user-score nil
                     :main-menu {:clicked nil})

              :else
              (let [new-user-score {:clicked nil}]
                (swap! state assoc :user-score new-user-score))))))

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
            (let [[x y] (lib/ezileamron cnv x y)
                  start-y (* (.-height cnv) 0.5)
                  btn-height (/ (.-height cnv) 15.0)
                  btn-gap (/ (.-height cnv) 60.0)
                  bhbg (+ btn-height btn-gap)]
              (cond
                ; start game
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.35) start-y
                                   (* (.-width cnv) 0.3) btn-height)
                  (.isPointInPath ctx x y))
                (let [new-game (INIT-GAME 1 {})]
                  ; (js/console.log (clj->js new-game))
                  (swap! state assoc
                         :main-menu nil
                         :game new-game))

                ; continue
                (and
                 @state/cached-levels
                 (do
                   (paths/rect-path ctx
                                    (* (.-width cnv) 0.35) (+ start-y bhbg)
                                    (* (.-width cnv) 0.3) btn-height)
                   (.isPointInPath ctx x y)))
                (let [level (state/get-continue-level)]
                  (if (>= level 41)
                    (swap! state assoc
                           :main-menu nil
                           :next-level {:times @state/cached-levels :lvl 41})

                    (swap! state assoc
                           :main-menu nil
                           :game (INIT-GAME level @state/cached-levels))))

                ; user scores
                (and
                 @state/cached-levels
                 (do
                   (paths/rect-path ctx
                                    (* (.-width cnv) 0.35) (+ start-y bhbg bhbg)
                                    (* (.-width cnv) 0.3) btn-height)
                   (.isPointInPath ctx x y)))
                (swap! state assoc
                       :main-menu nil
                       :user-score true)

                ; see how to play
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.35) (+ start-y bhbg bhbg bhbg)
                                   (* (.-width cnv) 0.3) btn-height)
                  (.isPointInPath ctx x y))
                (swap! state assoc
                       :main-menu nil
                       :how-to-play {:clicked nil})

                ; quit
                (do
                  (paths/rect-path ctx
                                   (* (.-width cnv) 0.375) (+ start-y bhbg bhbg bhbg bhbg)
                                   (* (.-width cnv) 0.25) btn-height)
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
        (if-some [how-to-play (:how-to-play @state)]

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
                  (swap! state assoc
                         :how-to-play nil
                         :main-menu {:clicked nil})

                  :else
                  (let [new-how-to-play {:clicked nil}]
                    (swap! state assoc :how-to-play new-how-to-play))))))

          ; game over
          (if-some [game-over (:game-over @state)]

            ()

            ; next level screen

            (if-some [next-level (:next-level @state)] ; safe

              (if-some [cm @clicked-mouse]

                (let [new-next-level (assoc next-level :clicked
                                            (if-some [cm (:now cm)]
                                              cm
                                              (:start cm)))]
                  (swap! state assoc :next-level new-next-level))

                (when-some [[x y] (:clicked next-level)] ; clicked
                  (let [[x y] (lib/ezileamron cnv x y)]
                    (cond

                      (do
                        (paths/rect-path ctx
                                         (* (.-width cnv) 0.35) (* (.-height cnv) 0.8)
                                         (* (.-width cnv) 0.3) (* (.-height cnv) 0.1))
                        (.isPointInPath ctx x y))
                      (do
                        (js/console.log (:lvl next-level))
                        (swap! state assoc
                               :next-level nil
                               :game (INIT-GAME
                                      (:lvl next-level)
                                      (:times next-level))))

                      :else
                      (let [new-next-level (assoc next-level :clicked nil)]
                        (swap! state assoc :next-level new-next-level))))))

              () ; unreachable
              )))))))

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
            (draw @state cnv @ctx lag-offset)))))))

;;
;; game entrypoint
;;

; initial/main function
; called once on page load/refresh, not called again on hot-reloads
(defn init []
  (when-some [cached-levels-raw (js/localStorage.getItem state/LOCAL-STORAGE-KEY)]
    (try
      (let [json (js/JSON.parse cached-levels-raw)]
        (if (object? json)
          (let [obj (js->clj json)
                obj (reduce-kv (fn [obj k v] (assoc obj (keyword k) v)) {} obj)]
            (reset! cached-levels obj))
          (js/console.error "Could not recover cached-levels: not a JSON object")))
      (catch js/Error e
        (js/console.error "Could not recover cached-levels" e))))

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
