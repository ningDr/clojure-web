(ns clojure-web.services.case-when
  (:require [clojure.string :as cstr]))

(def sql-list [{:a "a" :b "b" :c 1}
               {:a "a" :b "b" :c 2}
               {:a "a" :b "b" :c 1}
               {:a "aa" :b "bb" :c 1}
               {:a "aa" :b "bb" :c 3}])
(def static-keys [:a :b])
(def k :c)
(def new-ks [:1 :2 :3])
(def dic-vs [1 2 3])
(defn count-val [col, k, v] (loop [c col, i 0]
                              (if (empty? c)
                                i
                                (recur (rest c) (if (= (k (first c)) v) (inc i) i)))))

(defn case-when [static-keys k new-ks dic-vs sql-list]
  (let [grouped (vals (group-by #(hash (cstr/join (vals (select-keys % static-keys)))) sql-list))]
    (reduce
     (fn [m n]
       (conj m (reduce
                (fn [x y]
                  (merge x (select-keys y static-keys)
                         (loop [ks new-ks, vs dic-vs, res {}]
                           (if (empty? ks)
                             res
                             (recur
                              (rest ks)
                              (rest vs)
                              (merge res {(first ks) (count-val n k (first vs))})))))) {} n))) [] grouped)))

(case-when static-keys k new-ks dic-vs sql-list)