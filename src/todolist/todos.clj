(ns todolist.todos
  (:require [clojure.spec.alpha :as s]
            [todolist.db :as db]))

(s/def ::id string?)
(s/def ::title (s/and string?
                      #(not (empty? %))
                      #(<= (count %) 100)))
(s/def ::completed boolean?)
(s/def ::created-at integer?)

(s/def ::todo-create
  (s/keys :req-un [::title]
          :opt-un [::completed]))

(s/def ::todo
  (s/keys :req-un [::id ::title ::created-at ::completed]))

(defn validate-todo [todo spec]
  (if (s/valid? spec todo)
    [true todo]
    [false (s/explain-str spec todo)]))

(defn create-todo [todo-input]
  (let [[valid? result] (validate-todo todo-input ::todo-create)]
    (if valid?
      (let [todo (-> todo-input
                     (select-keys [:title :completed])
                     (assoc :id (str (java.util.UUID/randomUUID))
                            :created-at (System/currentTimeMillis)
                            :completed (or (:completed todo-input) false)))]
        (db/create-todo! todo)
        {:status 201 :body todo})
      {:status 400 :body {:error result}})))

(defn get-todos [_]
  {:status 200
   :body (db/get-all-todos)})

(defn update-todo [id todo-input]
  (if-let [existing-todo (db/get-todo id)]
    (let [updated-todo (merge existing-todo (select-keys todo-input [:title :completed]))
          [valid? result] (validate-todo updated-todo ::todo)]
      (if valid?
        (do
          (db/update-todo! updated-todo)
          {:status 200 :body updated-todo})
        {:status 400 :body {:error result}}))
    {:status 404 :body {:error "Todo not found"}}))

(defn delete-todo [id]
  (if (db/get-todo id)
    (do
      (db/delete-todo! id)
      {:status 204})
    {:status 404 :body {:error "Todo not found"}}))
