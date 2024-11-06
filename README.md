# Integration

An extremely basic integration engine written in Java, Spring, Spring Boot, Apache Camel, and Hibernate/JPA.  This project was created as a learning opportunity and is still in development.

This integration engine enables the creation of routes which are composed of components (communication points and processing steps). The route configuration determines how a message flows to and from the various components.

Communication between components and routes is managed using topics, enabling a single message to flow to one or more components for further processing.  To guarantee delivery between the components the [transactional outbox pattern](https://microservices.io/patterns/data/transactional-outbox.html) is used.

Each component may have a message acceptance policy and/or a message forwarding policy. A message acceptance policy determines if the current component will accept the message, while a message forwarding policy specifies whether a component will forward the message to other components.

One or more routes can be deployed as a single microservice.  

### Communication Point Types
<ol>
    <li>Inbound MLLP communication point</li> 
    <li>Outbound MLLP communication point</li> 
    <li>Inbound Directory communication point</li> 
    <li>Outbound Directory communication point</li> 
</ol>

### Processing Step Types
<ol>
    <li>Message Filters</li> 
    <li>Message Transformers</li> 
    <li>Message Splitters</li> 
</ol>

### Routes Are Connected Using
<ol>
    <li>Outbound route connectors</li> 
    <li>Inbound route connectors</li> 
</ol>

### Other Important Features
<ol>
    <li>Guaranteed message delivery</li>
    <li>All message flows recorded in the database</li>
    <li>Messages can be sent to multiple consumers using topics</li>
</ol>

### Future Enhancements
<ol>
    <li>Handling ACKs from destination systems</li>
    <li>More communication point types (e.g., REST, SOAP)</li>
    <li>Stopping/starting components</li>
    <li>Monitoring</li>
    <li>Reporting</li>
    <li>REST API</li>
    <li>A UI</li>
</ol>

## Getting Started

These instructions will help you set up the project on your local machine for development purposes.

### Prerequisites

Download and install the following using default settings:

- [Apache Ignite](https://ignite.apache.org/)
- [Apache ActiveMQ Artemis](https://activemq.apache.org/components/artemis/)
- [MySQL](https://www.mysql.com/)
- [MySQL Workbench](https://www.mysql.com/products/workbench/)
- [Smart HL7 Message Sender and Receiver](https://smarthl7.blogspot.com/p/download.html)

### Installation

Follow these steps to get the development environment up and running.  The Apache Ignite VM arguments are for Java 17.  

application.properties set the port for each example so please override on the command line if required.

```plaintext
From the integration module run the maven command: mvn clean install

Start Apache Ignite
Start Apache ActiveMQ Artemis
Start MySQL and import the database in the integration-core module.

Start each of the examples in integration-examples using the command below. Each of these is a Spring Boot application and is deployed to Apache Tomcat.

Starting the REST API module follows the same instructions.

mvn spring-boot:run -Dspring-boot.run.jvmArguments="--add-opens=java.base/jdk.internal.access=ALL-UNNAMED --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED --add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED --add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED --add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.sql/java.sql=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.management/sun.management=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"

