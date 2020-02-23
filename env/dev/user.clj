(ns user
  (:require [clojure-web.handler :as handler]
            [ring.server.standalone :as standalone]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'handler/app
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (file/wrap-file "resources")
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      file-info/wrap-file-info))

(defn start
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 8080)]
    (reset! server
            (standalone/serve (get-handler)
                              {:port port
                               :init handler/init
                               :auto-reload? true
                               :destroy handler/destroy
                               :join true}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop []
  (.stop @server)
  (reset! server nil))
