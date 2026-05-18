(ns assets)

(defonce BG
  (let [img (new js/Image)]
    (set! (.-src img) "bg.png")
    img))

(defn once-all-loaded [f]
  (let [promises
        (map
         (fn [img] (new js/Promise #(.addEventListener img "load" %)))
         [BG])

        promise (js/Promise.all promises)]
    (.then promise f)))
