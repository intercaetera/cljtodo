(ns todolist.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn hello-handler [_]
  {:status 200
   :body {:message "Hello from Clojure Todolist!"}})

(defroutes app-routes
  (GET "/health" [] (hello-handler nil))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main []
  (println "Starting server on port 3000...")
  (run-jetty app {:port 3000 :join? false}))
