<!--

Copyright (c) 2000-2018 Board of Trustees of Leland Stanford Jr. University,
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
`git clone ssh://git@gitlab.lockss.org/laaws/laaws-metadataservice.git`

### Create the Eclipse project (if so desired)
`File` -> `Import...` -> `Maven` -> `Existing Maven Projects`

### Specify the Repository
This web service requires an external Repository to provide an indication of
whether a URL is cached or not.

To specify the properties of such external Repository, edit in the file
`config/lockss.txt` the following options and specify the appropriate values:

`# default to local repo.`
`org.lockss.repository.v2Repository=local:demorepo:/tmp/locksslocalrepo`
`# Put this in lockss.opt to use a REST repositoy service`
`# org.lockss.repository.v2Repository=rest:demorepo:http://localhost:32640`

### Optional Configuration REST web service
The default configuration of this web service does not require that a
Configuration REST web service is running.

To run this web service with a Configuration REST web service, edit the file
`./runLaawsMdq` to comment out the line

mvn spring-boot:run -Drun.arguments="-p,config/common.xml,-p,config/lockss.txt,-p,config/lockss.opt"

and remove the comment from the last line of the script, like this:

mvn spring-boot:run -Drun.arguments="-c,http://lockss-u:lockss-p@localhost:54420,-p,http://localhost:54420/config/file/cluster,-p,config/common.xml,-p,config/lockss.txt,-p,config/lockss.opt"

To run this web service with a Configuration REST web service at a different
location than the default, change `localhost` and/or `54420` accordingly.

### Build the web service:
`./buildLaawsMdq`

This will run the tests as a pre-requisite for the build.

The result of the build is a so-called "uber JAR" file which includes the
project code plus all its dependencies and which is located at

`./target/laaws-metadata-service-*.jar`

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

### The status of the web service may be obtained at:
#### http://localhost:49520/status

### Stop the web service:
`./stopLaawsMdq`
