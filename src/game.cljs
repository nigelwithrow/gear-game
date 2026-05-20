(ns game
  (:require
   [assets]
   [lib]
   [paths]
   [state :refer [clicked-mouse state]]))

(def GEAR-STICK-RADIUS 11)

(def GEAR-BULB-RADIUS 40)

(def GEAR-ROD-HBREADTH 15)
(def GEAR-ROD-HLENGTH 300)

(defn draw-gear [cnv ctx cm?]
  (lib/with-gear-transform ctx cnv
    (fn [ctx _]
      (let [path paths/GEAR-SOCKET-PATH]
        ; (set! (.-fillStyle ctx) "black")
        (set! (.-fillStyle ctx) "rgba(0%, 0%, 0%, 40%)")
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

(defn draw [g cnv ctx]
  ; check if mouse is on gear
  (when-some [cm @clicked-mouse]
    (when-some [[x y] (:now cm)]
      (let [[x y] (lib/ezileamron cnv x y)]
        (when-some [gear (paths/get-gear ctx cnv x y)]
          (set! (.-font ctx) "bold 48px serif")
          (.fillText ctx (name gear) 100 200)))))

  ; draw gear stick
  (when-some [[x y] (:gear-location (:game @state))]
    (lib/with-gear-transform ctx cnv
      (fn [ctx scale]

        ; rod
        (let [angle
              (let
               [mid-x (/ 190 2)
                del-x (cond (= mid-x x) nil
                            :else (abs (- mid-x x)))
                angle (and del-x
                           (*
                            (if (>= mid-x x) -1 1)
                            (js/Math.asin (/ del-x GEAR-ROD-HLENGTH))))
                vert-len (and del-x
                              (js/Math.sqrt (-
                                             (js/Math.pow GEAR-ROD-HLENGTH 2)
                                             (js/Math.pow del-x 2))))
                bottom-y (and vert-len (+ y vert-len))
                top-y (and bottom-y (- bottom-y GEAR-ROD-HLENGTH))]

                (.save ctx)
                (when angle
                  (.translate ctx mid-x bottom-y)
                  (.rotate ctx angle)
                  (.translate ctx (- mid-x) (- bottom-y)))
                (.drawImage ctx assets/ROD
                            (- mid-x GEAR-ROD-HBREADTH) top-y
                            (* 2 GEAR-ROD-HBREADTH) GEAR-ROD-HLENGTH)
                (.restore ctx)
                angle)]

          ; bulb
          ((lib/with-ctx
             (fn [ctx]
               (.translate ctx x y)
               (.rotate ctx (* 0.6 angle))
               (.translate ctx (- x) (- y))
               (.drawImage ctx assets/BULB
                           (- x GEAR-BULB-RADIUS) (- y GEAR-BULB-RADIUS)
                           (* 1.9 GEAR-BULB-RADIUS) (* 2.1 GEAR-BULB-RADIUS)))) ctx))

        (.beginPath ctx)
        (.arc ctx
              x y
              GEAR-STICK-RADIUS
              0 (* 2 js/Math.PI))
        (.closePath ctx)
        (.fill ctx)

        ; ; bulb
        ; (.beginPath ctx)
        ; (set! (.-fillStyle ctx) "red")
        ; (.arc ctx
        ;       x (- y 240)
        ;       (* 4 GEAR-STICK-RADIUS)
        ;       0 (* 2 js/Math.PI))
        ; (.closePath ctx)
        ; (.fill ctx)
        )))

  (draw-gear cnv ctx @clicked-mouse))

(defn update_ [g cnv ctx]

  (when-some [cm @clicked-mouse]
    (when-some [[x y] (:move cm)]
      (swap! clicked-mouse assoc :move nil)

      (let [;[x y] (map #(* % 3.3) [x y])
            [x y] (lib/ezileamron cnv x y)
            scale (lib/calc-scale cnv)
            x' (/ x scale)
            y' (/ y scale)
            [old-draw-x  old-draw-y] (:gear-location g)
            new-x (+ old-draw-x x')
            new-y (+ old-draw-y y')
            inside-path (lib/with-gear-transform ctx cnv
                          (fn [ctx _scale]
                            (let [[x y] (map #(* % scale) [new-x new-y])
                                  [trans-x trans-y] (lib/calc-transform cnv scale)
                                  [x y] [(+ x trans-x) (+ y trans-y)]]

                              (.isPointInPath ctx paths/GEAR-SOCKET-PATH x y))))]

        (when inside-path
          (let [new-game
                  ; (update g :gear-location (fn [[x y]] [(+ x x') (+ y y')]))
                (assoc g :gear-location [new-x new-y])
                  ;
                ]
            (swap! state assoc :game new-game)))))))
