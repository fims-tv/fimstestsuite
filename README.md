# FIMS Test Suite

This repository contains the work in progress of the FIMS Test Suite that is being developed by the FIMS Test Suite and Platform Group. This group is a subgroup of the FIMS Technical Board and is created with the intention of providing tools that will assist in developing, testing and certifying FIMS compliant services.

## Overview

The FIMS Test Suite will be an application that allows developers of FIMS services and FIMS service clients to validate whether the messages they are sending and receiving are FIMS compliant. At the moment of writing the available functionality is limited to basic XML-Schema validation using the SOAP interfaces and logging of the HTTP messages that are being sent from a FIMS client to a FIMS service and vice versa.


## Version

1.1.0

## How does it work?

The normal use case is that a FIMS client sends an HTTP request to a FIMS service and then the FIMS service will send an HTTP response back. Optionally the HTTP request can contain a `<bms:replyTo>` and/or `<bms:faultTo>` nodes, which indicate endpoints that the FIMS service should use when it needs to send asynchronously messages back to the client.

The FIMS Test Suite is built such that it can intercept such messages and perform operations on them. It is capable of doing XML Schema validation of the messages that are passing by, as well it can write these (XML) messages in pretty print format to a text file.

## Requirements
For successful compilation and running of this test tool Java SDK v7+ is required to be installed.

## Usage

- Windows
  - Execute `fimstestsuite.bat` from windows explorer or command prompt
- Linux / MacOS
  - Open terminal and change working directory to the project folder then execute : `$ ./gradlew run`

In order to use this application you ideally have already a FIMS client and a FIMS service communicating with each other. It is however possible to test this application by simply logging HTTP traffic. The key concept is that the FIMS Test Suite works as a 'proxy' server. The FIMS client will not send messages directly to the FIMS service, but to the FIMS Test Suite.The FIMS Test Suite will redirect the messages to the FIMS service. Responses from the FIMS Service are in turn sent to the FIMS Test Suite and redirected back again to the FIMS client. This way the FIMS Test Suite will see all the messages being sent between the client and service.

The application is built with a modular approach, so it will be easy to add additional modules in the future. Now there are three modules available. The 'Proxy' module, the 'Logging' module and the Validation module. The Proxy module takes care of rerouting the messages coming from the FIMS client to the FIMS service and back. The Logging module will take care of writing the messages that pass by to a log file. In the Validation Module you can configure which XML schema you want to use for validation and Start and Stop the validation process.

##### Configuring the Proxy module
- FIMS Service IP Address: Set it to the IP address or Host name of the FIMS service.
- FIMS Service Port Number: Set it to the port number which the FIMS service uses.
- Test Suite IP Address: Set it to the IP address or Host name how the FIMS service can reach the machine with FIMS Test Suit running. This address will be used by FIMS service to send the asynchronous messages back to the client.
- Test Suite Port Number: Set it to any available port number. This port number must be used by the FIMS client to communicate with the proxy.

Click Connect to start the proxy.

##### Configuring the Logging module
Enable the logging method that you want to use and press Start to start logging. 

#### Configuring the Validation module
Select the XML Schema that you want to use for validation e.g. select captureMedia.xsd from the FIMS github repository to validate the communication to and from a capture service. Press Start to start logging. Open the Monitor window from the Window menu to see the validation in progress.

##### Configuring your FIMS client
It's important that your FIMS client is being configured to communicate to the FIMS Test Suite instead of the original FIMS service. Use the Test Suite IP Address and Port Number that you have provided in the Proxy configuration.

## Future work
We are planning to expand the FIMS Test Suite with more modules. Currently there is work in progress for a more advanced validation module, that will be capable of validating the messages for FIMS compliance beyond XML Schema validation. 

