# Integration Engine

This integration engine started as a learning opportunity to explore various integration patterns and solutions. It is currently under active development and is **not production-ready**. Significant changes and improvements are still needed to make it production-ready. The engine will support a wide range of integration patterns and provide foundational tools for transforming, routing, and processing messages across different microservices.

One of the key features of this engine is how easy it is to configure routes and define the relationships between components. Routes are built through simple, declarative method calls, where you can easily link inbound connectors, transformers, filters, splitters, and outbound adapters. For example:

```
java

@Override
@PostConstruct
public void configureRoute() throws Exception {
    addInboundFlow(mllpInboundAdapter, transformation, filter);
    addInboundFlow(inboundRouteConnector1, transformation);
    addInternalFlow(transformation, splitter);
    addInternalFlow(filter, splitter);
    addOutboundFlow(splitter, mllpOutboundAdapter);
    addDirectFlow(inboundRouteConnector2, mllpOutboundAdapter);
    
    applyConfiguration();
}
```
---

## 🛠 Getting Started

These instructions will help you set up a local development environment.

### Prerequisites

- **Docker**
- **Java (JDK 17 or higher)**: Required for building the application.
- **Maven**: Required for building the Java project locally.

---

## 🚀 Installation

From the **integration** folder

```
mvn clean install
```

Then, navigate to the **integration/config/samples/scripts** folder and run the following:

### First, run these services:

```
run-apache-activemq-artemis

run-apache-ignite

run-mysql
```

### Then, proceed with running the integration services:

```
run-file-input-route

run-file-output-route

run-mllp-input-route

run-mllp-output-route

run-rest-services
```

Please see the individual `README.md` files under `integration/integration-examples` for details on the routes.

### MLLP Sending and Receiving

MLLP communication (sending and receiving HL7 messages) can be easily performed using **SMART HL7 Sender** and **SMART HL7 Receiver**. These tools allow you to send and receive HL7 messages over the MLLP protocol, which is used for healthcare integration.

For more details on setting up and using the SMART HL7 tools, please refer to the respective documentation or setup guides.

### Testing REST Services with Postman

You can interact with and test the REST services provided by the integration engine using **Postman**. 
