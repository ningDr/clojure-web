(ns guestbook.models.clojureCond)
(defn clojureCond
  "cond vs switch case "
  [arg]
  (cond
    (= arg 5)
    (println "arg equal 5")
    (= arg 10)
    (println "arg equal 10")
    :else
    (println "arg is not known")))