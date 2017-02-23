(ns oracle-spatial-to-kml.oracle-db-conversion
  (:require [yesql.core :as y]))

(def db-spec {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname     "//127.0.0.1:3306/seurakunnat_geo"
              :user        "root"
              :password    "root"})

(def data-fields
  [:hiippakunta, :hpk_koodi, :kunta, :kunta_nro, :rovastikunta, :rvk_koodi, :seurakunta, :seurakuntatalous, :srk_koodi, 
                                   :srkt_koodi])

(y/defqueries "../src/sql/queries.sql"  
   {:connection db-spec})


(def basic-info-array
  [:oracle_id :seurakunta_nimi :seurakunta_tunnus :seurakunta_alue])

(defn get-basic-info
  [x]
  (zipmap basic-info-array (clojure.string/split 
                             (clojure.string/replace (re-find #"(?<=values\s\().*(?=MDSYS\.SDO_GEOMETRY\()" x) #"'" "") #",")))

(defn get-alue-tyyppi
  [x]
  (subs x (- (count x) 5) (- (count x) 3))  )


(defn get-ordinate-array
  [x]
  (re-find #"(?<=MDSYS\.SDO_ORDINATE_ARRAY\().*(?=\)\))" x))

(defn get-elem-info-array
  [x]
  (re-find #"(?<=MDSYS\.SDO_ELEM_INFO_ARRAY\().*(?=\),MDSYS\.SDO_ORDINATE_ARRAY)" x))

(defn get-geom-type
  [x]
  (re-find #"(?<=MDSYS\.SDO_GEOMETRY\().*(?=,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY)" x))

(defn to-mysql-array
  [rivi]
  (assoc 
    (get-basic-info rivi) 
    :geometry_type (get-geom-type rivi) 
    :ordinate_array_info (get-elem-info-array rivi) 
    :ordinate_array (get-ordinate-array rivi)
    :aluetyyppi (get-alue-tyyppi rivi)))


(defn save-to-database
  []
  (with-open [rdr (clojure.java.io/reader "resources/export.sql")]
    (doseq [rivi (drop 5 (line-seq rdr))]
      (if (get-ordinate-array rivi)
        (insert-seurakunta!  (to-mysql-array  rivi))))))
