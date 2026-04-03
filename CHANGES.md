# `laaws-metadata-service` Release Notes

## 2.8.0 (LOCKSS 2.0.91-beta2)

### Features

* Add roles checking in service endpoints
* Revisit LockssApp startup in tests
* Updated PageInfo spec to mark some properties as nullable; renamed `resultsPerPage` to `itemsInPage`
* Fixed PageInfo imports
* Patch OpenAPI spec for swagger-codegen Spring code generation
* Adopt 2.0-beta2 port conventions
* Define `build.java.projectAbbr` in POM
* Merge MDQ and MDX services into a single metadata service


## Changes Since 2.3.0
* Remove  Travis CI
* Move to OpenAPI 3
* Move to Java 17
* Suppress extraneous messages in tests
* Spring 6.x and Spring Boot 3.x related changes


## Changes Since 2.0.2.1

*   Switched to a 3-part version numbering scheme.

## 2.0.2.1

### Security

*   Out of an abundance of caution, re-released 2.0.2.0 with Jackson-Databind 2.9.10.8 (CVE-2021-20190).

## 2.0.2.0

### Features

*   ...

### Fixes

*   ...

## 2.0.1.0

### Features

*   REST services authenticate, clients provide credentials.
*   Improved startup coordination and ready waiting of all services and databases.
