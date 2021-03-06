(ns leiningen.new.chestnut
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]
            [clojure.string :refer [join]]))

(def render (renderer "chestnut"))

(defn dep-list [n list]
  (fn []
    (join "" (map #(str "\n" (apply str (repeat n " ")) "[" % "]") list))))

(defn http-kit? [opts]
  (some #{"--http-kit"} opts))

(defn site-middleware? [opts]
  (some #{"--site-middleware"} opts))

(defn om-tools? [opts]
  (some #{"--om-tools"} opts))

(defn server-clj-requires [opts]
  (if (http-kit? opts)
    ["org.httpkit.server :refer [run-server]"]
    ["ring.adapter.jetty :refer [run-jetty]"]))

(defn colre-cljs-requires [opts]
  (if (om-tools? opts)
    ["om.dom :as dom :include-macros true"]
    ["om-tools.dom :as dom :include-macros true"]))

(defn project-clj-deps [opts]
  (cond-> []
          (http-kit? opts) (conj ["http-kit \"2.1.19\""])
          (om-tools? opts) (conj ["prismatic/om-tools \"0.3.2\""])))

(defn chestnut [name & opts]
  (let [data {:name name
              :sanitized (name-to-path name)
              :server-clj-requires (dep-list 14 (server-clj-requires opts))
              :core-cljs-requires (dep-list 14 (core-cljs-deps opts))
              :project-clj-deps (dep-list 17 (project-clj-deps opts))
              :server-command (if (http-kit? opts) "run-server" "run-jetty")
              :compojure-handler (if (site-middleware? opts) "site" "api")}]
    (main/info "Generating fresh 'lein new' chestnut project.")
    (->files data
             ["project.clj"
              (render "project.clj" data)]
             ["resources/index.html"
              (render "resources/index.html" data)]
             ["src/clj/{{sanitized}}/server.clj"
              (render "src/clj/chestnut/server.clj" data)]
             ["src/clj/{{sanitized}}/dev.clj"
              (render "src/clj/chestnut/dev.clj" data)]
             ["src/cljs/{{sanitized}}/core.cljs"
              (render "src/cljs/chestnut/core.cljs" data)]
             ["LICENSE"
              (render "LICENSE" data)]
             ["README.md"
              (render "README.md" data)]
             [".gitignore"
              (render ".gitignore" data)]

             ;; Heroku support
             ["system.properties"
              (render "system.properties" data)]
             ["Procfile"
              (render "Procfile" data)])))
