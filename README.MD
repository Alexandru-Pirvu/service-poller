# Simple service poller application

## General description

A simple reactive application for demonstration purposes. It provides a basic interface
where users can add services and check their status in real time.

## General Architecture

The main system consists of 3 parts:
* MySQL database
* Java backend
* React frontend

### Java backend

The backend is created as a reactive service. It is constructed on top of the _**vert.x**_
framework and uses the _**verticles/event bus**_ approach for inter component communication.

The service exposes a simple http REST api for managing services. The server is created by
the vert.x instance and each request handler is reactive. The event bus of the backend is
exposed and allows inbound and outbound messages for real time client notifications
through web sockets.

The DB connection properties are configured via environment variables. Check docker-compose.yml for details.

### React fronted

The frontend is a thin React service. It was created with the _create-react-app_ tool and uses
TypeScript. It communicates with the backend via REST and connects to the vert.x event bus
through the websockets protocol.

## Deployment

All services are dockerized and a demo configuration is provided through the docker-compose file.
#### Deploying the application for the first time:

* ```cd docker && docker-compose up -d --build```
#### Subsequent deployments:
* ```cd docker && docker-compose up -d```

All the general docker and docker-compose commands can be used to change configurations.

In order to test the application an additional service is created: **_mockserver_**.
The mockserver is a simple node application that exposes 100 http servers on different
ports: 3000-3100. Each server is mapped to a single route: **/** and responds randomly
with 80% chance of success.

## Needed improvements

* multi-user support: authorisation and authentication
* unit and integration testing
* code refactorings:
  * better logging
  * better error handling
  * possible reactive alternatives to the event bus for certain situations
* general better UX: 
  * infinite scrolling
  * server-side pagination
* tls configurations

