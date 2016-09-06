# FIMS Test Suite

This repository contains the work in progress of the FIMS Test Suite that is being developed by the FIMS Test Suite and Platform Group. This group is a subgroup of the FIMS Technical Board and is created with the intention of providing tools that will assist in developing, testing and certifying FIMS compliant services.

## Overview

The FIMS Test Suite will be an application that allows developers of FIMS services and FIMS service clients to validate whether the messages they are sending and receiving are FIMS compliant. At the moment of writing the available functionality is limited to logging HTTP messages that are being sent from a FIMS client to a FIMS service and vice versa.

## How does it work?

The normal use case is that a FIMS client sends an HTTP request to a FIMS service and then the FIMS service will send an HTTP response back. Optionally the HTTP request can contain a `<bms:replyTo>` and/or `<bms:faultTo>` nodes, which indicate endpoints that the FIMS service should use when it needs to send asynchronously messages back to the client.

The FIMS Test Suite is built such that it can intercept such messages and perform operations on them. At the moment of writing the only available functionality is writing these (XML) messages in pretty print format to a text file.

## How to build it?

The project is built in Java and uses [Gradle][] as a build tool. It should be sufficient to have the Java JDK installed and the `$GRADLE_HOME/bin` folder added to the `$PATH` variable. Then running `gradle run` on the commandline should build and run the application.

[Gradle]: http://www.gradle.org "Gradle"

## How to use it?

In order to use this application you ideally have already a FIMS client and a FIMS service communicating with each other. It is however possible to test this application by simply logging HTTP traffic. The key concept is that the FIMS Test Suite works as a 'proxy' server. The FIMS client will not send messages directly to the FIMS service, but to the FIMS Test Suite.The FIMS Test Suite will redirect the messages to the FIMS service. Responses from the FIMS Service are in turn sent to the FIMS Test Suite and redirected back again to the FIMS client. This way the FIMS Test Suite will see all the messages being sent between the client and service.

The application is built with a modular approach, so it will be easy to add additional modules in the future. Now there are two modules available. The 'Proxy' module and the 'Logging' module. The Proxy module takes care of rerouting the messages coming from the FIMS client to the FIMS service and back. The Logging module will take care of writing the messages that pass by to a log file.

##### Configuring the Proxy module
- Local Address: Set it to the IP address or Host name how the FIMS service can reach the machine with FIMS Test Suit running. This address will be used by FIMS service to send the asynchronous messages back to the client.
- Local Port: Set it to any available port number. This port number must be used by the FIMS client to communicate with the proxy.
- Remote Address: Set it to the IP address or Host name of the FIMS service.
- Remote Port: Set it to the port number which the FIMS service uses.

Click Connect to start the proxy.

##### Configuring the Logging module
Enable the logging method that you want to use and press Start to start logging. 

##### Configuring your FIMS client
It's important that your FIMS client is being configured to communicate to the FIMS Test Suite instead of the original FIMS service. Use the local address an local port number that you have provided in the Proxy configuration.

## Future work
We are planning to expand the FIMS Test Suite with more modules. Currently there is work in progress for a validation module, that will be capable of validating the messages for FIMS compliance. Further there are plans for an Activity Monitor Module that will provide real time feedback of activity and can help with troubleshooting problems.

