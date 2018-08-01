# Cloud Native Roadshow Labs  [![Build Status](https://travis-ci.org/openshift-labs/cloud-native-labs.svg?branch=ocp-3.10)](https://travis-ci.org/openshift-labs/cloud-native-labs)

This is a one-day hands-on lab experience for bulding Cloud Native applications using 
Red Hat OpenShift Application Runtimes (Spring Boot, WildFly Swarm, Eclipse Vert.x and Node.js) 
utilizing a microservices architecture.


## CoolStore Online Store App

CoolStore is an online store web application built using Spring Boot, WildFly Swarm, Eclipse Vert.x, 
Node.js and AngularJS adopting the microservices architecture.

* **Web**: A Node.js/Angular front-end
* **API Gateway**: aggregates API calls to back-end services and provides a condenses REST API for front-end
* **Catalog**: a REST API for the product catalog and product information
* **Inventory**: a REST API for product's inventory status

```
                    +-------------+
                    |             |
                    |     Web     |
                    |             |
                    |   Node.js   |
                    |  AngularJS  |
                    +------+------+
                          |
                          v
                    +------+------+
                    |             |
                    | API Gateway |
                    |             |
                    |   Vert.x    |
                    |             |
                    +------+------+
                          |
                +---------+---------+
                v                   v
          +------+------+     +------+------+
          |             |     |             |
          |   Catalog   |     |  Inventory  |
          |             |     |             |
          | Spring Boot |     |WildFly Swarm|
          |             |     |             |
          +-------------+     +-------------+
```
