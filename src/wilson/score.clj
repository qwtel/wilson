(ns wilson.score
  (:require [infix.macros :refer [infix] :rename {infix §}]))

;; ============================================================================
;; Binary Scores
;; ============================================================================

;; ----------------------------------------------------------------------------
;; How Not To Sort By Average Rating
;; http://www.evanmiller.org/how-not-to-sort-by-average-rating.html
;; ----------------------------------------------------------------------------

(defn score [pos n]
  (§ 2 * pos - n))

(defn not-average [pos n & {:keys [confidence] :or {confidence 0.95}}]
  (if (< n 1)
    0
    ;; TODO: statistics library
    (let [z    1.95996397158435
          phat (double (/ pos n))]
      (§ (phat + z * z / (2 * n)
               - z * sqrt((phat * (1 - phat) + z * z / (4 * n)) / n))
         / (1 + z * z / n)))))

; (not-average 6 6)
; (ci-lower-bound 5 10)
;
; (def pos 5)
; (def n 10)
; (def phat (double (/ pos n)))
; (def z 1.95996397158435)
;
; (§ (phat + z * z / (2 * n) - z * sqrt((phat * (1 - phat) + z * z / (4 * n)) / n) / (1 + z * z / n)))

;; ============================================================================
;; Star Ratings
;; ============================================================================

;; ----------------------------------------------------------------------------
;; Ranking Items With Star Ratings
;; http://www.evanmiller.org/ranking-items-with-star-ratings.html
;; ----------------------------------------------------------------------------

(defn- sum-over [K f]
  (->> (range K)
    (map f)
    (reduce +)))

(defn star-rating [n s & {:keys [confidence] :or {confidence 0.95}}]
  ;; TODO: statistics library
  (let [z 1.95996397158435
        N (reduce + n)
        K (count s)
        s1 (sum-over K
             (fn [k] (let [n_k (n k)
                           s_k (s k)]
                       (§ s_k * (n_k + 1) / (N + K)))))
        s2 (sum-over K
             (fn [k] (let [n_k (n k)
                           s_k (s k)]
                       (§ s_k ** 2 * (n_k + 1) / (N + K)))))]
    (§ s1 - z * sqrt((s2 - s1 ** 2) / (N + K + 1)))))

; (star-rating [1 1 1 1 1] [1 2 3 4 5])
; (star-rating [10 10 10 10 10] [1 2 3 4 5])
; (star-rating [0 0 0 0 100] [1 2 3 4 5])
; (star-rating [0 0 0 0 1000] [1 2 3 4 5])
; (star-rating [0 0 0 0 10000] [1 2 3 4 5])
