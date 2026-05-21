(ns listeners
  (:require
   [lib]
   [state :refer [canvas clicked-mouse pressed-keys]]))

; upon resize, reset any mouse clicks (for game dev's convenience)
(defn resize-listener []
  (when-let [cnv @canvas]
    (reset! clicked-mouse nil)
    (lib/update-canvas-size cnv)))

; listen to keyup and keydown events to maintain keys pressed at any instant
(defn keydown-listener [e]
  (when-not (nil? @canvas)
    (swap! pressed-keys conj (.-code e))))
(defn keyup-listener [e]
  (when-not (nil? @canvas)
    (swap! pressed-keys disj (.-code e))))

; listen to pointerdown and pointerup events to maintain the position at which the mouse was clicked
(defn pointerdown-listener [e]
  (when-let [cnv @canvas]
    (when (= (.-button e) 0)
      (reset! clicked-mouse
              {:start (lib/normalize cnv (.-clientX e) (.-clientY e))}))))
(defn pointerup-listener []
  (when-not (nil? @canvas)
    (reset! clicked-mouse nil)))

; listen to pointermove event to maintain the mouse position & movement delta while it is clicked
(defn pointermove-listener [e]
  (when @clicked-mouse
    (let [cnv @canvas] ; safe because `cm`
      (swap! clicked-mouse
             (fn [cm]
               (let [[now-x now-y] (lib/normalize cnv (.-clientX e) (.-clientY e))]
                 (assoc cm
                        :now [now-x now-y]
                        :move (let [[dx dy] (if-some [[prev-x prev-y] (:now cm)]
                                              [(- now-x prev-x) (- now-y prev-y)]
                                              (let [[start-x start-y] (:start cm)]
                                                [(- now-x start-x) (- now-y start-y)]))
                                    [x y] (or (:move cm) [0 0])] [(+ x dx) (+ y dy)]))))))))

(defn create []
  (js/addEventListener "resize" resize-listener)

  (js/addEventListener "keydown" keydown-listener)
  (js/addEventListener "keyup" keyup-listener)

  (js/addEventListener "pointerdown" pointerdown-listener)
  (js/addEventListener "pointerup" pointerup-listener)

  (js/addEventListener "pointermove" pointermove-listener))

(defn destroy []
  (js/removeEventListener "resize" resize-listener)

  (js/removeEventListener "keydown" keydown-listener)
  (js/removeEventListener "keyup" keyup-listener)

  (js/removeEventListener "pointerdown" pointerdown-listener)
  (js/removeEventListener "pointerup" pointerup-listener)

  (js/removeEventListener "pointermove" pointermove-listener))
