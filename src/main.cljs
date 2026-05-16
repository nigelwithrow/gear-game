(ns main)

(def gamestate {})

; width / height
(def RATIO 1.78)

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
    (js/console.log (str gx) (str gy))
    (set! (.-width canvas) gw)
    (set! (.-height canvas) gh)
    (set! (.-left (.-style canvas)) (str gx "px"))
    (set! (.-top (.-style canvas)) (str gy "px"))))

(defn init []
  (let [canvas (.getElementById js/document "canvas")
        ctx (.getContext canvas "2d")]
    (update-canvas-size canvas)
    (js/addEventListener "resize" #(update-canvas-size canvas))
    (set! (.-fillStyle ctx) "green")
    (.fillRect ctx 20 10 150 100)))

(set! (.-onload js/window) init)

; (defn foo []
;   (js/alert "foo"))
; (println "Hello world!")

;; ADDED
; (defn average [a b]
;   (/ (+ a b) 2.0))
