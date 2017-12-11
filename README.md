<!--

Copyright (c) 2000-2017 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--> 
# laaws-metadata-query [![Build Status](https://travis-ci.org/lockss/laaws-mdq.svg?branch=master)](https://travis-ci.org/lockss/laaws-mdq)
The wrapper around the Metadata Query Service.

### Clone the repo
`git clone --recursive ssh://git@gitlab.lockss.org/laaws/laaws-metadataservice.git`

### Create the Eclipse project (if so desired)
`File` -> `Import...` -> `Maven` -> `Existing Maven Projects`

### Specify the Repository REST web service
This web service requires that an external Repository REST web service is
running so as to provide an indication of whether a URL is cached or not.

To specify the properties of such external REST web service, edit in
`config/lockss.txt` the following options and specify the appropriate values:

org.lockss.plugin.auContentFromWs.urlArtifactWs.password=the-correct-password
org.lockss.plugin.auContentFromWs.urlArtifactWs.restServiceLocation=http://localhost:the-correct-port/repos/demorepo/artifacts?committed=false&uri={uri}
org.lockss.plugin.auContentFromWs.urlArtifactWs.timeoutValue=600
org.lockss.plugin.auContentFromWs.urlArtifactWs.userName=the-correct-user

### Optional Configuration REST web service
The default configuration of this web service requires that a Configuration REST
web service is running. The specification of this Configuration REST web service
is in the script

`./runLaawsMdq`

To run this web service without a Configuration REST web service, remove

`-c,http://lockss-u:lockss-p@localhost:54420,-p,http://localhost:54420/config/file/cluster,`

from the script.

To run this web service with a Configuration REST web service at a different
location than the default, change `localhost` and/or `54420` accordingly.

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

The log is at `./logs/mdq.log`

### Build and run the web service:
`./buildAndRunLaawsMdq`

This will use port 49520. To use another port, edit the value of the
`server.port` property in file
`src/main/resources/application.properties`.

### API is documented at:
#### http://localhost:49520/swagger-ui.html

### Stop the web service:
`./stopLaawsMdq`
