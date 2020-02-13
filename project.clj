;; 增加以使用aliyun
(require 'cemerick.pomegranate.aether)
(cemerick.pomegranate.aether/register-wagon-factory!
 "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))

(defproject clojure-web "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [ring-server "0.5.0"]
                 [ring-cors "0.1.13"]
                 ;;[clj-http "3.9.1"]
                 ;; http response
                 [metosin/ring-http-response "0.9.1"]
                 ;; JDBC Dependencies
                 [org.clojure/java.jdbc "0.2.3"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [lib-noir "0.7.9"]
                 ;; JSON
                 [ring/ring-json "0.5.0"]
                 [robertluo/ring-middleware-format "0.8.0"]
                 [cheshire "5.9.0"]
                 [clj-json "0.5.3"]
                 ;; reagent
                 [reagent/reagent "0.8.1"]
                 ;; ajax
                 [cljs-ajax/cljs-ajax "0.8.0"]
                 ;; css
                 [clj-commons/cljss "1.6.4"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler clojure-web.handler/app
         :init    clojure-web.handler/init
         :destroy clojure-web.handler/destroy}
  :profiles
  {:uberjar    {:aot :all}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev        {:dependencies [[ring/ring-mock "0.4.0"]
                               [ring/ring-devel "1.7.1"]]}}
  :local-repo "F:\\ClojureRepos"
  :repositories [
                 ["central" "http://maven.aliyun.com/nexus/content/groups/public"]
                 ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]])
