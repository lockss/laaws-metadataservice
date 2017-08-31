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
`git clone --recursive ssh://git@gitlab.lockss.org/laaws/laaws-metadataservice.git`

### Create the Eclipse project (if so desired)
File -> Import... -> Maven -> Existing Maven Projects

### Build and install the required LOCKSS daemon jar files:
run `initBuild`

### Specify the Repository REST web service
This web service requires that an external Repository REST web service is
running so as to provide an indication of whether a URL is cached or not.

To specify the properties of such external REST web service, edit in
config/lockss.txt the following options and specify the appropriate values:

org.lockss.plugin.auContentFromWs.urlArtifactWs.password=the-correct-password
org.lockss.plugin.auContentFromWs.urlArtifactWs.restServiceLocation=http://localhost:the-correct-port/repos/demorepo/artifacts?committed=false&uri={uri}
org.lockss.plugin.auContentFromWs.urlArtifactWs.timeoutValue=600
org.lockss.plugin.auContentFromWs.urlArtifactWs.userName=the-correct-user

### Build the web service:
`./buildLaawsMdq`

This will run the tests as a pre-requisite for the build.

The result of the build is a so-called "uber JAR" file which includes the
project code plus all its dependencies and which is located at

`./target/laaws-metadata-service-0.0.1-SNAPSHOT.jar`

### Run the web service:
`./runLaawsMdq`

This will use port 49520. To use another port, edit the value of the
`server.port` property in file
`src/main/resources/application.properties`.

The log is at ./logs/laawsmdq.log

### Build and run the web service:
`./buildAndRunLaawsMdq`

This will use port 49520. To use another port, edit the value of the
`server.port` property in file
`src/main/resources/application.properties`.

### API is documented at:
#### http://localhost:49520/swagger-ui.html

### Stop the web service:
`./stopLaawsMdq`
