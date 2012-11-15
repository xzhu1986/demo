s3://isell.demo/batch/data/source/rlm/
s3://isell.demo/batch/data/output/rlm/
s3://isell.demo/batch/data/temp/rlm/
s3://isell.demo/batch/data/uuid/rlm/
false
onlyUpdateChanged  true   UpdateALL  false

s3://isell.demo/batch/scripts/java/cloud-rlm-batch.jar

s3://isell.demo/bootstrap-actions/rlm-batch.sh

s3://isell.log/cloud-rlm-batch/logs/


AWS Hadoop Libs:

activation-1.1.jar                  ftpserver-deprecated-1.0.0-M2.jar          jettison-1.1.jar                            kfs-0.2.LICENSE.txt
annotations.jar                     gson-1.6.jar                               jetty-6.1.26.jar                            libthrift-0.7.0.jar
ant-1.8.1.jar                       guava-12.0.jar                             jetty-ajp-7.5.4.v20111024.jar               log4j-1.2.16.jar
ant-launcher-1.8.1.jar              hadoop-capacity-scheduler-0.20.205.jar     jetty-all-7.5.4.v20111024-javadoc.jar       mail-1.4.3.jar
ant-nodeps-1.8.1.jar                hadoop-fairscheduler-0.20.205.jar          jetty-annotations-7.5.4.v20111024.jar       mina-core-2.0.0-M5.jar
apache-jar-resource-bundle-1.4.jar  hadoop-thriftfs-0.20.205.jar               jetty-client-7.5.4.v20111024.jar            mockito-all-1.8.5.jar
asm-3.1.jar                         hamcrest-all-1.1.jar                       jetty-continuation-7.5.4.v20111024.jar      netty-3.2.4.Final.jar
avro-1.5.3.jar                      hamcrest-core-1.1.jar                      jetty-deploy-7.5.4.v20111024.jar            opencsv-1.8.jar
avro-compiler-1.5.3.jar             high-scale-lib-1.1.1.jar                   jetty-http-7.5.4.v20111024.jar              org.apache.taglibs.standard.glassfish_1.2.0.v201004190952.jar
avro-ipc-1.5.3.jar                  high-scale-lib.jar                         jetty-io-7.5.4.v20111024.jar                paranamer-2.3.jar
avro-maven-plugin-1.5.3.jar         hsqldb-1.8.0.10.LICENSE.txt                jetty-jmx-7.5.4.v20111024.jar               plexus-active-collections-1.0-beta-2.jar
aws-java-sdk-1.3.2.jar              hsqldb-1.8.0.10.jar                        jetty-jndi-7.5.4.v20111024.jar              plexus-build-api-0.0.4.jar
build-helper-maven-plugin-1.5.jar   httpclient-4.1.1.jar                       jetty-jsp-2.1-7.5.4.v20111024.jar           plexus-compiler-api-1.8.1.jar
com.sun.el_1.0.0.v201004190952.jar  httpclient-cache-4.1.1.jar                 jetty-overlay-deployer-7.5.4.v20111024.jar  plexus-compiler-javac-1.5.3.jar
commons-beanutils-1.7.0.jar         httpcore-4.1.jar                           jetty-plus-7.5.4.v20111024.jar              plexus-compiler-manager-1.5.3.jar
commons-beanutils-core-1.8.0.jar    httpcore-nio-4.1.jar                       jetty-policy-7.5.4.v20111024.jar            plexus-digest-1.0.jar
commons-cli-1.2.jar                 httpmime-4.1.1.jar                         jetty-rewrite-7.5.4.v20111024.jar           plexus-interpolation-1.12.jar
commons-codec-1.5.jar               icu4j-4_0_1.jar                            jetty-security-7.5.4.v20111024.jar          plexus-io-1.0-alpha-4.jar
commons-collections-3.2.1.jar       jackson-core-asl-1.7.3.jar                 jetty-server-7.5.4.v20111024.jar            plexus-resources-1.0-alpha-5.jar
commons-configuration-1.6.jar       jackson-mapper-asl-1.7.3.jar               jetty-servlet-7.5.4.v20111024.jar           plexus-utils-2.1.jar
commons-daemon-1.0.1.jar            jamon-anttask-2.4.0.jar                    jetty-servlets-7.5.4.v20111024.jar          plexus-velocity-1.1.3.jar
commons-digester-1.8.jar            jamon-api-2.3.0.jar                        jetty-spring-7.5.4.v20111024.jar            protobuf-java-2.4.0.jar
commons-el-1.0.jar                  jamon-maven-plugin-2.3.4.jar               jetty-util-6.1.26.jar                       protobuf-java-2.4.0a.jar
commons-httpclient-3.1.jar          jamon-processor-2.4.1.jar                  jetty-util-7.5.4.v20111024.jar              servlet-api-2.5.jar
commons-lang-2.5.jar                jamon-runtime-2.4.0.jar                    jetty-webapp-7.5.4.v20111024.jar            slf4j-api-1.6.4.jar
commons-logging-1.1.1.jar           jasper-compiler-5.5.23.jar                 jetty-websocket-7.5.4.v20111024.jar         slf4j-log4j12-1.6.4.jar
commons-logging-adapters-1.1.1.jar  jasper-runtime-5.5.23.jar                  jetty-xml-7.5.4.v20111024.jar               smart-cli-parser.jar
commons-logging-api-1.1.1.jar       java_util_concurrent_chm.jar               jfreechart-0.9.21.jar                       snappy-java-1.0.3.2.jar
commons-math-2.1.jar                java_util_hashtable.jar                    joda-time-2.1.jar                           stax-1.2.0.jar
commons-net-1.4.1.jar               javax.el_2.1.0.v201004190952.jar           jruby-complete-no-joda-1.6.5.jar            stax-api-1.0.1.jar
core-3.1.1.jar                      javax.servlet.jsp_2.1.0.v201004190952.jar  jsp-2.1-6.1.14.jar                          surefire-api-2.4.3.jar
docbkx-maven-plugin-2.0.13.jar      jaxb-api-2.1.jar                           jsp-api-2.1-6.1.14.jar                      surefire-booter-2.4.3.jar
ecj-3.6.jar                         jaxb-impl-2.1.12.jar                       jsp-impl-2.1.3-b10.jar                      surefire-junit4-2.10.jar
emr-metrics-1.0.jar                 jcommon-0.9.6.jar                          jsr305.jar                                  velocity-1.7.jar
emr-s3distcp-1.0.jar                jdiff                                      jsr311-api-1.1.1.jar                        visualization-datasource-1.1.1.jar
file-management-1.2.1.jar           jersey-core-1.4.jar                        junit-4.8.1.jar                             xml-maven-plugin-1.0-beta-3.jar
ftplet-api-1.0.0.jar                jersey-json-1.4.jar                        junit.jar                                   xmlenc-0.52.jar
ftpserver-core-1.0.0.jar            jersey-server-1.4.jar                      kfs-0.2.2.jar                               zookeeper-3.4.2.jar
