# Spring Book API - TAS4K8s demo

The following demo is 

## Table of Contents

* [Run Locally](#run-locally)
* [Deploy to TAS4K8S - Source Artifact Push](#deploy-to-tas4k8s---source-artifact-push)
* [Deploy to TAS4K8S - Source Code Only](#deploy-to-tas4k8s---source-code-only)

## Run Locally 

Clone or Fork repository as follows.

```bash
$ git clone https://github.com/papicella/spring-book-service.git
$ cd spring-book-service
```

Build/Package as shown below.

```bash
$ ./mvnw -D skipTests package
```

Run as shown below accessing in a browser using - [http://localhost:8080/swagger-ui.html]

```bash
$ ./mvnw spring-boot:run
....
2020-06-04 09:43:08.598  INFO 4620 --- [           main] c.e.s.SpringBookServiceApplication       : Started SpringBookServiceApplication in 4.052 seconds (JVM running for 4.406)
2020-06-04 09:43:08.652  INFO 4620 --- [           main] c.e.springbookservice.LoadDatabase       : Preloading Book(id=1, title=Flexible Rails - Pas, author=Peter Armstrong)
2020-06-04 09:43:08.653  INFO 4620 --- [           main] c.e.springbookservice.LoadDatabase       : Preloading Book(id=2, title=Brownfield Application Development in .NET, author=Kyle Baley)
2020-06-04 09:43:08.654  INFO 4620 --- [           main] c.e.springbookservice.LoadDatabase       : Preloading Book(id=3, title=MongoDB in Action, author=Kyle Banker)
2020-06-04 09:43:08.655  INFO 4620 --- [           main] c.e.springbookservice.LoadDatabase       : Preloading Book(id=4, title=Java Persistence with Hibernate, author=Christian Bauer)
2020-06-04 09:43:08.656  INFO 4620 --- [           main] c.e.springbookservice.LoadDatabase       : Preloading Book(id=5, title=POJO's In Action, author=Chris Richardson)
```

![alt tag](https://i.ibb.co/Gvff5LL/tsl-book-service-tas4k8s-1.png)

You can also access the REST based API endpoints as shown below

```http request
$ http :8080/api/book/1
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 03 Jun 2020 23:52:24 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "author": "Peter Armstrong",
    "id": 1,
    "title": "Flexible Rails - Pas"
}

$ http :8080/api/book/
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 03 Jun 2020 23:52:26 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "author": "Peter Armstrong",
        "id": 1,
        "title": "Flexible Rails - Pas"
    },
    {
        "author": "Kyle Baley",
        "id": 2,
        "title": "Brownfield Application Development in .NET"
    },
    {
        "author": "Kyle Banker",
        "id": 3,
        "title": "MongoDB in Action"
    },
    {
        "author": "Christian Bauer",
        "id": 4,
        "title": "Java Persistence with Hibernate"
    },
    {
        "author": "Chris Richardson",
        "id": 5,
        "title": "POJO's In Action"
    }
]
```
## Create Service Broker

`Note: We are using helm3 here to install minibroker`

Create Namespace for minibroker and install with helm.

```bash
$ kubectl create ns minibroker
$ helm repo add minibroker https://minibroker.blob.core.windows.net/charts
$ helm repo update
$ helm install  minibroker --namespace minibroker minibroker/minibroker --set "deployServiceCatalog=false" --set "defaultNamespace=minibroker"
```

Verify installation.

```bash
$ helm ls -A
NAME      	NAMESPACE    	REVISION	UPDATED                              	STATUS  	CHART               	APP VERSION
certifier 	cert-manager 	1       	2020-05-28 15:22:04.423771 +1000 AEST	deployed	cert-manager-v0.15.0	v0.15.0
harbor    	harbor       	1       	2020-05-28 15:28:02.142397 +1000 AEST	deployed	harbor-1.3.2        	1.10.2
minibroker	minibroker   	1       	2020-06-04 09:58:01.94635 +1000 AEST 	deployed	minibroker-0.3.1
my-console	console      	1       	2020-06-01 09:16:14.497349 +1000 AEST	deployed	console-3.2.0       	3.2.0
nginx     	nginx-ingress	1       	2020-05-28 15:23:55.577671 +1000 AEST	deployed	nginx-ingress-1.36.3	0.30.0

$ kubectl get all -n minibroker
NAME                                         READY   STATUS    RESTARTS   AGE
pod/minibroker-minibroker-5fdb8b448f-qgw7d   2/2     Running   0          90s

NAME                            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
service/minibroker-minibroker   ClusterIP   10.39.254.247   <none>        80/TCP    90s

NAME                                    READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/minibroker-minibroker   1/1     1            1           91s

NAME                                               DESIRED   CURRENT   READY   AGE
replicaset.apps/minibroker-minibroker-5fdb8b448f   1         1         1       91s

```

Create Service Broker in TASK8s and enable access to the services. You will need to be logged in as ADMIN and provide the admin password as well

```bash
$ cf create-service-broker minibroker admin {ADMIN-PASSWORD} http://minibroker-minibroker.minibroker.svc.cluster.local
Creating service broker minibroker as admin...
OK

$ cf service-access
Getting service access as admin...
broker: minibroker
....

$ cf enable-service-access mysql
Enabling access to all plans of service mysql for all orgs as admin...
OK

$ cf enable-service-access mariadb
Enabling access to all plans of service mariadb for all orgs as admin...
OK

```

Verify the two MySQL services appear in the marketplace

```bash
$ cf marketplace --no-plans
Getting services from marketplace in org system / space development as admin...
OK

service   description              broker
mysql     Helm Chart for mysql     minibroker
mariadb   Helm Chart for mariadb   minibroker

TIP: Use 'cf marketplace -s SERVICE' to view descriptions of individual plans of a given service.
```

## Create Service 

Create a mariadb service as shown below

```bash
$ cf create-service mariadb 10-3-22 mariadb-svc -c '{"db": {"name": "my_database"}}'
Creating service instance mariadb-svc in org system / space development as admin...
OK

Create in progress. Use 'cf services' or 'cf service mariadb-svc' to check operation status.
```

Check the progress using a command as follows. Wait for it to say "**create succeeded**" before moving on to the next step

```bash
$ cf services
Getting services in org system / space development as admin...

name          service   plan      bound apps   last operation     broker       upgrade available
mariadb-svc   mariadb   10-3-22                create succeeded   minibroker
```

## Deploy to TAS4K8S - Source Artifact Push

We should already have a packaged artifact as per a previous step that packaged JAR files exists as shown below

```bash
$ ls -la ./target/spring-book-service-0.0.1-SNAPSHOT.jar
-rw-r--r--  1 papicella  staff  48342891 Jun  4 10:22 ./target/spring-book-service-0.0.1-SNAPSHOT.jar

```

Inspect the "**manifest.yaml**" file to see what our deployment will look like for TAS4K8s. We are referencing the service "**mariadb-svc**" as per above

```yaml
---
applications:
  - name: spring-book-service-api
    memory: 1024M
    instances: 1
    path: ./target/spring-book-service-0.0.1-SNAPSHOT.jar
    services:
      - mariadb-svc
```

Deploy to TAS4K8s as shown below using "**cf push**"

```bash
$ cf push -f manifest.yaml
Pushing from manifest to org system / space development as admin...
Using manifest file /Users/papicella/piv-projects/TAS4K8s/spring-book-service/manifest.yaml
Getting app info...
Creating app with these attributes...
+ name:        spring-book-service-api
  path:        /Users/papicella/pivotal/DemoProjects/spring-starter/pivotal/TAS4K8s/spring-book-service/target/spring-book-service-0.0.1-SNAPSHOT.jar
+ instances:   1
+ memory:      1G
  services:
+   mariadb-svc
  routes:
+   spring-book-service-api.apps.tas.lab.pasapples.me

Creating app spring-book-service-api...
Mapping routes...
Binding services...
Comparing local files to remote cache...
Packaging files to upload...
Uploading files...
 35.96 MiB / 35.96 MiB [====================================================================================================================================================================================================================] 100.00% 15s

Waiting for API to complete processing files...

Staging app and tracing logs...
   6 of 13 buildpacks participating
   org.cloudfoundry.openjdk                   v1.2.14
   org.cloudfoundry.jvmapplication            v1.1.12
   org.cloudfoundry.tomcat                    v1.3.18
   org.cloudfoundry.springboot                v1.2.13
   org.cloudfoundry.distzip                   v1.1.12
   org.cloudfoundry.springautoreconfiguration v1.1.11
   Previous image with name "harbor.lab.pasapples.me/library/6aa9e3df-3bb4-40a0-9041-41c430f2780d" not found

   Cloud Foundry OpenJDK Buildpack v1.2.14
   OpenJDK JRE 11.0.6: Contributing to layer
       Downloading from https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.6%!B(MISSING)10/OpenJDK11U-jre_x64_linux_hotspot_11.0.6_10.tar.gz
       Verifying checksum
       Expanding to /layers/org.cloudfoundry.openjdk/openjdk-jre
       Writing JAVA_HOME to shared
       Writing MALLOC_ARENA_MAX to shared
       Writing .profile.d/active-processor-count
   Java Security Properties v1.2.14: Contributing to layer
       Writing JAVA_OPTS to launch
   Security Provider Configurer v1.2.14: Contributing to layer
       Writing .profile.d/security-provider-classpath
       Writing .profile.d/security-provider-configurer
   Link-Local DNS v1.2.14: Contributing to layer
       Writing .profile.d/link-local-dns
   JVMKill Agent 1.16.0: Contributing to layer
       Downloading from https://java-buildpack.cloudfoundry.org/jvmkill/bionic/x86_64/jvmkill-1.16.0-RELEASE.so
       Verifying checksum
       Copying to /layers/org.cloudfoundry.openjdk/jvmkill
       Writing JAVA_OPTS to shared
   Class Counter v1.2.14: Contributing to layer
   Memory Calculator 4.0.0: Contributing to layer
       Downloading from https://java-buildpack.cloudfoundry.org/memory-calculator/bionic/x86_64/memory-calculator-4.0.0.tgz
       Verifying checksum
       Set $BPL_HEAD_ROOM to configure. Default 0
       Set $BPL_LOADED_CLASS_COUNT to configure. Default 35%!!(MISSING)o(MISSING)f classes
       Set $BPL_THREAD_COUNT to configure. Default 250
       Expanding to /layers/org.cloudfoundry.openjdk/memory-calculator
       Writing .profile.d/memory-calculator

   Cloud Foundry JVM Application Buildpack v1.1.12
   Executable JAR: Contributing to layer
       Writing CLASSPATH to shared
   Process types:
   executable-jar: java -cp $CLASSPATH $JAVA_OPTS org.springframework.boot.loader.JarLauncher
   task:           java -cp $CLASSPATH $JAVA_OPTS org.springframework.boot.loader.JarLauncher
   web:            java -cp $CLASSPATH $JAVA_OPTS org.springframework.boot.loader.JarLauncher

   Cloud Foundry Spring Boot Buildpack v1.2.13
   Spring Boot 2.3.1.BUILD-SNAPSHOT: Contributing to layer
       Writing CLASSPATH to shared
   5 application slices
   Process types:
   spring-boot: java -cp $CLASSPATH $JAVA_OPTS com.example.springbookservice.SpringBookServiceApplication
   task:        java -cp $CLASSPATH $JAVA_OPTS com.example.springbookservice.SpringBookServiceApplication
   web:         java -cp $CLASSPATH $JAVA_OPTS com.example.springbookservice.SpringBookServiceApplication

   Cloud Foundry Spring Auto-reconfiguration Buildpack v1.1.11
   Spring Auto-reconfiguration 2.11.0: Contributing to layer
       Downloading from https://repo.spring.io/release/org/cloudfoundry/java-buildpack-auto-reconfiguration/2.11.0.RELEASE/java-buildpack-auto-reconfiguration-2.11.0.RELEASE.jar
       Verifying checksum
       Copying to /layers/org.cloudfoundry.springautoreconfiguration/auto-reconfiguration
       Writing CLASSPATH to launch
   Adding layer 'launcher'
   Adding layer 'org.cloudfoundry.openjdk:class-counter'
   Adding layer 'org.cloudfoundry.openjdk:java-security-properties'
   Adding layer 'org.cloudfoundry.openjdk:jvmkill'
   Adding layer 'org.cloudfoundry.openjdk:link-local-dns'
   Adding layer 'org.cloudfoundry.openjdk:memory-calculator'
   Adding layer 'org.cloudfoundry.openjdk:openjdk-jre'
   Adding layer 'org.cloudfoundry.openjdk:security-provider-configurer'
   Adding layer 'org.cloudfoundry.jvmapplication:executable-jar'
   Adding layer 'org.cloudfoundry.springboot:spring-boot'
   Adding layer 'org.cloudfoundry.springautoreconfiguration:auto-reconfiguration'
   Adding 6/6 app layer(s)
   Adding layer 'config'
   *** Images (sha256:3b92cac4db3d826b790062814e8a2e54aa9ed1d8b16ac29a2196b895058f52d1):
   harbor.lab.pasapples.me/library/6aa9e3df-3bb4-40a0-9041-41c430f2780d
   harbor.lab.pasapples.me/library/6aa9e3df-3bb4-40a0-9041-41c430f2780d:b1.20200604.002942
   Adding cache layer 'org.cloudfoundry.jvmapplication:executable-jar'
   Adding cache layer 'org.cloudfoundry.springboot:spring-boot'
   Build successful

Waiting for app to start...

name:                spring-book-service-api
requested state:     started
isolation segment:   placeholder
routes:              spring-book-service-api.apps.tas.lab.pasapples.me
last uploaded:       Thu 04 Jun 10:30:47 AEST 2020
stack:
buildpacks:

type:           web
instances:      1/1
memory usage:   1024M
     state     since                  cpu    memory    disk      details
#0   running   2020-06-04T00:30:52Z   0.0%   0 of 1G   0 of 1G
```

## Deploy to TAS4K8S - Source Code Only


<hr size=2 />
Pas Apicella [pasa at vmware.com] is an Advisory Application Platform Architect at VMware APJ 