(ns smoketest.utilities.shutil
    (:require
        [clojure.string :as str]
    )
    (:use
        testing.core
        utilities.core
        utilities.shutil
    )
    (:import 
        [java.nio.file Path]
    )
)

(suite "rmtree"
    (:testbench 
        (fn [test]
            (let [p "st_rmtree"]
                (execute ["rm" "-rfv" p])
                (execute ["mkdir" "-p" p])
                (execute ["cp" "/dev/null" (str/join (sep) [p "haha"])])
                (execute ["mkdir" "-p" (str/join (sep) [p "hehe"])])
                (execute ["cp" "/dev/null" (str/join (sep) [p "hehe" "xixi"])])
                (test p)
            )
        )
    )
    (:fact rmtree 
        (fn [p] 
            (rmtree p)
            (let [{exitcode :exitcode} (execute ["stat" p])]
                (throw-if (= 0 exitcode)
                    AssertionError.
                    (format "%s exists" p)
                )
            )
        )
        :throws :nothing
    )
)

(suite "mkdir"
    (:testbench
        (fn [test]
            (let [rp "st_mkdir"]
                (try
                    (execute ["rm" "-rfv" rp])
                    (test (str/join (sep) [rp "hehe"]))
                (finally
                    (execute ["rm" "-rfv" rp])
                ))
            )
        )
    )
    (:fact mkdir
        (fn [p]
            (mkdir p)
            (let [{excode :exitcode} (execute ["stat" p])]
                (throw-if (> excode 0)
                    AssertionError.
                    (format "%s does not exist" p)
                )
            )
        )
        :throws :nothing
    )
    (:fact double-mkdir
        (fn [p]
            (mkdir p)
            (mkdir p)
        )
        :throws :nothing
    )
)

(suite "spit-file"
    (:testbench
        (fn [test]
            (let [p "st_spit_file"]
                (try
                    (rmtree p)
                    (test (str/join (sep) [p "hehe"]))
                (finally
                    (rmtree p)
                ))
            )
        )
    )
    (:fact spit-file
        (fn [p]
            (spitFile p "xixi")
            (slurp p)
        )
        :eq (fn [_] "xixi")
    )
)

(suite "open-file"
    (:testbench
        (fn [test]
            (let [d (tempdir "open-file")
                f (getPath d "xixi")
                ]
                (spitFile f "hehe")
                (try
                    (test f)
                (finally
                    (rmtree d)
                ))
            )
        )
    )
    (:fact open-file-fs
        (fn [f]
            (slurp (open-file f))
        )
        :eq
        (fn [_]
            "hehe"
        )
    )
    (:fact open-file-jar
        (fn [_]
            (slurp (open-file "@/resources/open-file.txt"))
        )
        :eq
        (fn [_]
            "haha"
        )
    )
)


(suite "execute"
    (:fact execute-stdout
        (execute ["echo" "-n" "haha"] :out :pipe)
        :is
        {
            :exitcode 0
            :out "haha"
        }
    )
    (:fact execute-stderr
        (execute ["bash" "-c" "echo -n haha 1>&2"] :err :pipe)
        :is
        {
            :exitcode 0
            :err "haha"
        }
    )
    (:fact execute-stdin
        (execute ["cat" "-"] :in "haha" :out :pipe)
        :is
        {
            :exitcode 0
            :out "haha"
        }
    )
    (:fact execute-stderr-to-stdout
        (execute ["bash" "-c" "echo -n haha 1>&2"] :out :pipe :err :out)
        :is
        {
            :exitcode 0
            :out "haha"
        }
    )
)

(suite "execute: with file"
    (:testbench
        (fn [test]
            (let [d (getPath "execute_test")
                    f (getPath d "xixi")
                ]
                (try
                    (rmtree d)
                    (spitFile f "haha")
                    (test f)
                (finally
                    (rmtree d)
                ))
            )
        )
    )
    (:fact execute-stdin-from-file
        #(execute ["cat" "-"] :in % :out :pipe)
        :eq
        (fn [_]
            {
                :exitcode 0
                :out "haha"
            }
        )
    )
    (:fact execute-stdout-to-file
        (fn [f]
            (execute ["echo" "-n" "hehe"] :out f)
            (slurp (.toFile f))
        )
        :eq
        (fn [_] "hehe")
    )
    (:fact execute-stderr-to-file
        (fn [f]
            (execute ["bash" "-c" "echo -n hehe 1>&2"] :err f)
            (slurp (.toFile f))
        )
        :eq
        (fn [_] "hehe")
    )
)
