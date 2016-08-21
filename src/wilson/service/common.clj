(ns wilson.service.common)

(defn tf->10
  "Maps true/false to 1/0"
  [e]
  (if (true? e) 1 0))
