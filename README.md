# DigiDoc Test Service

This repository contains back-end part of the test service to operate on DigiDoc containers.

**DigiDoc Test Service** available [here](https://18.156.149.72/)

### Contents

* [Overview](#overview)
* [Features](#features)
* [Dependencies](#dependencies)
* [System requirements](#system-requirements)
* [Installing](#installing)

### Overview

This application allows you to operate on DigiDoc container:

- **Upload files** - you can upload multiple files to make them signed with ID Card
- **Sign files** - you can create DigiDoc container signed with ID Card
- **Generate link** - you can generate the link and get DigiDoc container by this link
- **Download** - you can download DigiDoc container to your locale machine

### Features

* Use your ID-card with integrated DigiDoc key to sign files;

* [Id software](https://installer.id.ee/) has to be installed on your local machine to use **DigiDoc Test Service**;

* DigiDoc4j used for digitally signing files and signature verification;

* Created container will be available to download during 48 hours;

* There is restriction for file upload - maximum number of files is 20 and maximum size of one file is 10 Mb 

* Link generated with [Bitly](https://bitly.com/) service;

### Dependencies

All code is written in Java 11 and build on the following set of technologies:
- Spring Framework
- DigiDoc4j -  Java library for digitally signing documents and creating digital signature containers of signed documents
- h2database - Java SQL database

### System requirements

* OS ubuntu18.04 (2vCPU, 4GB RAM, 20GB HDD);
* docker v18+ [How to install docker](https://docs.docker.com/engine/install/ubuntu/)
* docker-compose v1.25+ [How to install docker-compose](https://docs.docker.com/compose/install/)

### Installing
This repository contains only frontend part. To install all components also follow READMEs in these repositories:
- [DigiDoc test service load balancer](https://github.com/Introduct/digi-doc-test-service-lb)
- [DigiDoc backend test service](https://github.com/Introduct/digi-doc-test-service)
Each componenet is installed with help of docker-compose and each repository contains corresponding compose file.

**1. Build backend docker image**

Clone this repo to destination host and build docker image. Dockerfile contains all neccessary build steps.
```
git clone  https://github.com/Introduct/digi-doc-test-service
cd digi-doc-test-service 
docker build . -t digi-doc-test-service
```

**2. Parametrize settings.**

[Settings for backend service](https://github.com/Introduct/digi-doc-test-service/blob/master/docker-compose-dev.yml#L35-L40) can be reviwed in compose file.
Database settings can be used as is since the database is deployed alongside with service in docker container.
The only settings that should be revised and tuned for you environment is `HOST_NAME`. It should be set to IP or DNS name of the destination host.

**3. Run backend as docker service.**

On destination host run docker-compose from folder with cloned repo.
It will start backend alongside with database.
```
HOST_NAME=https://<host_ip_or_dns_name> docker-compose -p digi-doc-test -f docker-compose-dev.yml up -d 
cd ..
```
