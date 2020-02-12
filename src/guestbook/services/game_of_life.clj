(ns guestbook.services.game-of-life
  (:require [cheshire.core :as cc-json]))

(defn transfer-board
  "doc: 获得一个board，转为01数字
  author: ning.dr@foxmail.com
  date: 2019/9/12 21:31"
  [char board]
  (mapv #(mapv (fn [i] (if (= i char) 1 0)) (seq %)) board))

(defn get-live-cell
  "doc: 计算一个cell周围的存活细胞数，以直角坐标系定位
  author: ning.dr@foxmail.com
  date: 2019/9/12 21:36"
  [board x y]
  (let [a (nth board (- y 1))
        b (nth board y)
        c (nth board (+ y 1))
        v (- x 1)
        z (+ x 1)]
    (+ (nth a v) (nth b v) (nth c v)
       (nth a z) (nth b z) (nth c z)
       (nth a x) (nth c x))))

(defn get-cell-map
  "doc: 得到当前棋盘的cell与周围细胞存活数的map形式
  author: ning.dr@foxmail.com
  date: 2019/9/12 21:45"
  [trans-board]
  (let [len (count (first trans-board))]
    (for [x (range 1 (- len 1))]
      (for [y (range 1 (- len 1))]
        {:x (nth (nth trans-board x) y)
         :f (get-live-cell trans-board y x)}))))

(defn get-next-board
  "doc: 根据cell的map形式，计算下一次的棋盘01形式
  author: ning.dr@foxmail.com
  date: 2019/9/12 21:50"
  [cell-map]
  (let [len (+ 2 (count (first cell-map)))
        next-cell (mapv #(conj (reduce (fn [m n] (conj m (cond
                                                           (> 2 (:f n)) 0
                                                           (and (= 3 (:f n)) (zero? (:x n))) 1
                                                           (> 4 (:f n)) (:x n)
                                                           (> 3 (:f n)) 0
                                                           :else 0))) [0] %) 0) cell-map)]
    (as-> (reduce #(conj %1 %2) [] (take len (repeat 0)))
          $
          (conj (reduce #(conj %1 %2) [$] next-cell) $))))

(defn get-result
  "doc: 将计算结果，转为要求形式
  author: ning.dr@foxmail.com
  date: 2019/9/12 21:59"
  [char0 char1 next-board]
  (mapv #(reduce (fn [x y] (str x (if (= y 1) char1 char0))) "" %) next-board))

(defn calculate-next
  "doc: 输入一个棋盘，计算下一个棋盘
  author: ning.dr@foxmail.com
  date: 2019/9/12 22:01"
  [board char0 char1]
  (let [trans (transfer-board char1 board)
        cell-map (get-cell-map trans)
        next-board (get-next-board cell-map)]
    (get-result char0 char1 next-board)))

(defn echart-format
  "doc: 得到echats形式的结果，格式 [[x y cell] [x y cell]]
  author: ning.dr@foxmail.com
  date: 2019/9/12 22:12"
  [board char1]
  (println "board=" board ";\n type(board)= " (type board) "char1=" char1)
  (let [trans (transfer-board char1 board)
        cell-map (get-cell-map trans)
        next-board (get-next-board cell-map)
        x-y-cell (for [x (range 0 5)]
                   (for [y (range 0 5)]
                     (vector x y (nth (nth next-board x) y))))]
    (cc-json/generate-string {:data (reduce #(concat %1 %2) [] x-y-cell)})))

(defn echart-format-json
  "doc: 得到echats形式的结果，格式 [[x y cell] [x y cell]]
  author: ning.dr@foxmail.com
  date: 2019/9/12 22:12"
  [board char1]
  (println "**echart-format-json**" board char1)
  (let [aa (cc-json/encode board)
        bb ["  *  " " *  * "]
        encode-bb (cc-json/encode bb)]
    (println "cheshire.core解析字符串数组结果：" aa)
    (println "类型为：" (type (cc-json/decode aa)))
    (println "==============")
    (println "bb编码：" encode-bb)
    (println "bb编码后再解码：" (cc-json/decode encode-bb)))
  #_(cc-json/encode (echart-format board char1)))