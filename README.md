# Implementation of Auto-Complete with MapReduce 

1. [Overview](#overview)

1. [Visualization](#visualization)

1. [Preliminaries](#preliminaries)


1. [Theory Behind Auto-Complete](#theory-behind-auto-complete)

1. [Hadoop Setup](#hadoop-setup)

1. [References](#references)


# Overview 

# Visualization 

# Preliminaries

* Operating system: The operating system is Ubuntu 18.04 LTS (Bionic Beaver).


* Docker container (<https://www.docker.com/why-docker>) version 18.09.5:

Docker enables users to bundle an application together with its preferred execution environment to be executed on a target machine. (<https://hadoop.apache.org/>)

See <https://docs.docker.com/install/> for more details on docker installation. Frequently used commands of docker can be found at <https://docs.docker.com/get-started/>.


* Hadoop cluster is launched within docker containers by following instructions at <https://devhub.io/repos/joway-hadoop-cluster-docker>, 3 containers with 1 master and 2 slaves will be started.

Output by running script `start-container.sh`:

```
start hadoop-master container...
start hadoop-slave1 container...
start hadoop-slave2 container...
```

# Theory Behind Auto-Complete 

* Most frequent user query-based suggestion generation


## Find IP on Linux Systems

* Method 1: `hostname -I`

* Method 2: `ifconfig | grep inet | grep broadcast`


# References 

1. [https://www.docker.com](https://www.docker.com)
2. [http://hadoop.apache.org/](http://hadoop.apache.org/)
3. [https://devhub.io/repos/joway-hadoop-cluster-docker](https://devhub.io/repos/joway-hadoop-cluster-docker)
4. [Speech and Language Processing, Daniel Jurafsky \& James H. Martin (Chapter 3)](https://web.stanford.edu/~jurafsky/slp3/3.pdf)
5. [MapReduce Tutorial on https://hadoop.apache.org](https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html)
