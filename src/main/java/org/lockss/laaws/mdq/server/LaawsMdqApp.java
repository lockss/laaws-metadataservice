/*

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

 */
package org.lockss.laaws.mdq.server;

import static org.lockss.app.LockssDaemon.*;
import org.lockss.app.LockssDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup code.
 */
//@Component
public class LaawsMdqApp {
  private static final Logger log = LoggerFactory.getLogger(LaawsMdqApp.class);

  // Manager descriptors.  The order of this table determines the order in
  // which managers are initialized and started.
  protected static final ManagerDesc[] myManagerDescs = {
      new ManagerDesc(ACCOUNT_MANAGER, "org.lockss.account.AccountManager"),
      // start plugin manager after generic services
      new ManagerDesc(PLUGIN_MANAGER, "org.lockss.plugin.PluginManager"),
      // start database manager before any manager that uses it.
      new ManagerDesc(METADATA_DB_MANAGER,
	  "org.lockss.metadata.MetadataDbManager"),
      // start metadata manager after pluggin manager and database manager.
      new ManagerDesc(METADATA_MANAGER,
	  "org.lockss.metadata.MetadataManager"),
      // Start the COUNTER reports manager.
      new ManagerDesc(COUNTER_REPORTS_MANAGER,
	  "org.lockss.exporter.counter.CounterReportsManager"),
      // Start the job manager.
      new ManagerDesc(JOB_MANAGER, "org.lockss.job.JobManager"),
      // Start the job database manager.
      new ManagerDesc(JOB_DB_MANAGER, "org.lockss.job.JobDbManager"),
      // NOTE: Any managers that are needed to decide whether a servlet is to be
      // enabled or not (through ServletDescr.isEnabled()) need to appear before
      // the AdminServletManager on the next line.
      new ManagerDesc(SERVLET_MANAGER,
	  "org.lockss.servlet.AdminServletManager"),
      new ManagerDesc(PROXY_MANAGER, "org.lockss.proxy.ProxyManager")
  };

  public static void main(String[] args) {
    AppSpec spec = new AppSpec()
      .setName("Metadata Query Service")
      .setArgs(args)
//       .addAppConfig(PluginManager.PARAM_START_ALL_AUS, "true")
      .setAppManagers(myManagerDescs);
    LockssDaemon.startStatic(LockssDaemon.class, spec);
  }

  public LaawsMdqApp() throws Exception {
    super();
  }
}
