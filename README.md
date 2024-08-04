# Integration

An extremely basic integration engine written in Java, Spring, Spring Boot, Apache Camel & Hibernate/JPA. This project was created as a learning opportunity and is nowhere near complete.  The idea is each component is deployed as a separate module and all communication is via queues and topics.
<br>
<br>
This project enable integration routes to be created which are composed of components (communication points and processing step).
<br>
<br>
Communication Point types:
<ol>
	<li>Inbound MLLP communication point</li> 
	<li>Outbound MLLP communication point</li> 
	<li>Inbound Directory communication point</li> 
	<li>Outbound Directory communication point</li> 
</ol>

<br>
<br>
Processing step types:
<ol>
	<li>Message Filters</li> 
	<li>Message Transformers</li> 
	<li>Message Splitters</li> 
</ol>

<br>
<br>
Routes are connected using:
<ol>
	<li>Outbound route connectors</li> 
	<li>Inbound route connectors</li> 
</ol>

<br>
<br>
Some other important features:
<ol>
	<li>Guaranteed message delivery</li>
	<li>All message flows recorded in the database</li>
	<li>Messages can be sent to multiple consumers using topics</li>
</ol>


<br>
<br>
A lot needs to be done including:
<ol>
	<li>Handling ACKs from destination systems</li>
	<li>More communication point types eg. REST, SOAP</li>
	<li>Stopping/starting components</li>
	<li>Monitoring</li>
	<li>Reporting</li>
	<li>REST API</li>
	<li>A UI</li>
</ol>


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development purposes.

### Prerequisites

Download and install the following.  Just use the default settings.


[Apache Ignite](https://ignite.apache.org/)
<br>
[Apache MX Artemis](https://activemq.apache.org/components/artemis/)
<br>
[MySQL](https://www.mysql.com/)
<br>
[MySQL Workbench](https://www.mysql.com/products/workbench/)
<br>
[Smart HL7 Message Sender and Receiver](https://smarthl7.blogspot.com/p/download.html)


### Installation

A step by step guide that will tell you how to get the development environment up and running.

```
$ Start Apache Ignite
$ Start Apache MX Artemis
$ Start MySQL and import the database in the integration-core module.
$ Start each of the examples in integration-examples.  Each of these is a Spring Boot application as is deployed to Apache Tomcat.  The VM arguments below need to be added.  I have only done this in Eclipse so far.
```


```
--add-opens=java.base/jdk.internal.access=ALL-UNNAMED
--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED
--add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED
--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED
--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED
--add-opens=java.base/java.io=ALL-UNNAMED
--add-opens=java.base/java.nio=ALL-UNNAMED
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED
--add-opens=java.base/java.math=ALL-UNNAMED
--add-opens=java.sql/java.sql=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.time=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.management/sun.management=ALL-UNNAMED
--add-opens java.desktop/java.awt.font=ALL-UNNAMED
```

## Usage

A few examples of useful commands and/or tasks.

```
$ To send a HL7 message via MLLP using Smart HL7 Message Sender
$ Second example
$ And keep this in mind
```




