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

(defonce promise-all-loaded
  (let [promises
        (map
         (fn [img] (new js/Promise #(.addEventListener img "load" %)))
         [BG BULB ROD])]

    (js/Promise.all promises)))
