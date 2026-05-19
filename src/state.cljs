(ns state)

;;
;; constants
;;

; width / height
(def RATIO 1.5)

(def FPS 60)
(def FRAME-DURATION (/ 1000 FPS))

(def INIT-GAME
  {:level 1
   ; :gear-location [95, 60] ; center of gear
   :gear-location [15, 15] ; 1st gear - relative to gear render location in gear-path scaler
   })

(def INIT-STATE
  {; game state
   :game INIT-GAME ; type is game/INIT-GAME
   :pressed nil

   ; show credits screen
   :credits true})

;;
;; state and stateful values
;;

(defonce state (atom INIT-STATE))

(def canvas (atom nil))
(def ctx (atom nil))

(def pressed-keys (atom #{}))
(def clicked-mouse (atom nil))

