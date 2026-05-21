(ns state)

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

; make sure that (<= 1 lvl 30)
; TODO HERE:
(defn level->libgame [lvl]
  (cond

    (<= 1 lvl 10)  {:show-socket true
                    :reverse false ; if true then the initial string will already be an invalid expr
                    :show-validity-array true
                    :difficulty (level->difficulty lvl)}

    (<= 11 lvl 20)  {:show-socket false
                     :reverse false
                     :show-validity-array true
                     :difficulty (+ 3 (level->difficulty (- lvl 10)))}

    (<= 21 lvl 30) {:show-socket false
                    :reverse true
                    :show-validity-array true
                    :difficulty (+ 6 (level->difficulty (- lvl 20)))}

    (<= 31 lvl 40) {:show-socket false
                    :reverse true
                    :show-validity-array false
                    :difficulty (+ 9 (level->difficulty (- lvl 30)))}))

(defn INIT-GAME []
  {:level 1
   ; :gear-location [95, 60] ; center of gear
   :gear-location [15, 15] ; 1st gear - relative to gear render location in gear-path scaler
   :clock 0.0 ; in ms
   :level-duration 0.0
   :libgame (level->libgame 1)

   :time-start (js/Date.now)
   :time 0})

(def INIT-STATE
  {; game state
   :game nil ; shape is INIT-GAME
   :pressed nil

   :main-menu {:clicked nil} ; or nil

   :user-score nil})

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

