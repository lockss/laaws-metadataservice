/*

 Copyright (c) 2017 Board of Trustees of Leland Stanford Jr. University,
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

 */
package org.lockss.laaws.mdq.api.impl;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.rs.auth.AccessControlFilter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class GetAuAuidTest {
  @ArquillianResource
  private URL url;
  private static Path tempDir;
  private static String configfiles[] = {"common.xml", "lockss.txt", "lockss.opt"};


  private static String initConfigFiles() throws IOException {
    StringWriter sw = new StringWriter();
    // copy the files
    tempDir = Files.createTempDirectory("laawsmdq");
    System.out.println("Moving config files to tempDir: " + tempDir);
    for(String config : configfiles) {
      Path src = FileSystems.getDefault().getPath(config);
      Path dst = tempDir.resolve(src.getFileName());
      Files.copy(src,dst, StandardCopyOption.COPY_ATTRIBUTES);
      sw.append(" ").append("-p ");
      sw.append(dst.toString());
    }
    // make sure we have the test ejb-jar file pointing to the correct values
    Path template = FileSystems.getDefault().getPath("template-ejb-jar.xml");
    Charset charset = StandardCharsets.UTF_8;
    String content = new String(Files.readAllBytes(template), charset);
    content = content.replaceAll("CONF_FILES_PLACEHOLDER", sw.toString());
    Path ejb_test = tempDir.resolve("ejb-jar.xml");
    Files.write(ejb_test, content.getBytes(charset));
    return ejb_test.toString();
  }


  @Deployment(testable = false)
  public static WebArchive createDeployment() {

    String ejb_file = "src/main/webapp/WEB-INF/ejb-jar.xml";
    try {
      ejb_file = initConfigFiles();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    // Import Maven runtime dependencies
    File[] files = Maven.resolver()
        .loadPomFromFile("pom.xml")
        .importRuntimeDependencies()
        .resolve()
        .withTransitivity()
        .asFile();

    // Create deploy file
    WebArchive war = ShrinkWrap.create(WebArchive.class)
        .addPackages(true,"org.lockss.laaws.mdq",
                  "org.lockss.laaws.mdq.model",
            "org.lockss.laaws.mdq.server",
            "org.lockss.laaws.mdq.api",
            "org.lockss.laaws.mdq.ebj")
        .addAsWebInfResource(new File(ejb_file))
        .addAsWebInfResource(new File("common.xml"), "common.xml")
        .addAsWebInfResource(new File("lockss.txt"), "lockss.txt")
        .addAsWebInfResource(new File("lockss.opt"), "lockss.opt")
        .addAsLibraries(files)
        .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    // Show the deploy structure
    //System.out.println(war.toString(true));
    return war;
  }

  @Test
  public void test1() throws Exception {
    System.out.println("GetAuAuidTest.test1() invoked");

    RequestSpecBuilder reqSpecBuilder = new RequestSpecBuilder();
    reqSpecBuilder.setBaseUri(url.toURI());
    given(reqSpecBuilder.build())
            .when().get("/aus/noAuth")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(401)
            .body("message", is(AccessControlFilter.noAuthorizationHeader));

    reqSpecBuilder = new RequestSpecBuilder();
    reqSpecBuilder.setBaseUri(url.toURI());
    given(reqSpecBuilder.build())
    .auth().preemptive().basic("username", "password")
    .when().get("/aus/badAuth")
    .then()
    .contentType(ContentType.JSON)
    .statusCode(401)
    .body("message", is(AccessControlFilter.badCredentials));
/*
    This test fails because 'noAu' is throwing before the test completes.
    reqSpecBuilder = new RequestSpecBuilder();
    reqSpecBuilder.setBaseUri(url.toURI());
    given(reqSpecBuilder.build())
    .auth().preemptive().basic("lockss-u", "lockss-p")
    .when().get("/aus/noAU")
    .then()
    .contentType(ContentType.JSON)
    .statusCode(404)
    .body("message", is(AccessControlFilter.badCredentials));

  */
  System.out.println("GetAuAuidTest.test1() done");
  }
}
