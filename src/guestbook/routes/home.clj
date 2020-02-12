(ns guestbook.routes.home
  (:require [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :refer [ok]]
            ; [schema.core :as schema]
            [guestbook.views.layout :as layout]
   ;;      引入hiccup库
            [hiccup.form :refer [form-to text-field text-area submit-button]]
            [guestbook.models.db :as db]
   ;;      添加会话管理器
            [noir.session :as session]
            [guestbook.services.game-of-life :as game]
            ;; json
            [cheshire.core :as cc-json])
  (:import [java.text SimpleDateFormat]))
(defn format-time
  "日期格式化"
  [timestamp]
  (-> "dd/MM/yyyy"
      (SimpleDateFormat.)
      (.format timestamp)))
;; 首先我们创建一个函数，用了呈现已有的消息。
;; 这个函数会生成一个包含了现有消息的HTML列表。
(defn show-guests
  "消息列表"
  []
  [:ul.guests
   (for [{:keys [message name timestamp]} (db/read-guests)
         #_[{:message "Howdy" :name "Bob" :timestamp nil}
            {:message "Hello" :name "Bob" :timestamp nil}]]
     [:li
      [:blockquote message]
      [:p "-" [:cite name]]
      [:time (format-time timestamp)]])])

;; 修改home函数，使顾客可以看到前面那些顾客留下的消息
;; 并提供一个表单用来创建新的消息
(defn home [& [name message error]]
  (println "启动repl/start后，自动打开浏览器，请求根路径...")
  (layout/common 
   [:div.hell
    [:h1 "GuestBook111" " " (session/get :user)]]  ;; (session/get :user)获取会话中的用户ID；session只能在请求上下文时使用
   [:p "Welcome to my guestbook "]
   [:p error]
   ;; 调用show-guests函数，创建已有的消息列表
   (show-guests)
   [:hr]
   ;; 我们创建一个具有name和message两个字段的表单
   ;; 当表单发送到服务器时，它们将会以同样的名字发送
   (form-to [:post "/"]
            [:p "Name:"]
            (text-field "name" name)
            [:p "Message:"]
            (text-area {:rows 10 :cols 40} "message" message)
            [:br]
            (submit-button "comment"))
   (println "home结束了")
   #_[:form
      [:p "Name:"]
      [:input]
      [:p "Message:"]
      [:textarea {:rows 10 :cols 40}]]))

(defn save-message
  "保存消息"
  [name message]
  (cond
    (empty? name)
    (home name message "Some dummy forgot to leave a name")
    (empty? message)
    (home name message "Don't you have something to say?")
    :else
    (do (println name message 2222)
        (db/save-message name message)
        (home name message))))

(defn cros-test
  []
  (println "cros-test")
  (cc-json/generate-string {:a 1 :b 2 :c {:d 4 :e 5}})
  #_{:a 1 :b 2 :c {:d 4 :e 5}})

(defn parse-json
  [req]
  (let [body (:body req)
        _ (println "-----------" (type body))]
    (str "hello cross!")))

(defn vue-test [a b]
  (println  a "***vue test ***" b)
  {:a 12})
;; 使用defroutes来定义guestbook.routes.home命名空间中的路由
;; 每个路由都代表着一个应用会响应的URI地址。
;; 路由的起始位置是HTTP请求的类型，如GET、POST
;; 然后是参数和主体部分
(defroutes home-routes
  (GET "/" [] (home))
  (GET "/vue-test" [a b] (vue-test a b))
  
  (POST "/cros-test" []
    (println "请求到了cros-test...")
    (ok (cros-test)))
  
  (POST "/parse-json" request
    (println "请求到了parse-json...")
    (ok (parse-json request)))
  
  (POST "/" [name message] 
    (println "请求到了/...")
    (save-message name message))
  (GET "/game" [char0 char1 board] (game/calculate-next char0 char1 board))
  (POST "/echart-game" [char1 board] (game/echart-format board char1))
  (GET "/echart-format-json" [char1 board] (game/echart-format-json board char1))
  #_(POST "/echart-format-json" []
      :body [params {s/Keyword s/Any}]
      (ok (game/echart-format-json)))
  
  (context "/compojure" []
    ;; clojure原生方式
    (GET "/get/:foo" {{foo :foo} :param}
      (ok (str "接收到了foo：" foo)))
    ;; compojure提供的解构功能
    (GET "/get2/:foo" [foo]
      (println "请求到了foo...")
      (ok (str "接收到了foo：" foo))))
  )