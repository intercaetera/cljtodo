(defproject todolist "0.1.0-SNAPSHOT"
  :description "A simple todolist app"
  :main todolist.core
  :plugins [[com.github.liquidz/antq "2.11.1264"]]
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [ring/ring-core "1.13.0"]
                 [ring/ring-jetty-adapter "1.13.0"]
                 [compojure "1.7.1"]
                 [ring/ring-json "0.5.1"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.12.780"]
                 [ring-cors "0.1.13"]])
