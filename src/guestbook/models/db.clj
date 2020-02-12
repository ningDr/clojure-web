(ns guestbook.models.db
  (:require [clojure.java.jdbc :as sql])
    ;;      导入java类需要使用:import
  (:import java.sql.DriverManager
           [java.util Date]))

;; 创建数据库连接定义
;; 是一个简单的map，包含了JDBC驱动的类型、协议、以及SQLite数据库的文件名
(def db {:class-name "org.sqlite.JDBC"
         :subprotocol "sqlite"
         :subname "db.sq3"})

;; 创建访客留言的数据表
(defn create-guest-book-table
  "创建表"
  []
  ;; sql/with-connection : 创建连接后自动关闭
  (sql/with-connection
    db
    (sql/create-table
      ;; 使用关键字定义表明
      :guestbook
      [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
      [:timestamp "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"]
      [:name "TEXT"]
      [:message "TEXT"])
    (sql/do-commands "CREATE INDEX timestamp_index ON guestbook (timestamp)")))

(defn read-guests
  "读取数据库记录"
  []
  (sql/with-connection
    db
    ;; sql/with-query-results : 执行查询，并返回结果
    (sql/with-query-results res
      ["SELECT * FROM guestbook ORDER BY timestamp DESC"]
      ;; 调用doall，因为res是惰性的，不会把所有结果都加载到内存中
      ;; 通过调用doall，我们强制对res进行了完全求值
      ;; 如果不这么做，一旦离开了函数的作用范围，我们的数据库连接就会关闭，在函数外就无法访问结果数据
      (doall res))))

(defn save-message
  "保存消息到数据库"
  [name message]
  (sql/with-connection
   db
   (sql/insert-values
    :guestbook
    [:name :message :timestamp]
    [name message (new Date)])))

;; 创建用户表
(defn create-user-table
  "保存用户注册数据"
  []
  (sql/with-connection
    db
    (sql/create-table
      :users
      [:id "VARCHAR(20) PRIMARY KEY"]
      [:pass "VARCHAR(100)"])))

;; 新增用户
(defn add-user-record
  "新用户注册添加到数据库"
  [user]
  (sql/with-connection
    db
    (sql/insert-record :users user)))

;; 查询用户
(defn get-user
  "根据用户登录账号查询用户"
  [id]
  (sql/with-connection
    db
    (sql/with-query-results
      res ["SELECT * FROM users WHERE id = ?" id]
      (first res))))