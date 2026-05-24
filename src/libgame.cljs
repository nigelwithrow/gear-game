(ns libgame)

(defn random [] (js/Math.random))

(defn randint [min max]
  (let [r (random)]
    (+ (js/Math.floor (* r (+ (- max min) 1))) min)))

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

; returns [validity-array whether-gone-bad]
; "([]{)}" -> validity-array: 000011
(defn expr-validity [s]
  (let [stack
        #js []

        [validity bad]
        (reduce
         (fn [[arr bad] c]

           (if bad
             [(conj arr 1) true]
             (if-some [validity
                       (case c
                         (\( \[ \{)
                         (do
                           (.push stack c)
                           0)

                         (\) \] \})
                         (let [last (last stack)]
                           (.pop stack)
                           (cond
                             (undefined? last) nil
                             (and (= last \() (= c \))) 0
                             (and (= last \[) (= c \])) 0
                             (and (= last \{) (= c \})) 0
                             :else nil)))]

               ; then
               [(conj arr validity) false]
               ; else
               [(conj arr 1) true])))
         [[] ; validity array
          false] ; whether gone bad
         s)]

    [validity bad]))

(defn rotate-parens [parens]
  (reduce-kv
   (fn [obj gear-name gear-parens]
     (assoc obj
            gear-name
            (if-some [_ gear-parens]
              (conj (vec (rest gear-parens)) (first gear-parens))
              nil)))
   {}
   parens))

; (set! (.-exprValidity js/window) (fn [s] (clj->js (expr-validity s))))
