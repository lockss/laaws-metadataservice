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
package org.lockss.laaws.mdq.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.config.ConfigManager;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.lockss.laaws.rs.model.ArtifactPage;
import org.lockss.test.LockssTestCase4;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * Test class for org.lockss.laaws.mdq.api.MetadataApiController and
 * org.lockss.laaws.mdq.api.UrlsApiController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiControllersTest extends LockssTestCase4 {
  private static final Logger logger =
      LoggerFactory.getLogger(ApiControllersTest.class);

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments to be
  // used for the tests.
  @Autowired
  ApplicationContext appCtx;

  // The path to the configuration file with the platform disk space location
  // definition.
  private String platformDiskSpaceConfigPath = null;

  // The indication of whether the external REST Repository service is
  // available.
  private static boolean isRestRepositoryServiceAvailable = false;

  // The identifier of an AU that exists in the test database.
  private String goodAuid = "org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2014&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F";

  // A DOI that exists in the test database.
  private String goodDoi = "10.3897/biorisk.9.6105";

  /**
   * Set up code to be run before all tests.
   */
  @BeforeClass
  public static void setUpBeforeAllTests() throws IOException {
    // Populate the indication of whether the external REST Repository service
    // is available.
    isRestRepositoryServiceAvailable = checkExternalRestService(
	"org.lockss.plugin.auContentFromWs.urlArtifactWs.restServiceLocation",
	new File("config/lockss.txt"),
	Collections.singletonMap("uri", "someDummyUri"));
  }

  /**
   * Provides the indication of whether an external REST service is available.
   * 
   * @param propertyName
   *          A String with the name of the configuration property with the REST
   *          service location template.
   * @param configFile
   *          A File with the configuration file containing the configuration
   *          property value.
   * @param uriMap
   *          A Map<String, String> with the map of values to be interpolated in
   *          the REST service location template.
   * @return a boolean with <code>true</code> if the external REST service is
   *         available, <code> false</code> otherwise.
   */
  private static boolean checkExternalRestService(String propertyName,
      File configFile, Map<String, String> uriMap) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("propertyName = " + propertyName);
      logger.debug("configFile = " + configFile.getAbsolutePath());
      logger.debug("uriMap = " + uriMap);
    }

    boolean isServiceAvailable = false;
    FileInputStream is = null;
    String restServiceLocation = null;

    // Get the REST service location.
    try {
      is = new FileInputStream(configFile);
      Properties properties = new Properties();
      properties.load(is);
      restServiceLocation = properties.getProperty(propertyName);
      if (logger.isDebugEnabled())
	logger.debug("restServiceLocation = " + restServiceLocation);
    } finally {
      try {
	is.close();
      } catch (IOException ioe) {
      }
    }

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    // Initialize the request headers.
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder
	.fromUriString(restServiceLocation).build().expand(uriMap);

    URI uri = UriComponentsBuilder.newInstance()
	.uriComponents(uriComponents).build().encode().toUri();
    if (logger.isDebugEnabled())
	logger.debug("Making request to '" + uri + "'...");

    // Make the request to the REST service and get its response.
    try {
      ResponseEntity<ArtifactPage> result = restTemplate.exchange(uri,
	  HttpMethod.GET, new HttpEntity<String>(null, headers),
	  ArtifactPage.class);

      int statusCode = result.getStatusCodeValue();
      if (logger.isDebugEnabled())
	logger.debug("Done: statusCode = " + statusCode);

      isServiceAvailable = statusCode == 200;
    } catch (Exception e) {
      if (logger.isDebugEnabled()) logger.debug("Done: No REST service.");
    }

    if (logger.isDebugEnabled())
      logger.debug("isServiceAvailable = " + isServiceAvailable);
    return isServiceAvailable;
  }

  /**
   * Set up code to be run before each test.
   * 
   * @throws IOException if there are problems.
   */
  @Before
  public void setUpBeforeEachTest() throws IOException {
    if (logger.isDebugEnabled()) logger.debug("port = " + port);

    // Get the path of a temporary directory where the test data will reside.
    String tempDirPath = getTempDir(ApiControllersTest.class.getCanonicalName())
	.getAbsolutePath();
    if (logger.isDebugEnabled()) logger.debug("tempDirPath = " + tempDirPath);

    // Copy the necessary files to the test temporary directory.
    File srcTree = new File("test/cache");
    if (logger.isDebugEnabled())
      logger.debug("srcTree = " + srcTree.getAbsolutePath());

    File destTree = new File(new File(tempDirPath), "cache");
    if (logger.isDebugEnabled())
      logger.debug("destTree = " + destTree.getAbsolutePath());

    FileSystemUtils.copyRecursively(srcTree, destTree);

    // Create a file that will communicate to the test REST service where its
    // data is located.
    createPlatformDiskSpaceConfigFile(tempDirPath);
  }

  /**
   * Creates a file that will communicate to the test REST service where its
   * data is located.
   *
   * @param dirPath
   *          A String with the path to the directory where the file is to be
   *          created.
   * @throws IOException
   *           if there are problems.
   */
  private void createPlatformDiskSpaceConfigFile(String dirPath)
      throws IOException {
    if (logger.isDebugEnabled()) logger.debug("dirPath = " + dirPath);

    // The configuration option with the temporary directory where the test data
    // resides.
    String platformDiskSpaceConfigParam =
	ConfigManager.PARAM_PLATFORM_DISK_SPACE_LIST + "=" + dirPath + "/cache"
	    + System.lineSeparator();
    if (logger.isDebugEnabled()) logger.debug("platformDiskSpaceConfigParam = '"
	+ platformDiskSpaceConfigParam +"'.");

    // The path to the file.
    platformDiskSpaceConfigPath = dirPath + "/platform.txt";

    // Create the file.
    Files.write(Paths.get(platformDiskSpaceConfigPath),
	platformDiskSpaceConfigParam.getBytes(), StandardOpenOption.CREATE);
  }

  /**
   * Runs the tests with authentication turned off.
   * 
   * @throws Exception
   *           if there are problems.
   */
  @Test
  public void runUnAuthenticatedTests() throws Exception {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/apiControllerTestAuthOff.opt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    getSwaggerDocsTest();
    getMetadataAusAuidUnAuthenticatedTest();
    deleteMetadataAusAuidUnAuthenticatedTest();
    postMetadataAusItemUnAuthenticatedTest();
    getUrlsDoiUnAuthenticatedTest();
    getUrlsOpenUrlUnAuthenticatedTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the tests with authentication turned on.
   * 
   * @throws Exception
   *           if there are problems.
   */
  @Test
  public void runAuthenticatedTests() throws Exception {
    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/apiControllerTestAuthOn.opt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    getSwaggerDocsTest();
    getMetadataAusAuidAuthenticatedTest();
    deleteMetadataAusAuidAuthenticatedTest();
    postMetadataAusItemAuthenticatedTest();
    getUrlsDoiAuthenticatedTest();
    getUrlsOpenUrlAuthenticatedTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Provides the standard command line arguments to start the server.
   * 
   * @return a List<String> with the command line arguments.
   */
  private List<String> getCommandLineArguments() {
    List<String> cmdLineArgs = new ArrayList<String>();

    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add(platformDiskSpaceConfigPath);

    return cmdLineArgs;
  }

  /**
   * Runs the Swagger-related tests.
   * 
   * @throws Exception
   *           if there are problems.
   */
  private void getSwaggerDocsTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    ResponseEntity<String> successResponse = new TestRestTemplate().exchange(
	getTestUrlTemplate("/v2/api-docs"), HttpMethod.GET, null, String.class);

    HttpStatus statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    String expectedBody = "{'swagger':'2.0',"
	+ "'info':{'description':'API of Metadata Service for LAAWS'}}";

    JSONAssert.assertEquals(expectedBody, successResponse.getBody(), false);
    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related un-authenticated-specific tests.
   */
  private void getMetadataAusAuidUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate().exchange(uri, HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.GET, null, AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<AuMetadataPageInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    AuMetadataPageInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    AuMetadataPageInfo result = successResponse.getBody();

    assertEquals(1, result.getItems().size());
    assertEquals(new Integer(50), result.getPageInfo().getResultsPerPage());
    assertEquals(new Integer(1), result.getPageInfo().getCurrentPage());
    assertNull(result.getPageInfo().getTotalCount());

    getMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related authenticated-specific tests.
   */
  private void getMetadataAusAuidAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate().exchange(uri, HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.GET, null, AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<AuMetadataPageInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    AuMetadataPageInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    AuMetadataPageInfo result = successResponse.getBody();

    assertEquals(1, result.getItems().size());
    assertEquals(new Integer(50), result.getPageInfo().getResultsPerPage());
    assertEquals(new Integer(1), result.getPageInfo().getCurrentPage());
    assertNull(result.getPageInfo().getTotalCount());

    getMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related authentication-independent tests.
   */
  private void getMetadataAusAuidCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.GET, null, AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    String badUrl = getTestUrlTemplate("/metadata/aus/non-existent");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(badUrl, HttpMethod.GET, new HttpEntity<String>(null, headers),
	    AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.NOT_FOUND, statusCode);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related un-authenticated-specific tests.
   */
  private void deleteMetadataAusAuidUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(uri, HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Integer> successResponse =
	new TestRestTemplate().exchange(uri, HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    Integer result = successResponse.getBody();
    assertEquals(1, result.intValue());

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(null, headers),
	    Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(0, result.intValue());

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(null, headers),
	    Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(0, result.intValue());

    deleteMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related authenticated-specific tests.
   */
  private void deleteMetadataAusAuidAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(uri, HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate().exchange(uri, HttpMethod.DELETE,
	new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.DELETE, new HttpEntity<String>(null, headers),
	    String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<Integer> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.DELETE, new HttpEntity<String>(null, headers),
	    Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    Integer result = successResponse.getBody();
    assertEquals(1, result.intValue());

    deleteMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related authenticated-independent tests.
   */
  private void deleteMetadataAusAuidCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", goodAuid));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<String> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    String badUrl = getTestUrlTemplate("/metadata/aus/non-existent");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(badUrl, HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.NOT_FOUND, statusCode);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the postMetadataAusItem()-related un-authenticated-specific tests.
   */
  private void postMetadataAusItemUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String url = getTestUrlTemplate("/metadata/aus");

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(url, HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.POST, null, String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate().exchange(url, HttpMethod.POST,
	new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.POST, new HttpEntity<String>(null, headers),
	    String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);

    postMetadataAusItemCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the postMetadataAusItem()-related authenticated-specific tests.
   */
  private void postMetadataAusItemAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String url = getTestUrlTemplate("/metadata/aus");

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(url, HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.POST, null, String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate().exchange(url, HttpMethod.POST,
	new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.POST, new HttpEntity<String>(null, headers),
	    String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    postMetadataAusItemCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the postMetadataAusItem()-related authentication-independent tests.
   */
  private void postMetadataAusItemCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String url = getTestUrlTemplate("/metadata/aus");

    ResponseEntity<String> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(url, HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(url, HttpMethod.POST, new HttpEntity<String>(null, headers),
	    String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String metadataJsonValue = "{\"scalarMap\":{\"date\":\"2014-10-06\"," 
	+ "\"coverage\":\"fulltext\",\"volume\":\"9\","
	+ "\"publication_name\":\"BioRisk Volume 2014\","
	+ "\"publisher_name\":\"Pensoft Publishers\",\"start_page\":\"1\","
	+ "\"au_id\":\"org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
	+ "&au_oai_date~2014&au_oai_set~biorisk"
	+ "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F\","
	+ "\"provider_name\":\"Pensoft Publishers\",\"fetch_time\":\"-1\","
	+ "\"item_title\":\"China in the anthropocene: Culprit, victim or "
	+ "last best hope for a global ecological civilisation?\","
	+ "\"doi\":\"10.3897/biorisk.9.6105\"},"
	+ "\"listMap\":{\"proprietary_id\":[]},"
	+ "\"mapMap\":{\"issn\":{\"e_issn\":\"13132652\","
	+ "\"p_issn\":\"13132652\"},\"url\":{\"Abstract\":"
	+ "\"http://biorisk.pensoft.net/articles.php?id=1904\",\"Access\":"
	+ "\"http://biorisk.pensoft.net/articles.php?id=1904\""
	+ ",\"FullTextPdfFile\":\"http://biorisk.pensoft.net/lib/ajax_srv/"
	+ "article_elements_srv.php?action=download_pdf&item_id=1904\""
	+ ",\"ArticleMetadata\":\"http://biorisk.pensoft.net/"
	+ "articles.php?id=1904\"}}}";

    ItemMetadata metadata =
	new ObjectMapper().readValue(metadataJsonValue, ItemMetadata.class);

    ResponseEntity<Long> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(url,
	    HttpMethod.POST, new HttpEntity<ItemMetadata>(metadata, headers),
	    Long.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    Long result = successResponse.getBody();
    assertTrue(result.longValue() > 0);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related un-authenticated-specific tests.
   */
  private void getUrlsDoiUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/urls/doi/{doi}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("doi", goodDoi));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<UrlInfo> errorResponse = new TestRestTemplate().exchange(uri,
	HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.NOT_FOUND, statusCode);

    // Create the URI of the request to the REST service.
    uriComponents = UriComponentsBuilder.fromUriString(template).build()
	.expand(Collections.singletonMap("doi",
	    UriUtils.encodePathSegment(goodDoi, "UTF-8")));

    uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    errorResponse = new TestRestTemplate().exchange(uri, HttpMethod.GET, null,
	UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    getUrlsDoiCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related authenticated-specific tests.
   */
  private void getUrlsDoiAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/urls/doi/{doi}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("doi", goodDoi));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    ResponseEntity<UrlInfo> errorResponse = new TestRestTemplate().exchange(uri,
	HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(uri, HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.NOT_FOUND, statusCode);

    // Create the URI of the request to the REST service.
    uriComponents = UriComponentsBuilder.fromUriString(template).build()
	.expand(Collections.singletonMap("doi",
	    UriUtils.encodePathSegment(goodDoi, "UTF-8")));

    uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    getUrlsDoiCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related authentication-independent tests.
   */
  private void getUrlsDoiCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/urls/doi/{doi}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("doi",
	    UriUtils.encodePathSegment(goodDoi, "UTF-8")));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<UrlInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    UrlInfo.class);

    HttpStatus statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    UrlInfo result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertTrue(("info:doi/" + goodDoi)
	.startsWith(result.getParams().get("rft_id")));

    if (isRestRepositoryServiceAvailable) {
      assertEquals(1, result.getUrls().size());
    } else {
      assertEquals(0, result.getUrls().size());
    }

    String badUrl = getTestUrlTemplate("/urls/doi/non-existent");

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(badUrl, HttpMethod.GET, new HttpEntity<String>(null, headers),
	    UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertEquals("info:doi/non-existent", result.getParams().get("rft_id"));
    assertEquals(0, result.getUrls().size());

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related un-authenticated-specific tests.
   */
  private void getUrlsOpenUrlUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String url = getTestUrlTemplate("/urls/openurl");

    ResponseEntity<UrlInfo> errorResponse = new TestRestTemplate().exchange(url,
	HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    getUrlsOpenUrlCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related authenticated-specific tests.
   */
  private void getUrlsOpenUrlAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String url = getTestUrlTemplate("/urls/openurl");

    ResponseEntity<UrlInfo> errorResponse = new TestRestTemplate().exchange(url,
	HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(url, HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    getUrlsOpenUrlCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related authentication-independent tests.
   */
  private void getUrlsOpenUrlCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String template = getTestUrlTemplate("/urls/openurl");

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(template,
	    HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(template, HttpMethod.GET,
	    new HttpEntity<String>(null, headers), UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.BAD_REQUEST, statusCode);

    String param = "rft_id=info:doi/" + goodDoi;

    // Create the URI of the request to the REST service.
    URI uri = UriComponentsBuilder.fromHttpUrl(template)
	.queryParam("params", param).build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<UrlInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(uri,
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    UrlInfo result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertEquals(param, "rft_id=" + result.getParams().get("rft_id"));

    if (isRestRepositoryServiceAvailable) {
      assertEquals(1, result.getUrls().size());
    } else {
      assertEquals(0, result.getUrls().size());
    }

    String param1 = "rft.issn=1313-2652";
    String param2 = "rft.volume=9";
    String param3 = "rft.spage=1";

    uri = UriComponentsBuilder.fromHttpUrl(template)
	.queryParam("params", param1).queryParam("params", param2)
	.queryParam("params", param3).build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(uri, HttpMethod.GET, new HttpEntity<String>(null, headers),
	    UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(3, result.getParams().size());
    assertEquals(param1, "rft.issn=" + result.getParams().get("rft.issn"));
    assertEquals(param2, "rft.volume=" + result.getParams().get("rft.volume"));
    assertEquals(param3, "rft.spage=" + result.getParams().get("rft.spage"));
    assertEquals(1, result.getUrls().size());

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Provides the URL template to be tested.
   * 
   * @param pathAndQueryParams
   *          A String with the path and query parameters of the URL template to
   *          be tested.
   * @return a String with the URL template to be tested.
   */
  private String getTestUrlTemplate(String pathAndQueryParams) {
    return "http://localhost:" + port + pathAndQueryParams;
  }
}
