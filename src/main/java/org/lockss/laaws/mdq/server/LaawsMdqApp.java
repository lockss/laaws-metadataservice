/*

 Copyright (c) 2016 Board of Trustees of Leland Stanford Jr. University,
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
package org.lockss.laaws.mdq.server;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import io.swagger.jaxrs.config.BeanConfig;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.lockss.app.LockssDaemon;
import org.lockss.config.CurrentConfig;
import org.lockss.daemon.ResourceUnavailableException;
import org.lockss.db.DbManager;
import org.lockss.metadata.MetadataManager;
import org.lockss.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Class for starting the Entity Browser
 */
public class LaawsMdqApp extends LockssDaemon {
  private static final Logger LOG = LoggerFactory.getLogger(LaawsMdqApp.class);

  public static String LAAWS_MDQ_SERVER_HOST ="LAAWS_MDQ_SERVER_HOST";
  public static String LAAWS_MDQ_SERVER_PORT = "LAAWS_MDQ_SERVER_PORT";
  private static final int DEFAULT_SERVER_PORT = 8888;
  private static final String DEFAULT_SERVER_HOST = "http://localhost";

  // Manager descriptors.  The order of this table determines the order in
  // which managers are initialized and started.
  protected final ManagerDesc[] managerDescs = {
      new ManagerDesc(RESOURCE_MANAGER, DEFAULT_RESOURCE_MANAGER),
      new ManagerDesc(STATUS_SERVICE, DEFAULT_STATUS_SERVICE),
      // keystore manager must be started before any others that need to
      // access managed keystores
      new ManagerDesc(KEYSTORE_MANAGER,
	  "org.lockss.daemon.LockssKeyStoreManager"),
      new ManagerDesc(ACCOUNT_MANAGER, "org.lockss.account.AccountManager"),
      // start plugin manager after generic services
      new ManagerDesc(PLUGIN_MANAGER, "org.lockss.plugin.PluginManager"),
      // start database manager before any manager that uses it.
      new ManagerDesc(DbManager.getManagerKey(), "org.lockss.db.DbManager"),
      // start metadata manager after pluggin manager and database manager.
      new ManagerDesc(MetadataManager.getManagerKey(),
	  "org.lockss.metadata.MetadataManager"),
      // NOTE: Any managers that are needed to decide whether a servlet is to be
      // enabled or not (through ServletDescr.isEnabled()) need to appear before
      // the AdminServletManager on the next line.
      new ManagerDesc(SERVLET_MANAGER, "org.lockss.servlet.AdminServletManager")
  };

  int serverPort;
  String serverHost;

