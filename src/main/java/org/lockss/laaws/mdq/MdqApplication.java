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
package org.lockss.laaws.mdq;

import org.lockss.app.LockssApp.AppSpec;
import org.lockss.app.LockssApp.ManagerDesc;
import org.lockss.app.LockssDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.UrlPathHelper;

/**
 * The Spring-Boot application.
 */
@SpringBootApplication
public class MdqApplication extends WebMvcConfigurerAdapter implements CommandLineRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(MdqApplication.class);

  protected static final ManagerDesc[] myManagerDescs = {
      new ManagerDesc(LockssDaemon.ACCOUNT_MANAGER,
		      "org.lockss.account.AccountManager"),
      // start plugin manager after generic services
      new ManagerDesc(LockssDaemon.PLUGIN_MANAGER,
		      "org.lockss.plugin.PluginManager"),
      // start database manager before any manager that uses it.
      new ManagerDesc(LockssDaemon.METADATA_DB_MANAGER,
		      "org.lockss.metadata.MetadataDbManager"),
      // start metadata manager after pluggin manager and database manager.
      new ManagerDesc(LockssDaemon.METADATA_MANAGER,
		      "org.lockss.metadata.MetadataManager"),
      // Start the COUNTER reports manager.
      new ManagerDesc(LockssDaemon.COUNTER_REPORTS_MANAGER,
		      "org.lockss.exporter.counter.CounterReportsManager"),
      // Start the job manager.
      new ManagerDesc(LockssDaemon.JOB_MANAGER,
		      "org.lockss.job.JobManager"),
      // Start the job database manager.
      new ManagerDesc(LockssDaemon.JOB_DB_MANAGER,
		      "org.lockss.job.JobDbManager"),
      // NOTE: Any managers that are needed to decide whether a servlet is to be
      // enabled or not (through ServletDescr.isEnabled()) need to appear before
      // the AdminServletManager on the next line.
      new ManagerDesc(LockssDaemon.SERVLET_MANAGER,
		      "org.lockss.servlet.AdminServletManager"),
      new ManagerDesc(LockssDaemon.PROXY_MANAGER,
		      "org.lockss.proxy.ProxyManager")
  };

  /**
   * The entry point of the application.
   *
   * @param args A String[] with the command line arguments.
   */
  public static void main(String[] args) {
    logger.info("Starting the application");
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
    // Start the REST service.
    SpringApplication.run(MdqApplication.class, args);
  }

  /**
   * Starts the LOCKSS daemon.
   */
  public void run(String... args) {
    // Check whether there are command line arguments available.
    if (args != null && args.length > 0) {
      // Yes: Start the LOCKSS daemon.
      logger.info("Starting the LOCKSS daemon");

//       LaawsMdqApp.main(args);

      AppSpec spec = new AppSpec()
	.setName("Metadata Query Service")
	.setArgs(args)
	.setAppManagers(myManagerDescs);
      LockssDaemon.startStatic(LockssDaemon.class, spec);
    } else {
      // No: Do nothing. This happens when a test is started and before the
      // test setup has got a chance to inject the appropriate command line
      // parameters.
    }
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
      UrlPathHelper urlPathHelper = new UrlPathHelper();
      urlPathHelper.setUrlDecode(false);
      configurer.setUrlPathHelper(urlPathHelper);
  }
}
