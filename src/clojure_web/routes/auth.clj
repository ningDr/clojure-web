(ns clojure-web.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [clojure-web.views.layout :as layout]
            [clojure-web.models.db :as db]
            [hiccup.form :refer [form-to label text-field password-field submit-button]]
   ;;      添加重定向
   ;;      Ring在ring.util.response命名空间中提供了重定向的功能
   ;;      启用了lib-noir，使用noir.response/redirect替换
   ;;      lib-noir允许使用操作关键字表达重定向状态码
            [noir.response :refer [redirect]]
   ;;      增加会话管理器依赖
            [noir.session :as session]
   ;;      增加处理验证方法，同时需要将handler封装到wrap-noir-validation中间件
            [noir.validation :refer [rule errors? has-value? on-error]]
   ;;      添加加密功能
            [noir.util.crypt :as crypt]))
(declare format-error control)

;; ****************注册开始****************
;; 注册页面
(defn registration-page
  "这个函数用于为我们呈现页面，并会展示一个表单给用户，用于引导用户输入ID和密码"
  []
  (layout/common
   (form-to [:post "/register"]
            ;(label "id" "screen name")
            ;(text-field "id")
            ;[:br]
            ;(label "pass" "Password")
            ;(password-field "pass")
            ;[:br]
            ;(label "pass1" "Retype password")
            ;(password-field "pass1")
            ;[:br]
            ;; 简化
            ;(control text-field "id" "Screen name")
            ;(control password-field "pass" "Password")
            ;(control password-field "pass1" "Retype password")
            ;(submit-button "Create Account")
            (control text-field :id "screen name")
            (control password-field :pass "Password")
            (control password-field :pass1 "Retype password")
            (submit-button "login"))))

;; 处理用户注册
(defn handle-registration
  "验证新注册的用户"
  [id pass pass1]
  (println id pass pass1)
  (rule (= pass pass1)
        [:pass "password was not retyped correctly"])
  ;; 自己增加，原书没有
  (rule (empty? (db/get-user id))
        [:id "please change a id for register"])
  (if (errors? :pass :id)
    (registration-page)
    (do
      (println (crypt/encrypt pass))
      (db/add-user-record {:id id :pass (crypt/encrypt pass)}) ;; 不加盐加密密码
      (redirect "/login"))))
;; ****************注册结束****************

(defn control
  "提取公共元素，抽象并构造一个辅助函数"
  [field name text]
  ;; 平时我们会用一个vector来直接表述，但这次创建的函数使用list函数包装
  ;; 这是因为Hiccup使用vector来表达HTML标签
  ;; 但是标签内容并不能用vector来表达
  ;(list (label name text)
  ;      (field name)
  ;      [:br]
  ;      )
  ;; 更新control函数，在调用on-error时，传入控制名
  ;; 这便实现了错误汇聚
  ;; 对提供的键名使用format-error格式化
  (list (on-error name format-error)
        (println (on-error name format-error))
        (label name text)
        (field name)
        [:br]))

(defn format-error
  "创建一个辅助函数，将错误进行统一处理"
  [[error]]
  [:p.error error])

;; ****************登录开始****************
(defn login-page
  "
    修改handler增加会话管理器后，我们看到的内容涉及创建登录页面并将用户添加到会话
    此函数创建一个包含用户ID和密码的登录表单，并使用通用布局封装
    当用户点击提交按钮，表单会将一个HTTP发送给“/login” URI
  "
  ;[& [error]]
  []
  (layout/common
   ;(if error [:div.error "Login error: " error])
   ;(form-to
   ;  [:post "/login"]
   ;  (control text-field :id "screen name")
   ;  (control password-field :pass "Password")
   ;  (submit-button "login")
   ;  )
   ;; 出错由control处理了，故该函数可以简化如下 [& [error]]改成无参函数
   (form-to [:post "/login"]
            (control text-field :id "screen name")
            (control password-field :pass "Password")
            (submit-button "login"))))

(defn handle-login
  "登录验证"
  [id pass]
  ;(cond
  ;  (empty? id)
  ;  (login-page "Screen name is required")
  ;  (empty? pass)
  ;  (login-page "Password is required")
  ;  (and (= "foo" id) (= "bar" pass))
  ;  (do
  ;    (session/put! :user id)
  ;    (redirect "/")
  ;    )
  ;  :else
  ;  (login-page "authentication failed")
  ;  )
  ;; 使用noir.validation/rule辅助函数，替换cond来实现决策
  ;; 每个规则都对内容判定，检查各自是否能通过
  ;; 最后，函数调用noir.validation/errors?去检查规则中是否产生错误
  ;; 如果有，我们就显示登录页面
  ;; 否则我们将用户记录到会话，并重定向到home页面
  ;(rule (has-value? id)
  ;      [:id "Screen name is required"]
  ;      )
  ;(rule (= id "foo")
  ;      [:id "unknown user"]
  ;      )
  ;(rule (has-value? pass)
  ;      [:pass "Password is required"]
  ;      )
  ;(rule (= pass "bar")
  ;      [:pass "invalid password"]
  ;      )
  ;(if (errors? :id :pass)
  ;  ;; 出错进入登录页
  ;  (login-page)
  ;  ;; 正常重定向到首页
  ;  (do
  ;    (session/put! :user id)
  ;    (redirect "/")
  ;    )
  ;  )
  (let [user (db/get-user id)]
    (rule (has-value? id)
          [:id "Screen name is required"])
    ;; 自己增加，原书没有
    (rule ((complement empty?) user)
          [:id "ID hasn't registered"])
    (rule (has-value? pass)
          [:pass "Password is required"])
    ;; 验证密码，使用crypt/compare函数去比对此时提供的密码和其在注册中创建的哈希版本
    (rule (and user (crypt/compare pass (:pass user)))
          [:pass "Invalid password"])
    (if (errors? :id :pass)
      ;; 出错进入登录页
      (login-page)
      ;; 正常重定向到首页
      (do
        (session/put! :user id)
        (redirect "/")))))
;; ****************登录结束****************

;; 增加了一个页面，需要增加一个对应路由
;; 定义的新路由需要在handler中引用，同时需要添加路由
;; 函数形参vector中使用下划线，用在被执行的函数不使用此参数时
(defroutes auth-routes
           (GET "/register" [_] (registration-page))
           ;; 添加POST请求处理
           (POST "/register" [id pass pass1]
             ;; 增加注册验证
             (handle-registration id pass pass1))
           ;; GET login简单调用login-page函数显示页面
           ;; 在重定向到home页面之前
           ;; POST login路由使用noir.session/put!函数和:user键将用户添加到会话
           (GET "/login" [] (login-page))
           (POST "/login" [id pass]
             ;(session/put! :user id)
             ;(redirect "/")
             (handle-login id pass)))
