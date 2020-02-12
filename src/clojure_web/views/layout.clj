(ns clojure-web.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "clojure-web"]
     (include-css "/css/tailwind.min.css" "/css/screen.css")]
    [:body body]))
