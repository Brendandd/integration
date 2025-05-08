# Integration Engine

This integration engine started as a learning opportunity to explore various integration patterns and solutions. It is currently under active development and is **not production-ready**. Significant changes and improvements are still needed to make it production-ready. The engine will support a wide range of integration patterns and provide foundational tools for transforming, routing, and processing messages across different microservices.

## Route Configuration
In this engine, routes are configured declaratively by linking various components. The configureRoute() method is where the magic happens, connecting adapters, transformers, filters, and splitters in an intuitive way.

Here’s an example of how a route can be configured:

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

- addInboundFlow(): Links inbound components (e.g., adapters, connectors) to the transformation and filtering logic.


- addInternalFlow(): Connects internal components like transformers and splitters.


- addOutboundFlow(): Defines the path to send data to the output (e.g., adapters).


- addDirectFlow(): Establishes a direct flow between inbound and outbound components without any transformations or filters.


## Component Configuration
Each component in the system is configured with annotations. These annotations make it easy to specify component behaviors such as content types, routing policies, and other configuration details.

Example:

```
java

@IntegrationComponent(name = "Allow-only-ADT-A04")
@AcceptancePolicy(name = "acceptADT^A04")
@ForwardingPolicy(name = "forwardAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7MessageTypeFilter extends BaseFilterProcessingStep {
}

```

- @IntegrationComponent: Identifies the component and provides metadata such as its name.


- @AcceptancePolicy: Defines the acceptance criteria for messages (e.g., message types).


- @ForwardingPolicy: Configures how messages will be forwarded after processing.


- @AllowedContentType: Specifies the content types that the component can handle (e.g., HL7). Content type validation will be a future change.

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
