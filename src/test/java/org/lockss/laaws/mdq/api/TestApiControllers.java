/*

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

 */
package org.lockss.laaws.mdq.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.PageInfo;
import org.lockss.laaws.mdq.model.UrlInfo;
import org.lockss.test.SpringLockssTestCase;
import org.lockss.util.ListUtil;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Test class for org.lockss.laaws.mdq.api.MetadataApiController and
 * org.lockss.laaws.mdq.api.UrlsApiController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestApiControllers extends SpringLockssTestCase {
  private static final String UI_PORT_CONFIGURATION_TEMPLATE =
      "UiPortConfigTemplate.txt";
  private static final String UI_PORT_CONFIGURATION_FILE = "UiPort.txt";
  private static final String DB_CONFIGURATION_TEMPLATE =
      "DbConfigTemplate.txt";
  private static final String DB_CONFIGURATION_FILE = "DbConfig.txt";

  // The name of the root directory of the local repository.
  private static final String REPOSITORY_ROOT_DIR_NAME = "testRepo";

  private static final String EMPTY_STRING = "";

  // The identifier of an AU that exists in the test system.
  private static final String GOOD_AUID_1 =
      "org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2014&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F";

  // The identifier of another AU that exists in the test system.
  private static final String GOOD_AUID_2 =
      "org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2015&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F";

  // The identifier of an AU that does not exist in the test system.
  private static final String UNKNOWN_AUID ="unknown_auid";

  // Credentials.
  private static final String GOOD_USER = "lockss-u";
  private static final String GOOD_PWD = "lockss-p";
  private static final String BAD_USER = "badUser";
  private static final String BAD_PWD = "badPassword";

  // Metadata contents for the first AU that exists in the test system.
  private static final String GOOD_METADATA_1 = "{\"scalarMap\":"
      + "{\"date\":\"2014-10-06\",\"coverage\":\"fulltext\",\"volume\":\"9\","
      + "\"publication_name\":\"BioRisk\","
      + "\"publisher_name\":\"Pensoft Publishers\",\"start_page\":\"1\","
      + "\"au_id\":\"org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2014&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F\","
      + "\"provider_name\":\"Pensoft Publishers\",\"fetch_time\":\"-1\","
      + "\"item_title\":\"China in the anthropocene: Culprit, victim or "
      + "last best hope for a global ecological civilisation?\","
      + "\"doi\":\"10.3897/biorisk.9.6105fgl\"},"
      + "\"listMap\":{\"author_name\":[\"Joachim Spangenberg\"]},"
      + "\"mapMap\":{\"issn\":{\"e_issn\":\"13132652\","
      + "\"p_issn\":\"13132652\"},\"url\":{\"Abstract\":"
      + "\"http://biorisk.pensoft.net/articles.php?id=1904\",\"Access\":"
      + "\"https://biorisk.pensoft.net/articles.php?id=1904\""
      + ",\"FullTextPdfFile\":\"http://biorisk.pensoft.net/lib/ajax_srv/"
      + "article_elements_srv.php?action=download_pdf&item_id=1904\""
      + ",\"ArticleMetadata\":\"http://biorisk.pensoft.net/"
      + "articles.php?id=1904\"}}}";

  // Metadata contents for the second AU that exists in the test system.
  private static final String GOOD_METADATA_2 = "{\"scalarMap\":"
      + "{\"date\":\"2015-10-06\",\"coverage\":\"fulltext\",\"volume\":\"19\","
      + "\"publication_name\":\"BioRisk\","
      + "\"publisher_name\":\"Pensoft Publishers\",\"start_page\":\"11\","
      + "\"au_id\":\"org|lockss|plugin|pensoft|oai|PensoftOaiPlugin"
      + "&au_oai_date~2015&au_oai_set~biorisk"
      + "&base_url~http%3A%2F%2Fbiorisk%2Epensoft%2Enet%2F\","
      + "\"provider_name\":\"Pensoft Publishers\",\"fetch_time\":\"-1\","
      + "\"item_title\":\"FGL Test Article\"},"
      + "\"listMap\":{\"author_name\":[\"Fernando García-Loygorri\"]},"
      + "\"mapMap\":{\"issn\":{\"e_issn\":\"13132652\","
      + "\"p_issn\":\"13132652\"},\"url\":{\"Access\":"
      + "\"https://biorisk.pensoft.net/articles.php?id=19040\""
      + ",\"FullTextPdfFile\":\"http://biorisk.pensoft.net/lib/ajax_srv/"
      + "article_elements_srv.php?action=download_pdf&item_id=19040\"}}}";

  // A DOI that does not exist in the test system.
  private static final String UNKNOWN_DOI ="unknown_doi";

  private static final Logger logger =
      LoggerFactory.getLogger(TestApiControllers.class);

  // The metadata of the first Archival Unit in the test system.
  private static ItemMetadata ITEM_METADATA_1 = null;

  // The metadata of the second Archival Unit in the test system.
  private static ItemMetadata ITEM_METADATA_2 = null;

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments to be
  // used for the tests.
  @Autowired
  ApplicationContext appCtx;

  /**
   * Set up code to be run before all tests.
   */
  @BeforeClass
  public static void setUpBeforeAllTests() throws IOException {
    // Fill in the metadata of the first Archival Unit in the test system.
    ITEM_METADATA_1 =
	new ObjectMapper().readValue(GOOD_METADATA_1, ItemMetadata.class);

    // Fill in the metadata of the second Archival Unit in the test system.
    ITEM_METADATA_2 =
	new ObjectMapper().readValue(GOOD_METADATA_2, ItemMetadata.class);
  }

  /**
   * Set up code to be run before each test.
   * 
   * @throws IOException if there are problems.
   */
  @Before
  public void setUpBeforeEachTest() throws IOException {
    if (logger.isDebugEnabled()) logger.debug("port = " + port);

    // Set up the temporary directory where the test data will reside.
    setUpTempDirectory(TestApiControllers.class.getCanonicalName());

    // Copy the necessary files to the test temporary directory.
    File srcTree = new File(new File("test"), "cache");
    if (logger.isDebugEnabled())
      logger.debug("srcTree = " + srcTree.getAbsolutePath());

    copyToTempDir(srcTree);

    srcTree = new File(new File("test"), REPOSITORY_ROOT_DIR_NAME);
    if (logger.isDebugEnabled())
      logger.debug("srcTree = " + srcTree.getAbsolutePath());

    copyToTempDir(srcTree);

    // Set up the UI port.
    setUpUiPort(UI_PORT_CONFIGURATION_TEMPLATE, UI_PORT_CONFIGURATION_FILE);

    // Set up the database configuration.
    setUpDbConfig(DB_CONFIGURATION_TEMPLATE, DB_CONFIGURATION_FILE);
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
    cmdLineArgs.add("test/config/testAuthOff.txt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    getSwaggerDocsTest();
    getStatusTest();
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
    cmdLineArgs.add("test/config/testAuthOn.txt");

    CommandLineRunner runner = appCtx.getBean(CommandLineRunner.class);
    runner.run(cmdLineArgs.toArray(new String[cmdLineArgs.size()]));

    getSwaggerDocsTest();
    getStatusTest();
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
   * @throws IOException
   *           if there are problems.
   */
  private List<String> getCommandLineArguments() throws IOException {
    List<String> cmdLineArgs = new ArrayList<String>();

    cmdLineArgs.add("-p");
    cmdLineArgs.add(getPlatformDiskSpaceConfigPath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/common.xml");
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/lockss.txt");
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getUiPortConfigFile().getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add(getDbConfigFile().getAbsolutePath());
    cmdLineArgs.add("-p");
    cmdLineArgs.add("test/config/lockss.opt");

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
   * Runs the status-related tests.
   * 
   * @throws Exception
   *           if there are problems.
   */
  private void getStatusTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    ResponseEntity<String> successResponse = new TestRestTemplate().exchange(
	getTestUrlTemplate("/status"), HttpMethod.GET, null, String.class);

    HttpStatus statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    String expectedBody = "{\"version\":\"1.0.0\",\"ready\":true}}";

    JSONAssert.assertEquals(expectedBody, successResponse.getBody(), false);
    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related un-authenticated-specific tests.
   */
  private void getMetadataAusAuidUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId: Spring reports it cannot find a match to an endpoint.
    runTestGetMetadataAusAuid(null, null, null, HttpStatus.NOT_FOUND);

    // Empty AUId: Spring reports it cannot find a match to an endpoint.
    runTestGetMetadataAusAuid(EMPTY_STRING, null, null, HttpStatus.NOT_FOUND);

    // Unknown AUId.
    runTestGetMetadataAusAuid(UNKNOWN_AUID, null, null, HttpStatus.NOT_FOUND);

    // Success with no credentials.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1, null, null,
	HttpStatus.OK));

    // success with bad credentials.
    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2, BAD_USER,
	BAD_PWD, HttpStatus.OK));

    getMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related authenticated-specific tests.
   */
  private void getMetadataAusAuidAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId.
    runTestGetMetadataAusAuid(null, null, null, HttpStatus.UNAUTHORIZED);

    // Empty AUId.
    runTestGetMetadataAusAuid(EMPTY_STRING, null, null,
	HttpStatus.UNAUTHORIZED);

    // No credentials.
    runTestGetMetadataAusAuid(GOOD_AUID_2, null, null, HttpStatus.UNAUTHORIZED);
    runTestGetMetadataAusAuid(UNKNOWN_AUID, null, null,
	HttpStatus.UNAUTHORIZED);

    // Bad credentials.
    runTestGetMetadataAusAuid(GOOD_AUID_1, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED);
    runTestGetMetadataAusAuid(UNKNOWN_AUID, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED);

    getMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getMetadataAusAuid()-related authentication-independent tests.
   */
  private void getMetadataAusAuidCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId: Spring reports it cannot find a match to an endpoint.
    runTestGetMetadataAusAuid(null, GOOD_USER, GOOD_PWD, HttpStatus.NOT_FOUND);

    // Empty AUId: Spring reports it cannot find a match to an endpoint.
    runTestGetMetadataAusAuid(EMPTY_STRING, GOOD_USER, GOOD_PWD,
	HttpStatus.NOT_FOUND);

    // Unknown AUId.
    runTestGetMetadataAusAuid(UNKNOWN_AUID, GOOD_USER, GOOD_PWD,
	HttpStatus.NOT_FOUND);

    // Success.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1,
	GOOD_USER, GOOD_PWD, HttpStatus.OK));

    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2,
	GOOD_USER, GOOD_PWD, HttpStatus.OK));

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Performs a GET operation for the metadata of an Archival Unit.
   * 
   * @param auId
   *          A String with the identifier of the Archival Unit.
   * @param user
   *          A String with the request username.
   * @param password
   *          A String with the request password.
   * @param expectedStatus
   *          An HttpStatus with the HTTP status of the result.
   * @return an AuMetadataPageInfo with the Archival Unit metadata.
   */
  private AuMetadataPageInfo runTestGetMetadataAusAuid(String auId, String user,
      String password, HttpStatus expectedStatus) {
    if (logger.isDebugEnabled()) {
      logger.debug("auId = " + auId);
      logger.debug("user = " + user);
      logger.debug("password = " + password);
      logger.debug("expectedStatus = " + expectedStatus);
    }

    // Get the test URL template.
    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", auId));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> requestEntity = null;

    // Check whether there are any custom headers to be specified in the
    // request.
    if (user != null || password != null) {

      // Initialize the request headers.
      HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      setUpCredentials(user, password, headers);

      if (logger.isDebugEnabled())
	logger.debug("requestHeaders = " + headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<String>(null, headers);
    }

    // Make the request and get the response. 
    ResponseEntity<AuMetadataPageInfo> response =
	new TestRestTemplate(restTemplate).exchange(uri, HttpMethod.GET,
	    requestEntity, AuMetadataPageInfo.class);

    // Get the response status.
    HttpStatus statusCode = response.getStatusCode();
    assertEquals(expectedStatus, statusCode);

    return response.getBody();
  }

  /**
   * Runs the deleteMetadataAusAuid()-related un-authenticated-specific tests.
   */
  private void deleteMetadataAusAuidUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId: Spring reports it cannot find a match to an endpoint.
    runTestDeleteMetadataAusAuid(null, null, null, HttpStatus.NOT_FOUND, -1);
    runTestDeleteMetadataAusAuid(null, BAD_USER, BAD_PWD, HttpStatus.NOT_FOUND,
	-1);

    // Empty AUId: Spring reports it cannot find a match to an endpoint.
    runTestDeleteMetadataAusAuid(EMPTY_STRING, null, null, HttpStatus.NOT_FOUND,
	-1);
    runTestDeleteMetadataAusAuid(EMPTY_STRING, BAD_USER, BAD_PWD,
	HttpStatus.NOT_FOUND, -1);

    // Unknown AUId.
    runTestDeleteMetadataAusAuid(UNKNOWN_AUID, null, null, HttpStatus.NOT_FOUND,
	-1);
    runTestDeleteMetadataAusAuid(UNKNOWN_AUID, BAD_USER, BAD_PWD,
	HttpStatus.NOT_FOUND, -1);

    // Delete the first good Archival Unit with no credentials.
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, null, null, HttpStatus.OK, 1);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, null, null,
	HttpStatus.OK).getItems().isEmpty());

    // Verify that the second good Archival Unit is not affected.
    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2, null, null,
	HttpStatus.OK));

    // Delete again the first good Archival Unit with bad credentials.
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, BAD_USER, BAD_PWD, HttpStatus.OK,
	0);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, BAD_USER, BAD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    // Verify that the second good Archival Unit is not affected.
    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2, BAD_USER,
	BAD_PWD, HttpStatus.OK));

    deleteMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related authenticated-specific tests.
   */
  private void deleteMetadataAusAuidAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId.
    runTestDeleteMetadataAusAuid(null, null, null, HttpStatus.UNAUTHORIZED, -1);
    runTestDeleteMetadataAusAuid(null, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED, -1);

    // Empty AUId.
    runTestDeleteMetadataAusAuid(EMPTY_STRING, null, null,
	HttpStatus.UNAUTHORIZED, -1);
    runTestDeleteMetadataAusAuid(EMPTY_STRING, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED, -1);

    // Unknown AUId.
    runTestDeleteMetadataAusAuid(UNKNOWN_AUID, null, null,
	HttpStatus.UNAUTHORIZED, -1);
    runTestDeleteMetadataAusAuid(UNKNOWN_AUID, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED, -1);

    // First good Archival Unit.
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, null, null,
	HttpStatus.UNAUTHORIZED, -1);
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED, -1);

    // Second good Archival Unit.
    runTestDeleteMetadataAusAuid(GOOD_AUID_2, null, null,
	HttpStatus.UNAUTHORIZED, -1);
    runTestDeleteMetadataAusAuid(GOOD_AUID_2, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED, -1);

    deleteMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related authenticated-independent tests.
   */
  private void deleteMetadataAusAuidCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No AUId: Spring reports it cannot find a match to an endpoint.
    runTestDeleteMetadataAusAuid(null, GOOD_USER, GOOD_PWD,
	HttpStatus.NOT_FOUND, -1);

    // Empty AUId: Spring reports it cannot find a match to an endpoint.
    runTestDeleteMetadataAusAuid(EMPTY_STRING, GOOD_USER, GOOD_PWD,
	HttpStatus.NOT_FOUND, -1);

    // Unknown AUId.
    runTestDeleteMetadataAusAuid(UNKNOWN_AUID, GOOD_USER, GOOD_PWD,
	HttpStatus.NOT_FOUND, -1);

    // Delete the second good Archival Unit.
    runTestDeleteMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK, 1);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    // Delete again the second good Archival Unit.
    runTestDeleteMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK, 0);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Performs a DELETE operation for the metadata of an Archival Unit.
   * 
   * @param auId
   *          A String with the identifier of the Archival Unit.
   * @param user
   *          A String with the request username.
   * @param password
   *          A String with the request password.
   * @param expectedStatus
   *          An HttpStatus with the HTTP status of the result.
   * @param expectedDeletedCount
   *          An int with the count of expected metadata items to be deleted.
   */
  private void runTestDeleteMetadataAusAuid(String auId, String user,
      String password, HttpStatus expectedStatus, int expectedDeletedCount) {
    if (logger.isDebugEnabled()) {
      logger.debug("auId = " + auId);
      logger.debug("user = " + user);
      logger.debug("password = " + password);
      logger.debug("expectedStatus = " + expectedStatus);
      logger.debug("expectedDeletedCount = " + expectedDeletedCount);
    }

    // Get the test URL template.
    String template = getTestUrlTemplate("/metadata/aus/{auid}");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(template)
	.build().expand(Collections.singletonMap("auid", auId));

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> requestEntity = null;

    // Check whether there are any custom headers to be specified in the
    // request.
    if (user != null || password != null) {

      // Initialize the request headers.
      HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      setUpCredentials(user, password, headers);

      if (logger.isDebugEnabled())
	logger.debug("requestHeaders = " + headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<String>(null, headers);
    }

    // The next call should use the Integer class instead of the String class,
    // but Spring gets confused when errors are reported.
    // Make the request and get the response. 
    ResponseEntity<String> response = new TestRestTemplate(restTemplate).
	exchange(uri, HttpMethod.DELETE, requestEntity, String.class);

    // Get the response status.
    HttpStatus statusCode = response.getStatusCode();
    assertEquals(expectedStatus, statusCode);

    if (isSuccess(statusCode)) {
      // Verify the count of deleted items.
      assertEquals(expectedDeletedCount, Integer.parseInt(response.getBody()));
    }
  }

  /**
   * Runs the postMetadataAusItem()-related un-authenticated-specific tests.
   */
  private void postMetadataAusItemUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // Missing payload (This should return HttpStatus.BAD_REQUEST, but Spring
    // returns HttpStatus.UNSUPPORTED_MEDIA_TYPE).
    runTestPostMetadataAus(null, null, null, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    runTestPostMetadataAus(null, BAD_USER, BAD_PWD,
	HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    // Verify that the first good Archival Unit is empty.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, null, null, HttpStatus.OK)
	.getItems().isEmpty());

    // Verify that the second good Archival Unit is empty.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, null, null, HttpStatus.OK)
	.getItems().isEmpty());

    // Fill in the metadata of the first Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_1, null, null, HttpStatus.OK);

    // Verify.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1, null, null,
	HttpStatus.OK));

    // Verify that the second good Archival Unit is unchanged.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, null, null, HttpStatus.OK)
	.getItems().isEmpty());

    // Fill in the metadata of the second Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_2, BAD_USER, BAD_PWD, HttpStatus.OK);

    // Verify.
    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2, null, null,
	HttpStatus.OK));

    // Verify that the first good Archival Unit is unchanged.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1, BAD_USER,
	BAD_PWD, HttpStatus.OK));

    // Delete the first good Archival Unit with no credentials.
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, null, null, HttpStatus.OK, 1);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, null, null, HttpStatus.OK)
	.getItems().isEmpty());

    // Delete the second good Archival Unit with no credentials.
    runTestDeleteMetadataAusAuid(GOOD_AUID_2, null, null, HttpStatus.OK, 1);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, null, null, HttpStatus.OK)
	.getItems().isEmpty());

    postMetadataAusItemCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the postMetadataAusItem()-related authenticated-specific tests.
   */
  private void postMetadataAusItemAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No payload.
    runTestPostMetadataAus(null, null, null, HttpStatus.UNAUTHORIZED);
    runTestPostMetadataAus(null, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED);

    // Fill in the metadata of the first Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_1, null, null,
	HttpStatus.UNAUTHORIZED);

    // Fill in the metadata of the second Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_2, BAD_USER, BAD_PWD,
	HttpStatus.UNAUTHORIZED);

    // Delete the first good Archival Unit.
    runTestDeleteMetadataAusAuid(GOOD_AUID_1, GOOD_USER, GOOD_PWD,
	HttpStatus.OK, 1);

    // Verify.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    postMetadataAusItemCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the postMetadataAusItem()-related authentication-independent tests.
   */
  private void postMetadataAusItemCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // Missing payload (This should return HttpStatus.BAD_REQUEST, but Spring
    // returns HttpStatus.UNSUPPORTED_MEDIA_TYPE).
    runTestPostMetadataAus(null, GOOD_USER, GOOD_PWD,
	HttpStatus.UNSUPPORTED_MEDIA_TYPE);

    // Verify that the first good Archival Unit is empty.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_1, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    // Verify that the second good Archival Unit is empty.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    // Fill in the metadata of the first Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_1, GOOD_USER, GOOD_PWD, HttpStatus.OK);

    // Verify.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1, GOOD_USER,
	GOOD_PWD, HttpStatus.OK));

    // Verify that the second good Archival Unit is unchanged.
    assertTrue(runTestGetMetadataAusAuid(GOOD_AUID_2, GOOD_USER, GOOD_PWD,
	HttpStatus.OK).getItems().isEmpty());

    // Fill in the metadata of the second Archival Unit in the test system.
    runTestPostMetadataAus(ITEM_METADATA_2, GOOD_USER, GOOD_PWD, HttpStatus.OK);

    // Verify.
    verifyGoodAu2Metadata(runTestGetMetadataAusAuid(GOOD_AUID_2, GOOD_USER,
	GOOD_PWD, HttpStatus.OK));

    // Verify that the first good Archival Unit is unchanged.
    verifyGoodAu1Metadata(runTestGetMetadataAusAuid(GOOD_AUID_1, GOOD_USER,
	GOOD_PWD, HttpStatus.OK));

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Performs a POST operation for the metadata of an Archival Unit.
   * 
   * @param metadata
   *          An ItemMetadata with the metadata.
   * @param user
   *          A String with the request username.
   * @param password
   *          A String with the request password.
   * @param expectedStatus
   *          An HttpStatus with the HTTP status of the result.
   */
  private void runTestPostMetadataAus(ItemMetadata metadata, String user,
      String password, HttpStatus expectedStatus) {
    if (logger.isDebugEnabled()) {
      logger.debug("metadata = " + metadata);
      logger.debug("user = " + user);
      logger.debug("password = " + password);
      logger.debug("expectedStatus = " + expectedStatus);
    }

    // Get the test URL template.
    String template = getTestUrlTemplate("/metadata/aus");

    // Create the URI of the request to the REST service.
    UriComponents uriComponents =
	UriComponentsBuilder.fromUriString(template).build();

    URI uri = UriComponentsBuilder.newInstance().uriComponents(uriComponents)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<ItemMetadata> requestEntity = null;

    // Check whether there are any custom headers to be specified in the
    // request.
    if (metadata != null || user != null || password != null) {

      // Initialize the request headers.
      HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      setUpCredentials(user, password, headers);

      if (logger.isDebugEnabled())
	logger.debug("requestHeaders = " + headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<ItemMetadata>(metadata, headers);
    }

    // The next call should use the Long class instead of the String class,
    // but Spring gets confused when errors are reported.
    // Make the request and get the response. 
    ResponseEntity<String> response =
	new TestRestTemplate(restTemplate). exchange(uri, HttpMethod.POST,
	    requestEntity, String.class);

    // Get the response status.
    HttpStatus statusCode = response.getStatusCode();
    assertEquals(expectedStatus, statusCode);
  }

  /**
   * Runs the getUrlsDoi()-related un-authenticated-specific tests.
   */
  private void getUrlsDoiUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No DOI.
    runTestGetUrlsDoi(null, null, null, HttpStatus.OK, null);
    runTestGetUrlsDoi(null, BAD_USER, BAD_PWD, HttpStatus.OK, null);

    // Empty DOI.
    runTestGetUrlsDoi(EMPTY_STRING, null, null, HttpStatus.OK, null);
    runTestGetUrlsDoi(EMPTY_STRING, BAD_USER, BAD_PWD, HttpStatus.OK, null);

    // DOI of the first Archival Unit in the test system.
    String doi = ITEM_METADATA_1.getScalarMap().get("doi");
    String expectedUrl = ITEM_METADATA_1.getMapMap().get("url").get("Access");

    runTestGetUrlsDoi(doi, null, null, HttpStatus.OK, expectedUrl);
    runTestGetUrlsDoi(doi, BAD_USER, BAD_PWD, HttpStatus.OK, expectedUrl);

    // Unknown DOI.
    runTestGetUrlsDoi(UNKNOWN_DOI, null, null, HttpStatus.OK, null);
    runTestGetUrlsDoi(UNKNOWN_DOI, BAD_USER, BAD_PWD, HttpStatus.OK, null);

    getUrlsDoiCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related authenticated-specific tests.
   */
  private void getUrlsDoiAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No DOI.
    runTestGetUrlsDoi(null, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsDoi(null, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED, null);

    // Empty DOI.
    runTestGetUrlsDoi(EMPTY_STRING, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsDoi(EMPTY_STRING, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // DOI of the first Archival Unit in the test system.
    String doi = ITEM_METADATA_1.getScalarMap().get("doi");

    runTestGetUrlsDoi(doi, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsDoi(doi, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED, null);

    // Unknown DOI.
    runTestGetUrlsDoi(UNKNOWN_DOI, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsDoi(UNKNOWN_DOI, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    getUrlsDoiCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related authentication-independent tests.
   */
  private void getUrlsDoiCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No DOI.
    runTestGetUrlsDoi(null, GOOD_USER, GOOD_PWD, HttpStatus.OK, null);

    // Empty DOI.
    runTestGetUrlsDoi(EMPTY_STRING, GOOD_USER, GOOD_PWD, HttpStatus.OK, null);

    // DOI of the first Archival Unit in the test system.
    String doi = ITEM_METADATA_1.getScalarMap().get("doi");
    String expectedUrl = ITEM_METADATA_1.getMapMap().get("url").get("Access");

    runTestGetUrlsDoi(doi, GOOD_USER, GOOD_PWD, HttpStatus.OK, expectedUrl);

    // Unknown DOI.
    runTestGetUrlsDoi(UNKNOWN_DOI, GOOD_USER, GOOD_PWD, HttpStatus.OK, null);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Performs a GET operation for the URL of a DOI.
   *
   * @param doi
   *          A String with the DOI.
   * @param user
   *          A String with the request username.
   * @param password
   *          A String with the request password.
   * @param expectedStatus
   *          An HttpStatus with the HTTP status of the result.
   * @param expectedUrl
   *          A String with the URL in the result.
   */
  private void runTestGetUrlsDoi(String doi, String user, String password,
      HttpStatus expectedStatus, String expectedUrl) {
    if (logger.isDebugEnabled()) {
      logger.debug("doi = " + doi);
      logger.debug("user = " + user);
      logger.debug("password = " + password);
      logger.debug("expectedStatus = " + expectedStatus);
      logger.debug("expectedUrl = " + expectedUrl);
    }

    // Get the test URL template.
    String template = getTestUrlTemplate("/urls/doi");

    // Create the URI of the request to the REST service.
    URI uri = UriComponentsBuilder.fromHttpUrl(template).queryParam("doi", doi)
	.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> requestEntity = null;

    // Check whether there are any custom headers to be specified in the
    // request.
    if (user != null || password != null) {

      // Initialize the request headers.
      HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      setUpCredentials(user, password, headers);

      if (logger.isDebugEnabled())
	logger.debug("requestHeaders = " + headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<String>(null, headers);
    }

    // Make the request and get the response. 
    ResponseEntity<UrlInfo> response = new TestRestTemplate(restTemplate)
	.exchange(uri, HttpMethod.GET, requestEntity, UrlInfo.class);

    // Get the response status.
    HttpStatus statusCode = response.getStatusCode();
    assertEquals(expectedStatus, statusCode);

    // Verify.
    if (isSuccess(statusCode)) {
      UrlInfo result = response.getBody();

      // Parameters.
      Map<String, String> params = result.getParams();
      assertEquals(1, params.size());

      if (doi == null) {
	assertEquals("info:doi/", params.get("rft_id"));
      } else {
	assertEquals("info:doi/" + doi, params.get("rft_id"));
      }

      // URLs.
      List<String> urls = result.getUrls();

      if (expectedUrl == null) {
	assertEquals(0, urls.size());
      } else {
	assertEquals(1, urls.size());
	assertEquals(expectedUrl, urls.get(0));
      }
    }
  }

  /**
   * Runs the getUrlsOpenUrl()-related un-authenticated-specific tests.
   */
  private void getUrlsOpenUrlUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No OpenURL params.
    runTestGetUrlsOpenUrl(null, null, null, HttpStatus.BAD_REQUEST, null);
    runTestGetUrlsOpenUrl(null, BAD_USER, BAD_PWD, HttpStatus.BAD_REQUEST,
	null);

    // Empty OpenURL params.
    List<String> params = new ArrayList<>();
    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.BAD_REQUEST, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.BAD_REQUEST,
	null);

    // DOI of the first Archival Unit in the test system.
    params = ListUtil.list("rft_id=info:doi/"
	+ ITEM_METADATA_1.getScalarMap().get("doi"));
    String expectedUrl = ITEM_METADATA_1.getMapMap().get("url").get("Access");

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.OK, expectedUrl);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.OK,
	expectedUrl);

    // Unknown DOI.
    params = ListUtil.list("rft_id=info:doi/" + UNKNOWN_DOI);

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.OK, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.OK, null);

    // Multiple parameters for the first Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_1.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_1.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_1.getScalarMap().get("start_page"));

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.OK, expectedUrl);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.OK,
	expectedUrl);

    // Multiple parameters for the second Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_2.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_2.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_2.getScalarMap().get("start_page"));

    expectedUrl = ITEM_METADATA_2.getMapMap().get("url").get("Access");

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.OK, expectedUrl);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.OK,
	expectedUrl);

    getUrlsOpenUrlCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related authenticated-specific tests.
   */
  private void getUrlsOpenUrlAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No OpenURL params.
    runTestGetUrlsOpenUrl(null, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(null, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // Empty OpenURL params.
    List<String> params = new ArrayList<>();
    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // DOI of the first Archival Unit in the test system.
    params = ListUtil.list("rft_id=info:doi/"
	+ ITEM_METADATA_1.getScalarMap().get("doi"));

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // Unknown DOI.
    params = ListUtil.list("rft_id=info:doi/" + UNKNOWN_DOI);

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // Multiple parameters for the first Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_1.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_1.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_1.getScalarMap().get("start_page"));

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    // Multiple parameters for the second Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_2.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_2.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_2.getScalarMap().get("start_page"));

    runTestGetUrlsOpenUrl(params, null, null, HttpStatus.UNAUTHORIZED, null);
    runTestGetUrlsOpenUrl(params, BAD_USER, BAD_PWD, HttpStatus.UNAUTHORIZED,
	null);

    getUrlsOpenUrlCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related authentication-independent tests.
   */
  private void getUrlsOpenUrlCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    // No OpenURL params.
    runTestGetUrlsOpenUrl(null, GOOD_USER, GOOD_PWD, HttpStatus.BAD_REQUEST,
	null);

    // Empty OpenURL params.
    List<String> params = new ArrayList<>();
    runTestGetUrlsOpenUrl(params, GOOD_USER, GOOD_PWD, HttpStatus.BAD_REQUEST,
	null);

    // DOI of the first Archival Unit in the test system.
    params = ListUtil.list("rft_id=info:doi/"
	+ ITEM_METADATA_1.getScalarMap().get("doi"));
    String expectedUrl = ITEM_METADATA_1.getMapMap().get("url").get("Access");

    runTestGetUrlsOpenUrl(params, GOOD_USER, GOOD_PWD, HttpStatus.OK,
	expectedUrl);

    // Unknown DOI.
    params = ListUtil.list("rft_id=info:doi/" + UNKNOWN_DOI);
    runTestGetUrlsOpenUrl(params, GOOD_USER, GOOD_PWD, HttpStatus.OK, null);

    // Multiple parameters for the first Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_1.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_1.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_1.getScalarMap().get("start_page"));

    runTestGetUrlsOpenUrl(params, GOOD_USER, GOOD_PWD, HttpStatus.OK,
	expectedUrl);

    // Multiple parameters for the second Archival Unit in the test system.
    params = ListUtil.list(
	"rft.issn=" + ITEM_METADATA_2.getMapMap().get("issn").get("p_issn"),
	"rft.volume=" + ITEM_METADATA_2.getScalarMap().get("volume"),
	"rft.spage=" + ITEM_METADATA_2.getScalarMap().get("start_page"));

    expectedUrl = ITEM_METADATA_2.getMapMap().get("url").get("Access");

    runTestGetUrlsOpenUrl(params, GOOD_USER, GOOD_PWD, HttpStatus.OK,
	expectedUrl);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Performs a GET operation for the URL that results from performing an
   * OpenURL query.
   *
   * @param openUrlParams
   *          A List<String> with the parameters of the OpenURL query.
   * @param user
   *          A String with the request username.
   * @param password
   *          A String with the request password.
   * @param expectedStatus
   *          An HttpStatus with the HTTP status of the result.
   * @param expectedUrl
   *          A String with the URL in the result.
   */
  private void runTestGetUrlsOpenUrl(List<String> openUrlParams, String user,
      String password, HttpStatus expectedStatus, String expectedUrl) {
    if (logger.isDebugEnabled()) {
      logger.debug("openUrlParams = " + openUrlParams);
      logger.debug("user = " + user);
      logger.debug("password = " + password);
      logger.debug("expectedStatus = " + expectedStatus);
      logger.debug("expectedUrl = " + expectedUrl);
    }

    // Get the test URL template.
    String template = getTestUrlTemplate("/urls/openurl");

    // Create the URI of the request to the REST service.
    UriComponentsBuilder ucb = UriComponentsBuilder.fromHttpUrl(template);

    if (openUrlParams != null) {
      for (String param : openUrlParams) {
	ucb = ucb.queryParam("params", param);
      }
    }

    URI uri = ucb.build().encode().toUri();
    if (logger.isDebugEnabled()) logger.debug("uri = " + uri);

    // Initialize the request to the REST service.
    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> requestEntity = null;

    // Check whether there are any custom headers to be specified in the
    // request.
    if (user != null || password != null) {

      // Initialize the request headers.
      HttpHeaders headers = new HttpHeaders();

      // Set up the authentication credentials, if necessary.
      setUpCredentials(user, password, headers);

      if (logger.isDebugEnabled())
	logger.debug("requestHeaders = " + headers.toSingleValueMap());

      // Create the request entity.
      requestEntity = new HttpEntity<String>(null, headers);
    }

    // Make the request and get the response. 
    ResponseEntity<UrlInfo> response = new TestRestTemplate(restTemplate)
	.exchange(uri, HttpMethod.GET, requestEntity, UrlInfo.class);

    // Get the response status.
    HttpStatus statusCode = response.getStatusCode();
    assertEquals(expectedStatus, statusCode);

    // Verify.
    if (isSuccess(statusCode)) {
      UrlInfo result = response.getBody();

      // Parameters.
      Map<String, String> params = result.getParams();
      assertEquals(openUrlParams.size(), params.size());

      for (Map.Entry<String, String> param : params.entrySet()) {
	assertTrue(openUrlParams.contains(
	    param.getKey() + "=" + param.getValue()));
      }

      // URLs.
      List<String> urls = result.getUrls();

      if (expectedUrl == null) {
	assertEquals(0, urls.size());
      } else {
	assertEquals(1, urls.size());
	assertEquals(expectedUrl, urls.get(0));
      }
    }
  }

  /**
   * Verifies that the passed metadata matches that of the known first good
   * Archival Unit.
   * 
   * @param auMetadata
   *          A AuMetadataPageInfo with the Archival Unit metadata to be
   *          verified.
   */
  private void verifyGoodAu1Metadata(AuMetadataPageInfo auMetadata) {
    if (logger.isDebugEnabled()) logger.debug("auMetadata = " + auMetadata);

    PageInfo pageInfo = auMetadata.getPageInfo();
    assertNull(pageInfo.getTotalCount());
    assertEquals(new Integer(50), pageInfo.getResultsPerPage());
    assertEquals(new Integer(1), pageInfo.getCurrentPage());
    assertTrue(pageInfo.getCurLink().startsWith(getTestUrlTemplate("")));
    assertTrue(pageInfo.getNextLink().startsWith(getTestUrlTemplate("")));

    List<ItemMetadata> items = auMetadata.getItems();
    assertEquals(1, items.size());
    assertEquals(ITEM_METADATA_1, items.get(0));
  }

  /**
   * Verifies that the passed metadata matches that of the known second good
   * Archival Unit.
   * 
   * @param auMetadata
   *          A AuMetadataPageInfo with the Archival Unit metadata to be
   *          verified.
   */
  private void verifyGoodAu2Metadata(AuMetadataPageInfo auMetadata) {
    if (logger.isDebugEnabled()) logger.debug("auMetadata = " + auMetadata);

    PageInfo pageInfo = auMetadata.getPageInfo();
    assertNull(pageInfo.getTotalCount());
    assertEquals(new Integer(50), pageInfo.getResultsPerPage());
    assertEquals(new Integer(1), pageInfo.getCurrentPage());
    assertTrue(pageInfo.getCurLink().startsWith(getTestUrlTemplate("")));
    assertTrue(pageInfo.getNextLink().startsWith(getTestUrlTemplate("")));

    List<ItemMetadata> items = auMetadata.getItems();
    assertEquals(1, items.size());
    assertEquals(ITEM_METADATA_2, items.get(0));
  }

  /**
   * Adds credentials to the HTTP headers, if necessary.
   * 
   * @param user
   *          A String with the credentials username.
   * @param password
   *          A String with the credentials password.
   * @param headers
   *          An HttpHeaders with the HTTP headers.
   */
  private void setUpCredentials(String user, String password,
      HttpHeaders headers) {
    // Check whether there are credentials to be added.
    if (user != null && password != null) {
      // Yes: Set the authentication credentials.
      String credentials = user + ":" + password;
      String authHeaderValue = "Basic " + Base64.getEncoder()
      .encodeToString(credentials.getBytes(Charset.forName("US-ASCII")));

      headers.set("Authorization", authHeaderValue);
    }
  }

  /**
   * Provides an indication of whether a successful response has been obtained.
   * 
   * @param statusCode
   *          An HttpStatus with the response status code.
   * @return a boolean with <code>true</code> if a successful response has been
   *         obtained, <code>false</code> otherwise.
   */
  private boolean isSuccess(HttpStatus statusCode) {
    return statusCode.is2xxSuccessful();
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
