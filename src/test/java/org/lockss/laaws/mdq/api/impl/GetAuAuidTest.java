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

//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.is;
//import io.restassured.builder.RequestSpecBuilder;
//import io.restassured.http.ContentType;
import java.io.File;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
//import org.lockss.rs.auth.AccessControlFilter;

@RunWith(Arquillian.class)
public class GetAuAuidTest {
  @ArquillianResource
  private URL url;

  @Deployment(testable = false)
  public static WebArchive createDeployment() throws Exception {
    String cwd = new File(".").getCanonicalPath();
    System.out.println("cwd = " + cwd);
    WebArchive wa = ShrinkWrap.create(WebArchive.class);

//    wa.addAsLibrary(new File("lockss-daemon/lib/lockss.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/xercesImpl-2.7.1.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/commons-collections-3.2.2.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/jakarta-oro-2.0.8.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/commons-lang3-3.4.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/derby.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/commons-httpclient-3.0-rc4.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/xstream-1.1.3.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/jetty-5.1.5L.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/commons-io-2.4.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/gettext-commons-0.9.6.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/htmlparser-1.6p.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/derbyclient.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/postgresql-jdbc4-9.1.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/mysql-connector-java-5.1.29-bin.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/castor-0.9.4.1-xml.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/commons-beanutils-1.9.2.jar"));
//    wa.addAsLibrary(new File("lockss-daemon/lib/derbynet.jar"));
//    wa.addPackages(true, "org.lockss.laaws.mdq.api"
//	,"org.lockss.laaws.mdq.model"
//	,"org.lockss.laaws.mdq.server"
//	,"org.lockss.laaws.mdq.ejb"
//	)
//    .addClass(GetAuAuidTest.class)
//    .addAsWebInfResource(new File("src/main/webapp/WEB-INF/ejb-jar.xml"))
//    //.addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-deployment-structure.xml"))
//    //.addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"))
//    //.addAsManifestResource(new File("src/main/webapp/META-INF/MANIFEST.MF"))
//    .addAsWebInfResource(new File("common.xml"), "classes/common.xml")
//    .addAsWebInfResource(new File("lockss.txt"), "classes/lockss.txt")
//    .addAsWebInfResource(new File("lockss.opt"), "classes/lockss.opt")
//    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
//
//    System.out.println("wa = " + wa.toString(true));
    return wa;
  }

  @Test
  public void test1() throws Exception {
    System.out.println("GetAuAuidTest.test1() invoked");
//    RequestSpecBuilder reqSpecBuilder = new RequestSpecBuilder();
//    reqSpecBuilder.setBaseUri(url.toURI());
//    given(reqSpecBuilder.build())
//            .when().get("/aus/noAuth")
//            .then()
//            .contentType(ContentType.JSON)
//            .statusCode(401)
//            .body("message", is(AccessControlFilter.noAuthorizationHeader));
//
//    reqSpecBuilder = new RequestSpecBuilder();
//    reqSpecBuilder.setBaseUri(url.toURI());
//    given(reqSpecBuilder.build())
//    .auth().preemptive().basic("username", "password")
//    .when().get("/aus/badAuth")
//    .then()
//    .contentType(ContentType.JSON)
//    .statusCode(401)
//    .body("message", is(AccessControlFilter.badCredentials));
//
//    reqSpecBuilder = new RequestSpecBuilder();
//    reqSpecBuilder.setBaseUri(url.toURI());
//    given(reqSpecBuilder.build())
//    .auth().preemptive().basic("lockss-u", "lockss-p")
//    .when().get("/aus/noAU")
//    .then()
//    .contentType(ContentType.JSON)
//    .statusCode(404)
//    .body("message", is(AccessControlFilter.badCredentials));

    System.out.println("GetAuAuidTest.test1() done");
  }
}
