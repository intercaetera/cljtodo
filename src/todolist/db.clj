(ns todolist.db
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]))

(def todos-table "todos")

(def dynamodb-host (or (System/getenv "DYNAMODB_HOST") "localhost"))

(def ddb (aws/client
           {:api :dynamodb
            :credentials-provider (credentials/basic-credentials-provider
                                    {:access-key-id "FAKE"
                                     :secret-access-key "FAKE"})
            :region "us-east-1"
            :endpoint-override {:protocol :http
                                :hostname dynamodb-host
                                :port 8000}}))

(defn create-table! []
  (aws/invoke ddb
              {:op :CreateTable
               :request
               {:TableName todos-table
                :AttributeDefinitions [{:AttributeName "id"
                                       :AttributeType "S"}]
                :KeySchema [{:AttributeName "id"
                             :KeyType "HASH"}]
                :ProvisionedThroughput {:ReadCapacityUnits 1
                                        :WriteCapacityUnits 1}}}))

(defn init-db! []
  (try
    (create-table!)
    (println "Created todos table")
    (catch Exception e
      (if (= "ResourceInUseException" (:__type (ex-data e)))
        (println "Todos table already exists")
        (throw e)))))

(defn ->ddb-item [todo]
  {:id {:S (:id todo)}
   :title {:S (:title todo)}
   :completed {:BOOL (:completed todo)}
   :created-at {:N (str (:created-at todo))}})

(defn <-ddb-item [item]
  {:id (get-in item [:id :S])
   :title (get-in item [:title :S])
   :completed (get-in item [:completed :BOOL])
   :created-at (Long/parseLong (get-in item [:created-at :N]))})

(defn create-todo! [todo]
  (aws/invoke ddb
              {:op :PutItem
               :request {:TableName todos-table
                         :Item (->ddb-item todo)}}))

(defn get-todo [id]
  (println "Running get-todo" id)
  (let [result (aws/invoke ddb
                           {:op :GetItem
                            :request {:TableName todos-table
                                      :Key {:id {:S id}}}})]
    (when-let [item (:Item result)]
      (<-ddb-item item))))

(defn get-all-todos []
  (let [result (aws/invoke ddb
                           {:op :Scan
                            :request {:TableName todos-table}})]
    (->> (:Items result)
         (map <-ddb-item))))

(defn update-todo! [todo]
  (aws/invoke ddb
              {:op :PutItem
               :request {:TableName todos-table
                         :Item (->ddb-item todo)}}))

(defn delete-todo! [id]
  (aws/invoke ddb
              {:op :DeleteItem
               :request {:TableName todos-table
                         :Key {:id {:S id}}}}))
