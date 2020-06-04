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

## Deploy to TAS4K8S - Source Code Only


<hr size=2 />
Pas Apicella [pasa at vmware.com] is an Advisory Application Platform Architect at VMware APJ 