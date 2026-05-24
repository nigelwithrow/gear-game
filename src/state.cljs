(ns state
  (:require
   [libgame]))

;;
;; constants
;;

; width / height
(def RATIO 1.5)

(def FPS 60)
(def FRAME-DURATION (/ 1000 FPS))

(defn level->difficulty [n] (case n
                              (1 2 3) 1
                              (4 5 6) 2
                              (7 8 9 10) 3))

(def MID-X (/ 190 2))

(def GEAR-BULB-RADIUS 40)

(def GEAR-ROD-HBREADTH 15)
(def GEAR-ROD-HLENGTH 300)

(def LOCAL-STORAGE-KEY "nigelwithrow$parentheses-drive-me-crazy")

; make sure that (<= 1 lvl 30)
; TODO HERE:
(defn level->libgame [lvl]
  (cond

    (<= 1 lvl 10) (let [difficulty (level->difficulty lvl)]
                    {:show-socket true
                     :show-validity-array true
                     :expr (libgame/gen-expr difficulty)
                     :filled difficulty ; half
                    ; :duration (+ (* 1 lvl) 5)
                     :duration nil})

    (<= 11 lvl 20) (let [difficulty (+ 3 (level->difficulty (- lvl 10)))]
                     {:show-socket true
                      :show-validity-array true
                      :expr (libgame/gen-expr difficulty)
                      :filled (quot  (* 2 difficulty) 3) ; 1/3
                      :duration nil})

    (<= 21 lvl 30) (let [difficulty (+ 6 (level->difficulty (- lvl 20)))]
                     {:show-socket true
                      :show-validity-array false
                      :expr (libgame/gen-expr difficulty)
                      :filled (quot  (* 2 difficulty) 3) ; 1/3
                      :duration nil})

    (<= 31 lvl 40) (let [difficulty 9]
                     {:show-socket false
                      :show-validity-array false
                      :expr (libgame/gen-expr difficulty)
                      :filled (quot  (* 2 difficulty) 3) ; 1/3
                      :duration nil})))

(defn num->gear [n]
  (case n 1 :1 2 :2 3 :3 4 :4 5 :5 6 :R))

(defn random-gear []
  (num->gear (libgame/randint 1 5)))

(defn calc-angle [x y]
  (let [del-x (cond (= MID-X x) 0
                    :else (abs (- MID-X x)))
        angle (*
               (if (>= MID-X x) -1 1)
               (js/Math.asin (/ del-x GEAR-ROD-HLENGTH)))
        vert-len (js/Math.sqrt (-
                                (js/Math.pow GEAR-ROD-HLENGTH 2)
                                (js/Math.pow del-x 2)))
        bottom-y (+ y vert-len)
        top-y (- bottom-y GEAR-ROD-HLENGTH)]
    angle))

(defn INIT-GAME
  [lvl times]
  (let [libgame (level->libgame lvl)

        start-expr (apply str (take (:filled libgame) (:expr libgame)))

        remaining-expr (drop (:filled libgame) (:expr libgame))

        parens (if (= (count (:expr libgame)) 1) ; parentheses distributed across all gears

                 (assoc nil (random-gear) (first remaining-expr))

                 (reduce
                  (fn [acc paren]
                    (let [r-gear (random-gear)]
                      (update acc r-gear (fn [on-gear] (conj on-gear paren)))))
                  {}
                  remaining-expr))]
    {:level lvl
     ; relative to gear render location in gear-path scaler
     :gear-location [95, 60, (calc-angle 95 60)] ; center of gear + angle
     :at-gear nil ; or symbol
     ; :gear-location [15, 15] 
     :libgame libgame ; static throughout a level

     :clicked nil ; to know if user just clicked on gear; causing a rotate

     :lvl-time-start (js/Date.now)

     :parens parens

     :expr start-expr

     :times times ; times for all levels - grows
     }))

(def INIT-STATE
  {; game state
   :game nil ; shape is INIT-GAME
   :pressed nil

   :user-score nil ; or <todo>
   :main-menu {:clicked nil} ; or { :clicked [num num] }
   :how-to-play nil ; or { :clicked [num num] }
   :game-over nil ; or { :clicked [num num] }
   :next-level nil ; or {:lvl int :times times :clicked [num num]} -- remember that :lvl 41 is win-screen
   })

;;
;; state and stateful values
;;

;; shape is INIT-STATE
(defonce state (atom INIT-STATE))

(def canvas (atom nil))
(def ctx (atom nil))

(def pressed-keys (atom #{}))

;; shape: nil|{
;;   :start [float float]
;;   :move [float float]
;;   :now [float float]}
(def clicked-mouse (atom nil))

(defonce running (atom true))
(defonce raf-id (atom nil))

(defonce cached-levels (atom nil))

(defn get-continue-level []
  (let [cached-levels @cached-levels
        levels (reduce
                (fn [acc k]
                  (if-some [num (parse-long (name k))]
                    (conj acc num) acc))
                []
                (keys cached-levels))
        levels (sort levels)]
    (inc (last levels))))
