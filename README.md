<!--
Copyright (c) 2016-2017 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.
--> 
# laaws-metadata-query [![Build Status](https://travis-ci.org/lockss/laaws-mdq.svg?branch=master)](https://travis-ci.org/lockss/laaws-mdq)
The wrapper around the Metadata Query Service.

### Clone the repo
`git clone --recursive https://github.com/lockss/laaws-mdq.git`

### Create the Eclipse project (if so desired)
File -> Import... -> Maven -> Existing Maven Projects

### Build and install the required LOCKSS daemon jar files:
run `initBuild`

### Build the web service:
`./buildLaawsMdq`

This will use port 8888 during the build. To use, for example, port 8889,
instead, either edit the value of $service_port in ./buildLaawsMdq or run:

`./buildLaawsMdq 8889`

The result of the build is a so-called "uber JAR" file which includes the
project code plus all its dependencies and which is located at

`./target/laaws-metadata-service-swarm.jar`

### Run the web service:
`./runLaawsMdq`

This will listen to port 8888. To use, for example, port 8889, instead, either
edit the value of $service_port in ./runLaawsMdq or run:

`./runLaawsMdq 8889`

The log is at ./logs/laawsmdq.log

### Build and run the web service:
`./buildAndRunLaawsMdq`

This will use port 8888 for both steps. To use, for example, port 8889, instead,
either edit the value of $service_port in ./buildAndRunLaawsMdq or run:

`./buildAndRunLaawsMdq 8889`

### Stop the web service:
`./stopLaawsMdq`

### API is documented at:
#### localhost:8888/docs/

### Getting Archival Unit information from a web service, not the repository
In ./lockss.opt add the following option:

org.lockss.openUrlResolver.urlCacheFromWs=true

To specify the properties of the web service used to get an indication of
wheter a URL is cached or not, in ./lockss.opt add the following options with
the appropriate values:

org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.addressLocation=http://localhost:8081/ws/ContentService?wsdl
org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.password=the-correct-password
org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.serviceName=ContentServiceImplService
org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.targetNameSpace=http://content.ws.lockss.org/
org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.timeoutValue=600
org.lockss.openUrlResolver.urlCacheFromWs.isUrlCachedWs.userName=the-correct-user
