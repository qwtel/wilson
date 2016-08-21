(ns wilson.handler.common)

(defn update-body-if-success [{:keys [status body] :as res} f]
  (if (or (< status 200) (>= status 300))
    res
    (update res :body f)))
