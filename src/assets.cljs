(ns assets)

(defonce BG
  (let [img (new js/Image)]
    (set! (.-src img) "bg.png")
    img))

(defonce BULB
  (let [img (new js/Image)]
    (set! (.-src img) "bulb.png")
    img))

(defonce ROD
  (let [img (new js/Image)]
    (set! (.-src img) "rod.png")
    img))

(defonce MENU
  (let [img (new js/Image)]
    (set! (.-src img) "menu.png")
    img))

(defonce HOW-TO-PLAY
  (let [img (new js/Image)]
    (set! (.-src img) "how-to-play.png")
    img))

(defonce NEXT-LEVEL
  (let [img (new js/Image)]
    (set! (.-src img) "next-level.png")
    img))

(defonce WIN
  (let [img (new js/Image)]
    (set! (.-src img) "win.png")
    img))

(defonce USER-SCORE-BG
  (let [img (new js/Image)]
    (set! (.-src img) "user-score-bg.png")
    img))

(defonce MEME
  (let [img (new js/Image)]
    (set! (.-src img) "meme.png")
    img))

(defonce PARENS
  (let [img (new js/Image)]
    (set! (.-src img) "parens.png")
    img))

(defonce GEAR
  (let [img (new js/Image)]
    (set! (.-src img) "gear.png")
    img))

(defn paren->source-offset [c]
  (case c
    \( 0
    \) 1
    \[ 2
    \] 3
    \{ 4
    \} 5
    (do
      (js/console.error "wut is this:" (clj->js c))
      nil)))

(defonce promise-all-loaded
  (let [promises
        (map
         (fn [img] (new js/Promise #(.addEventListener img "load" %)))
         [BG BULB ROD MENU HOW-TO-PLAY MEME PARENS NEXT-LEVEL GEAR USER-SCORE-BG WIN])]

    (js/Promise.all promises)))
