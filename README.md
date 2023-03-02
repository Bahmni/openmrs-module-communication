openmrs-module-communication
==========================
OpenMRS Module for Sending Mails

Description
-----------
This module is supporting [bahmni-core](https://github.com/Bahmni/bahmni-core) module for sending emails

## Build

[![OpenmrsModuleCommunication-master Actions Status](https://github.com/Bahmni/openmrs-module-communication/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/Bahmni/openmrs-module-communication/actions)

### Prerequisite
    JDK 1.8

### Clone the repository and build the omod

    git clone https://github.com/bahmni/openmrs-module-communication
    cd openmrs-module-communication
    mvn clean install

## Deploy

Copy ```openmrs-module-communication/omod/target/communication-VERSION-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS
