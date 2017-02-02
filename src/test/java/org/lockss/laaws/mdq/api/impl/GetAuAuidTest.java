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

//import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;
//import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;

@RunWith(Arquillian.class)
public class GetAuAuidTest {

  @Deployment
  public static Archive<?> createDeployment() throws Exception {
    System.out.println("GetAuAuidTest.createDeployment() invoked");
    JARArchive archive = ShrinkWrap.create(JARArchive.class);
//    JARArchive archive = ShrinkWrap.create(MavenImporter.class)
//    .loadPomFromFile("pom.xml").importBuildOutput().as(JARArchive.class);

//    JARArchive archive = ShrinkWrap.createFromZipFile(JARArchive.class,
//	new File("target/laaws-metadata-service-swarm.jar"));
//    JARArchive archive = ShrinkWrap.create(JARArchive.class)
//	.merge(ShrinkWrap.createFromZipFile(JARArchive.class,
//	    new File("target/laaws-metadata-service-swarm.jar")));

    System.out.println("GetAuAuidTest.createDeployment() archive = "
	+ archive.toString(true));
    return archive;
  }

  @Test
  public void test1() {
    System.out.println("GetAuAuidTest.test1() invoked");
    System.out.println("GetAuAuidTest.test1() done");
  }
}
