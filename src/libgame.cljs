(ns libgame)

(defn random [] (js/Math.random))

; (defn random [] (Math/random))

; (defn gen-expr
;   ([difficulty] (gen-expr difficulty 1))
;   ([difficulty depth]
;    (let [r (random)]

;      (do
;        (println depth (pow r (/ (+ 0 depth) difficulty)))
;        (cond
;          (> (pow r (/ (* 3 depth) difficulty)) 0.5)
;          (str "["
;               (gen-expr difficulty (* 2 depth))
;               (gen-expr difficulty (* 2 depth))
;               (gen-expr difficulty (* 2 depth))
;               "]")

;          (> (pow r (/ (* 2 depth) difficulty)) 0.5)
;          (str "{"
;               (gen-expr difficulty (* 2 depth))
;               (gen-expr difficulty (* 2 depth))
;               "}")

;          (> (pow r (/ (* 1 depth) difficulty)) 0.5)
;          (str "("
;               (gen-expr difficulty (* 2 depth))
;               ")")

;          :else "()")))))

(defn rbraces []
  (let [r (random)]
    (cond
      (> r 0.66) "()"
      (> r 0.33) "[]"
      :else "{}")))

(defn gen-expr [difficulty]
  (let [r (random)]

    (cond
      (= difficulty 1) (rbraces)

      (= difficulty 2) (let [[a b] (rbraces)] (str a (rbraces) b))

      (= difficulty 3) (let [[a b] (rbraces)]
                         (cond
                           (> r 0.5) (str a (rbraces) (rbraces) b)
                           :else (str a (gen-expr 2) b)))

      :else (let [[a b] (rbraces)
                  q (int (/ difficulty 3))
                  r' (mod difficulty 3)
                  [x y z] (cond (= r' 0) [q q q] (= r' 1) [q q (inc q)] (= r' 2) [q (inc q) (inc q)])]
              (str
               a
               (cond
                 (> r 0.75) (str (gen-expr x) (gen-expr y) (gen-expr (dec z)))
                 (> r 0.5) (str (gen-expr (+ x y)) (gen-expr (dec z)))
                 (> r 0.25) (str (gen-expr x) (gen-expr (+ y (dec z))))
                 :else (str (gen-expr (+ x y (dec z)))))
               b)))))

(defn new-game [reverse difficulty]
  (let [s (gen-expr difficulty)]
    (if (not reverse)

      ()

      ())))
