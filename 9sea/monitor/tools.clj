(ns monitor.tools
    (:import
        [java.io BufferedInputStream BufferedReader InputStreamReader]
    )
    (:require
        [utilities.shutil :as sh]
    )
    (:import 
        [java.nio.file Files LinkOption]
    )
)

(defn check-process [tag]
    (let [
            cmd  (into-array 
                    ["/bin/sh" "-c" 
                        (str "ps -ef |grep -v grep |grep " tag)
                    ]
                )
            run (Runtime/getRuntime)
            p (.exec run cmd)
            out (->>
                    (.getInputStream p)
                    BufferedInputStream.
                    InputStreamReader.
                    BufferedReader.
                    .readLine
                )
        ]
        out
    )
)

 (defn now [] (new java.util.Date))

(defn restart-process [bash]
    (let [
            cmd  (into-array 
                    ["/bin/sh" "-c" 
                        bash ";"
                    ]
                )
            run (Runtime/getRuntime)
            ;p (apply (partial sh/execute " java ") bash)
            p (.exec run cmd)
        ]
        p
    )
)

(defn check [tag bash sleeptime]
    (Thread/sleep sleeptime)
    (let [flag (check-process tag)
        ]
        (if (nil? flag)
            (restart-process bash)
        )
    )
    (recur tag bash sleeptime)
)

(defn fileList [p]
    (try
        (with-open [files (Files/newDirectoryStream (sh/getPath p))]
            (->> files
                (filter #(Files/isRegularFile % (into-array LinkOption [])))
                (reduce #(str %1 "\n" %2))
                doall
            )
        )
        (catch Exception e 
            e
        )
    )

)
