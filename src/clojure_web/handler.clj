(ns clojure-web.handler
  (:require [compojure.core :refer [defroutes routes]]
            ; [ring.middleware.resource :refer [wrap-resource]]
            ; [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.cors :refer [wrap-cors]]
   ;;  [ring.middleware.session :as session]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure-web.routes.home :refer [home-routes]]
   ;;      添加SQLite数据库
            [clojure-web.models.db :as db]
   ;;      添加注册页
            [clojure-web.routes.auth :refer [auth-routes]]
   ;;      添加会话管理器
            [noir.session :as noir-session]
   ;;      会话存储处理，代替Redis
            [ring.middleware.session.memory :refer [memory-store]]
   ;;      添加验证组件
            [noir.validation :refer [wrap-noir-validation]])
  (:import [java.io File]))

(defn init []
  (println "clojure-web is starting")
  (if-not (.exists (File. "./db.sq3"))
    (db/create-guest-book-table)
    (println "db.sq3库已创建")))

(defn destroy []
  (println "clojure-web is shutting down"))

(defroutes app-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(def app
  (-> (routes auth-routes home-routes app-routes)
      (handler/site)                                        ;; 用于生成Ring handler
      (noir-session/wrap-noir-session
       {:store (memory-store)})
      (wrap-base-url)
      ;; 登录验证
      (noir-session/wrap-noir-session
       {:store (memory-store)})
      (wrap-noir-validation)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))