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

(defonce MEME
  (let [img (new js/Image)]
    (set! (.-src img) "meme.png")
    img))

(defonce promise-all-loaded
  (let [promises
        (map
         (fn [img] (new js/Promise #(.addEventListener img "load" %)))
         [BG BULB ROD MENU HOW-TO-PLAY MEME])]

    (js/Promise.all promises)))
