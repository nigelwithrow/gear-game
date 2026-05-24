(ns game
  (:require
   [assets]
   [lib]
   [libgame]
   [paths]
   [state :refer [clicked-mouse GEAR-BULB-RADIUS GEAR-ROD-HBREADTH
                  GEAR-ROD-HLENGTH MID-X state]]))

(defn draw-gear [cnv ctx cm?]
  (lib/with-gear-transform ctx cnv
    (fn [ctx _]
      (let [path paths/GEAR-SOCKET-PATH]
        ; (set! (.-fillStyle ctx) "black")
        (set! (.-fillStyle ctx) "rgba(0%, 0%, 0%, 40%)")
        (when-let [cm cm?]
          (let [[x y] (:start cm)
                [x y] (lib/ezileamron cnv x y)]
            ; (when (.isPointInPath ctx path x y)
            ;   (set! (.-fillStyle ctx) "red"))
            ))
        (.fill ctx path)

        ; (set! (.-fillStyle ctx) "green")
        ; (doseq [s (vals paths/GEAR_AREAS)]
        ;   (.fill ctx (new js/Path2D s)))
        ))))

(defn draw [g cnv ctx]
  (set! (.-fillStyle ctx) "white")
  ; (let [fmt-time (str (quot (:time g) 1000) "." (mod (:time g) 1000))]
  ;   (.fillText ctx fmt-time 100 200))

  ; draw gear stick
  (let [[x y angle] (:gear-location g)]
    (lib/with-gear-transform ctx cnv
      (fn [ctx _scale]

        ; rod
        (let
         [del-x (cond (= MID-X x) 0
                      :else (abs (- MID-X x)))
          vert-len (js/Math.sqrt (-
                                  (js/Math.pow GEAR-ROD-HLENGTH 2)
                                  (js/Math.pow del-x 2)))
          bottom-y (+ y vert-len)
          top-y (- bottom-y GEAR-ROD-HLENGTH)]

          (.save ctx)
          (when angle
            (.translate ctx MID-X bottom-y)
            (.rotate ctx angle)
            (.translate ctx (- MID-X) (- bottom-y)))
          (.drawImage ctx assets/ROD
                      (- MID-X GEAR-ROD-HBREADTH) top-y
                      (* 2 GEAR-ROD-HBREADTH) GEAR-ROD-HLENGTH)
          (.restore ctx)

          ; bulb
          (lib/with-ctx
            ctx
            (fn [ctx]
              (.translate ctx x y)
              (.rotate ctx (* 0.6 angle))
              (.translate ctx (- x) (- y))
              (.drawImage ctx assets/BULB
                          (- x GEAR-BULB-RADIUS) (- y GEAR-BULB-RADIUS)
                          (* 1.9 GEAR-BULB-RADIUS) (* 2.1 GEAR-BULB-RADIUS)))))

        (.beginPath ctx)
        (.arc ctx
              x y
              paths/GEAR-STICK-RADIUS
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
        ))

    ; validity array
    (let [height (/ (.-height cnv) 30.0)
          mid-x (/ (.-width cnv) 3)
          y (* (.-height cnv) 0.2875)
          char-width (/ height 2)
          width (* char-width (count (:expr g)))
          x (- mid-x (/ width 2.0))]

      (lib/with-ctx
        ctx
        (fn [ctx]
          (.translate ctx x y)
          (.scale ctx char-width height)

          (doall (map
                  (fn [i c valid]
                    (let [sx (assets/paren->source-offset c)]
                      ; draw parentheses
                      (.drawImage ctx assets/PARENS
                                  (* sx 32) 0 32 64 ; source
                                  i 0 1 1; dest
                                  )

                      ; draw validity underline
                      (let [pad 0.1]
                        (set! (.-fillStyle ctx)
                              (case valid
                                0 "white"
                                1 "red"))

                        (.fillRect ctx
                                   i ; x
                                   1.5 ; y
                                   (- 1 (* 2 pad)) ; w
                                   0.3 ; h
                                   ))))

                  (range)
                  (:expr g)
                  (get (libgame/expr-validity (:expr g)) 0))))))

    (lib/with-ctx ctx
      (fn [ctx _scale]
        (let [w (* (.-width cnv) 0.14074)
              h (/ (* (.-height cnv) 2) 15)
              ; x (* (.-width cnv) 0.47315)
              x (/ (- (.-width cnv) w) 2)
              ; y (* (.-height cnv) 0.43472)
              y (/ (- (.-height cnv) h) 2)]

          (.translate ctx x y)
          (.scale ctx
                  (/ w 19)
                  (/ h 12))

          ; gear for parens
          (.drawImage ctx assets/GEAR 0 0 19 12)

          (let [x-pad (* 0.3 3)
                y-pad (* 0.3 6)
                line-height (+ (- 6 (* 2 y-pad)) (* 0.1 6))]

            ; parens corresp to gear
            (reduce-kv
             (fn [_ k v]
               (let [[f x y] (case k
                               :1 [-1 x-pad (- y-pad 6)]
                               :3 [-1 (+ x-pad 8) (- y-pad 6)]
                               :5 [-1 (+ x-pad 16) (- y-pad 6)]

                               :2 [+1 x-pad (+ 12 y-pad)]
                               :4 [+1 (+ x-pad 8) (+ 12 y-pad)])]

                 (doall (map
                         (fn [i paren]
                           (let [sx (assets/paren->source-offset paren)]
                             (.save ctx)
                             (when (> i 0)
                               (let [opacity (/ 1.0 (+ 1 (* 0.6 i)))]
                                 (set! (.-filter ctx) (str "opacity(" opacity ")"))))
                             (.drawImage ctx assets/PARENS
                                         (* sx 32) ; source
                                         0
                                         32
                                         64

                                         x ; dest
                                         (+ y (* i f line-height))
                                         (- 3 (* 2 x-pad))
                                         (- 6 (* 2 y-pad)))
                             (.restore ctx)))
                         (range) v))
                 ()))
             nil
             (:parens g)))))))

  (draw-gear cnv ctx @clicked-mouse))

(defn update_ [g cnv ctx]

  (let [scale (lib/calc-scale cnv)
        [gear-x gear-y] (:gear-location g)

        ; update gear position
        g
        (if-some [cm @clicked-mouse]
          (let [g
                (assoc g :clicked (when-not (:now cm)
                                    (:start cm)))]

            (if-some [[x y] (:move cm)]
              (do
                (swap! clicked-mouse assoc :move nil)

                (let [;[x y] (map #(* % 3.3) [x y])
                      [x y] (lib/ezileamron cnv x y)
                      x' (/ x scale)
                      y' (/ y scale)
                      new-x (+ gear-x x')
                      new-y (+ gear-y y')

                      inside-path
                      (lib/with-gear-transform ctx cnv
                        (fn [ctx _scale]
                          (let [[x y] (map #(* % scale) [new-x new-y])
                                [trans-x trans-y] (lib/calc-transform cnv scale)
                                [x y] [(+ x trans-x) (+ y trans-y)]]

                            (and (.isPointInPath ctx paths/GEAR-SOCKET-PATH x y)
                                 (every?
                                  (fn [[off-x off-y]]
                                    (.isPointInPath ctx paths/GEAR-SOCKET-PATH (+ x off-x) (+ y off-y)))
                                  paths/INSCRIBED-OCTAGON)))))]

                  ; (when-some [gear inside-gear]
                  ;   (js/console.log (name gear)))

                  (if inside-path
                    (assoc g :gear-location [new-x
                                             new-y
                                             (state/calc-angle new-x new-y)])
                    g)))
              g))

          (if-some [[x y] (:clicked g)]

            (let [[x y] (lib/ezileamron cnv x y)
                  is-click-on-gear-buld
                  (lib/with-gear-transform ctx cnv (fn [ctx _scale]
                                                     (.beginPath ctx)
                                                     (.arc ctx
                                                           gear-x gear-y
                                                           paths/GEAR-STICK-RADIUS
                                                           0 (* 2 js/Math.PI))
                                                     (.closePath ctx)
                                                     (.isPointInPath ctx x y)))]
              (if is-click-on-gear-buld ; in-place click on gear --> rotate

                (do
                  (assoc g
                         :clicked nil
                         :parens (libgame/rotate-parens (:parens g))))

                g))

            g))

        ; update parens played
        g
        (let [[new-x new-y] (:gear-location g)
              old-parens (:parens g)
              old-expr (:expr g)

              at-gear
              (lib/with-gear-transform ctx cnv
                (fn [ctx _scale]
                  (let [[x y] (map #(* % scale) [new-x new-y])
                        [trans-x trans-y] (lib/calc-transform cnv scale)
                        [x y] [(+ x trans-x) (+ y trans-y)]]

                    (paths/get-gear ctx x y))))

              [new-parens new-expr]
              (if (and (not (:at-gear g)) at-gear) ; entered new gear
                (if-some [parens-on-this-gear (at-gear old-parens)]

                  (let [paren (first parens-on-this-gear)]
                    [(assoc old-parens
                            at-gear
                            (if (= (count parens-on-this-gear) 1)
                              nil
                              (vec (rest parens-on-this-gear))))

                     (str (:expr g) paren)])

                  (if (= at-gear :R)

                    (if-some [popped-paren (last old-expr)]

                      (let [new-parens (update old-parens (state/random-gear) conj popped-paren)
                            new-expr (subs old-expr 0 (dec (count old-expr)))]
                        [new-parens new-expr])

                      [old-parens old-expr])

                    [old-parens old-expr]))

                [old-parens old-expr])]

          (assoc g
                 :expr new-expr
                 :parens new-parens
                 :at-gear at-gear))

        ; update whether level lost or won or neither
        g
        (let [now (- (js/Date.now) (:lvl-time-start g))
              [_ expr-bad] (libgame/expr-validity (:expr g))]

          (if (and (:duration (:libgame g)) ; duration is nil => infinite time to finish lvl
                   (> now (:duration (:libgame g))))

            (if (or expr-bad (not (= (count (:expr g)) (count (:expr (:libgame g))))))
              :fail ; game over
              (assoc g :next true) ; next level - pause screen
              )

            (if (and
                 (not expr-bad)
                 (= (count (:expr g)) (count (:expr (:libgame g)))))

              (assoc g :next true) ; next level - pause screen

              g)))]

    (cond
      (= :fail g) (swap! state assoc :game nil :game-over true)
      (:next g) (let [new-times (:times g)
                      new-times
                      (assoc new-times
                             (keyword (str (:level g)))
                             (- (js/Date.now) (:lvl-time-start g)))]

                  (js/localStorage.setItem state/LOCAL-STORAGE-KEY
                                           (js/JSON.stringify (clj->js new-times)))
                  (swap! state assoc :game nil :next-level {:lvl (inc (:level g))
                                                            :times new-times}))

      :else (swap! state assoc :game g))))
