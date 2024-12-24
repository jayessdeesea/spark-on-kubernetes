# 1. What is this?

These are my notes on how to create a local spark cluster for IntelliJ development under Windows. This is all a work
in progress as I don't really know Kubernetes very well nor the best ways to run Spark-on-Kubernetes.

This creates the following containers:

* A container running Apache Spark history server
* A container running Apache Spark master
* A container running Apache Spark worker
* A container SSH server that enables IntelliJ to call spark-submit

# 2. Prereqs

## 2.1. Install third-party products

* https://www.docker.com/products/docker-desktop/
* https://www.jetbrains.com/idea/ <-- with the spark plugin

## 2.2. Create Durable Storage

```bash
mkdir -p C:\kubernetes\storage\spark-work
``` 

# 3. Create the cluster

## 3.0. Reset Kubernetes

Within Docker Desktop

* Check for updates and apply as necessary
* Reset to factory defaults
* Enable Kubernetes (Settings -> Kubernetes -> Enable Kubernetes)

## 3.1. Create the Kubernetes namespace

Do this:

* Apply the 01-spark-namespace.yaml file by right click, Apply to cluster "docker-desktop" ...

Set the default namespace via
* Services -> docker-desktop
* Right click, select Namespace
* select spark-namespace 

## 3.2. Set up the permissions

Action

* Apply the 02-spark-permissions.yaml file by right click, Apply to cluster "docker-desktop" ...

## 3.3. Create the shared volumes

Action

* Apply the 03-spark-volume.yaml by file right click, Apply to cluster "docker-desktop" ...

Verify

* Look for a volume called spark-work-dir-volume under Service -> docker-desktop -> Storage -> Persistent Volume

## 3.4. Start history server container

Action

* Apply the 04-spark-history-server.yaml by file right click, Apply to cluster "docker-desktop" ...

Verify

* http://localhost:18080/ <-- This should show the history server

## 3.5. Start master container

Action

* Apply the 05-spark-master.yaml by file right click, Apply to cluster "docker-desktop" ...

Verify

* http://localhost:8080/ <-- this should show the spark UI with zero workers

## 3.6. Start worker-a container

Action

* Apply the 06-spark-worker-a.yaml by file right click, Apply to cluster "docker-desktop" ...

Verify

* http://localhost:8080/ <-- this should show the spark UI with a single worker
* http://localhost:8081/ <-- this should show the worker UI

## 3.7. Start spark-ssh container

Action

* docker build -t localhost/spark-ssh:1.0 kubernetes/07-spark-ssh
* Apply the 07-spark-ssh.yaml file

# 4. IntelliJ

maven clean,package