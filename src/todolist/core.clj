(ns todolist.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [todolist.todos :as todos]
            [todolist.db :as db]))

(defn hello-handler [_]
  {:status 200
   :body {:message "Hello from Clojure Todolist!"}})

(defroutes app-routes
  (GET "/health" [] (hello-handler nil))
  (GET "/todos" [] (todos/get-todos nil))
  (POST "/todos" req (todos/create-todo (:body req)))
  (PUT "/todos/:id" [id :as req] (todos/update-todo id (:body req)))
  (DELETE "/todos/:id" [id] (todos/delete-todo id))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main []
  (println "Initializing database...")
  (db/init-db!)
  (println "Starting server on port 3000...")
  (run-jetty app {:port 3000 :join? false}))