  public static void main( String[] args ) {
	LaawsMdqApp laawsMdqApp;
    if (!SystemUtils.isJavaVersionAtLeast(MIN_JAVA_VERSION)) {
      System.err.println("LOCKSS requires at least Java " + MIN_JAVA_VERSION +
                         ", this is " + SystemUtils.JAVA_VERSION +
                         ", exiting.");
      System.exit(Constants.EXIT_CODE_JAVA_VERSION);
    }

    StartupOptions opts = getStartupOptions(args);
    setSystemProperties();

    try {
      laawsMdqApp = new LaawsMdqApp(opts.getPropUrls(), opts.getGroupNames());
      laawsMdqApp.startDaemon();
      // raise priority after starting other threads, so we won't get
      // locked out and fail to exit when told.
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);

    } catch (ResourceUnavailableException e) {
      LOG.error("Exiting because required resource is unavailable", e);
      System.exit(Constants.EXIT_CODE_RESOURCE_UNAVAILABLE);
      return;                           // compiler doesn't know that
                                        // System.exit() doesn't return
    } catch (Throwable e) {
      LOG.error("Exception thrown in main loop", e);
      System.exit(Constants.EXIT_CODE_EXCEPTION_IN_MAIN);
      return;                           // compiler doesn't know that
                                        // System.exit() doesn't return
    }
    if (CurrentConfig.getBooleanParam(PARAM_APP_EXIT_IMM,
                                      DEFAULT_APP_EXIT_IMM)) {
      try {
	laawsMdqApp.stop();
      } catch (RuntimeException e) {
        // ignore errors stopping daemon
      }
      System.exit(Constants.EXIT_CODE_NORMAL);
    }
    laawsMdqApp.keepRunning();
    LOG.info("Exiting because time to die");
    System.exit(Constants.EXIT_CODE_NORMAL);
  }

  public LaawsMdqApp(List<String> propUrls, String groupNames)
		  throws Exception {
    super(propUrls, groupNames);

    serverHost = System.getProperty(LAAWS_MDQ_SERVER_HOST, DEFAULT_SERVER_HOST);
    serverPort = Integer.getInteger(System.getProperty(LAAWS_MDQ_SERVER_PORT),
	DEFAULT_SERVER_PORT);

    // Build the Swagger data
    buildSwagger(serverHost+":"+serverPort);
    Server server = configureServer();
    server.start();
  }

  protected Server configureServer() throws Exception {
    Resource.setDefaultUseCaches( false );

    URI baseUri = UriBuilder.fromUri(serverHost).port(serverPort).build();

    HandlerList handlers = initHandlers(baseUri);

    // configure server
    Server server = new Server(serverPort);
    server.setHandler( handlers );
    return server;
  }

  protected static void buildSwagger(String host) {
    // This configures Swagger
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion( "1.0.2" );
    beanConfig.setResourcePackage("org.lockss.laaws.mdq.api");

    beanConfig.setDescription( "Metadata Query Service API" );
    beanConfig.setTitle( "Metadata Query Service" );

    beanConfig.setSchemes(new String[]{"http"});
    //beanConfig.setHost(host);
//    beanConfig.setBasePath("/docs");
    beanConfig.setScan(true);
  }

  protected static HandlerList initHandlers(URI baseUri) throws Exception {
    // init handlers
    final HandlerList handlers = new HandlerList();

    // Handler for swagger UI, static handler.
    ContextHandler swaggerUIContextHandler = buildSwaggerUIContextHandler();
    handlers.addHandler(swaggerUIContextHandler);

    // Handler for load jersey resources.
    ContextHandler resourceContextHandler = buildResourceContextHandler();
    handlers.addHandler(resourceContextHandler);
    return handlers;
  }

  private static ContextHandler buildResourceContextHandler() {
    ResourceConfig resourceConfig = new ResourceConfig();

    resourceConfig.packages("org.lockss.laaws.mdq.model",
                            "org.lockss.laaws.mdq.api",
                            "io.swagger.jaxrs.listing");
    resourceConfig.register(JacksonFeature.class);
    resourceConfig.register(AccessControlFilter.class);
    //resourceConfig.register(NotFoundException.class);

    ServletContainer servletContainer = new ServletContainer(resourceConfig);
    ServletHolder servletHolder = new ServletHolder(servletContainer);

    servletHolder.setInitOrder(1);
    servletHolder.setInitParameter("jersey.config.server.tracing", "ALL");

    ServletContextHandler servletContextHandler =
      new ServletContextHandler(ServletContextHandler.SESSIONS);
    servletContextHandler.setSessionHandler(new SessionHandler());
    servletContextHandler.setContextPath("/");

    servletContextHandler.addServlet(servletHolder, "/*");

    return servletContextHandler;
  }

  private static ContextHandler buildSwaggerUIContextHandler() throws Exception {
    final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
    swaggerUIResourceHandler.setResourceBase(
      LaawsMdqApp.class.getClassLoader().getResource("swaggerui").toURI()
      .toString());
    ContextHandler swaggerUIContextHandler = new ContextHandler();
    swaggerUIContextHandler.setContextPath("/docs/");
    swaggerUIContextHandler.setHandler(swaggerUIResourceHandler);

    return swaggerUIContextHandler;
  }

  /**
   * Provides the manager's descriptors.
   * 
   * @return a ManagerDesc[] with the application manager's descriptors.
   */
  protected ManagerDesc[] getManagerDescs() {
    return managerDescs;
  }
}