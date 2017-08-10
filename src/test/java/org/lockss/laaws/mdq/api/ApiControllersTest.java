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

import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lockss.laaws.mdq.model.AuMetadataPageInfo;
import org.lockss.laaws.mdq.model.ItemMetadata;
import org.lockss.laaws.mdq.model.UrlInfo;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * Test class for org.lockss.laaws.mdq.api.MetadataApiController and
 * org.lockss.laaws.mdq.api.UrlsApiController.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiControllersTest {
  private static final Logger logger =
      LoggerFactory.getLogger(ApiControllersTest.class);

  // The port that Tomcat is using during this test.
  @LocalServerPort
  private int port;

  // The application Context used to specify the command line arguments to be
  // used for the tests.
  @Autowired
  ApplicationContext appCtx;

  /**
   * Runs the tests with authentication turned off.
   * 
   * @throws Exception
   *           if there are problems.
   */
  @Test
  public void runUnAuthenticatedTests() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("port = " + port);

    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/apiControllerTestAuthOff.opt");

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
    if (logger.isDebugEnabled()) logger.debug("port = " + port);

    // Specify the command line parameters to be used for the tests.
    List<String> cmdLineArgs = getCommandLineArguments();
    cmdLineArgs.add("-p");
    cmdLineArgs.add("config/apiControllerTestAuthOn.opt");

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
	getTestUrl("/v2/api-docs"), HttpMethod.GET, null, String.class);

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

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<AuMetadataPageInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
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

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null,
	    AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<AuMetadataPageInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    AuMetadataPageInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    AuMetadataPageInfo result = successResponse.getBody();

    assertEquals(50, result.getItems().size());
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

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<AuMetadataPageInfo> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.GET, null, AuMetadataPageInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    uri = "/metadata/aus/non-existent";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.GET,
	    new HttpEntity<String>(null, headers), AuMetadataPageInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.NOT_FOUND, statusCode);

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related un-authenticated-specific tests.
   */
  private void deleteMetadataAusAuidUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(getTestUrl(uri), HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Integer> successResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    Integer result = successResponse.getBody();
    assertEquals(1, result.intValue());

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(0, result.intValue());

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), Integer.class);

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

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(getTestUrl(uri), HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.DELETE,
	    new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<Integer> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.DELETE, new HttpEntity<String>(null, headers),
	    Integer.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    Integer result = successResponse.getBody();
    assertEquals(67, result.intValue());

    deleteMetadataAusAuidCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the deleteMetadataAusAuid()-related authenticated-independent tests.
   */
  private void deleteMetadataAusAuidCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String auid = "org|lockss|plugin|taylorandfrancis|TaylorAndFrancisPlugin"
	+ "&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&journal_id~rafr20"
	+ "&volume_name~8";

    String uri = "/metadata/aus/" + UriUtils.encodePathSegment(auid, "UTF-8");

    ResponseEntity<String> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.DELETE, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    uri = "/metadata/aus/non-existent";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.DELETE,
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

    String uri = "/metadata/aus";

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(getTestUrl(uri), HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.POST, null, String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate().exchange(getTestUrl(uri),
	HttpMethod.POST, new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.POST,
	    new HttpEntity<String>(null, headers), String.class);

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

    String uri = "/metadata/aus";

    ResponseEntity<String> errorResponse = new TestRestTemplate()
	.exchange(getTestUrl(uri), HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.POST, null, String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate().exchange(getTestUrl(uri),
	HttpMethod.POST, new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.POST,
	    new HttpEntity<String>(null, headers), String.class);

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

    String uri = "/metadata/aus";

    ResponseEntity<String> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.POST, null, String.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.POST,
	    new HttpEntity<String>(null, headers), String.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, statusCode);

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String metadataJsonValue = "{\"scalarMap\":{\"date\":\"2016\"," 
	+ "\"coverage\":\"fulltext\",\"volume\":\"8\","
	+ "\"publication_name\":\"Africa Review\","
	+ "\"publisher_name\":\"Taylor & Francis\","
	+ "\"au_id\":\"org|lockss|plugin|taylorandfrancis|TaylorAndFrancis"
	+ "Plugin&base_url~http%3A%2F%2Fwww%2Etandfonline%2Ecom%2F&"
	+ "journal_id~rafr20&volume_name~8\"," +
	"\"provider_name\":\"Taylor & Francis\",\"fetch_time\":\"-1\"},"
	+ "\"listMap\":{\"proprietary_id\":[\"rafr20\"]},"
	+ "\"mapMap\":{\"issn\":{\"e_issn\":\"09744061\","
	+ "\"p_issn\":\"09744053\"},\"url\":{\"SupplementaryMaterials\":"
	+ "\"http://www.tandfonline.com/doi/suppl/10.1080/01436597.2015.1128816"
	+ "\",\"FullTextHtml\":\"http://www.tandfonline.com/doi/full/10.1080/"
	+ "01436597.2015.1128816\",\"Abstract\":\"http://www.tandfonline.com/"
	+ "doi/abs/10.1080/01436597.2015.1128816\",\"PdfPlus\":"
	+ "\"http://www.tandfonline.com/doi/pdfplus/10.1080/01436597.2015."
	+ "1128816\",\"References\":\"http://www.tandfonline.com/doi/ref/10."
	+ "1080/01436597.2015.1128816\",\"Access\":"
	+ "\"http://www.tandfonline.com/doi/pdf/10.1080/01436597.2015.1128816\""
	+ ",\"CitationRis\":\"http://www.tandfonline.com/action/"
	+ "downloadCitation?doi=10.1080%2F01436597.2015.1128816&format=ris&"
	+ "include=cit\",\"FullTextPdfFile\":\"http://www.tandfonline.com/doi/"
	+ "pdf/10.1080/01436597.2015.1128816\",\"ArticleMetadata\":"
	+ "\"http://www.tandfonline.com/action/downloadCitation?doi="
	+ "10.1080%2F01436597.2015.1128816&format=ris&include=cit\"}}}";

    ItemMetadata metadata =
	new ObjectMapper().readValue(metadataJsonValue, ItemMetadata.class);

    ResponseEntity<Long> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.POST,
	    new HttpEntity<ItemMetadata>(metadata, headers), Long.class);

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

    String doi = "10.1080/09744053.2016.1193941";
    String uri = "/urls/doi/" + UriUtils.encode(doi, "UTF-8");

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null, UrlInfo.class);

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

    String doi = "10.1080/09744053.2016.1193941";
    String uri = "/urls/doi/" + UriUtils.encode(doi, "UTF-8");

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null, UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    getUrlsDoiCommonTest();

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsDoi()-related authentication-independent tests.
   */
  private void getUrlsDoiCommonTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String doi = "10.1080/09744053.2016.1193941";
    String uri = "/urls/doi/" + UriUtils.encode(doi, "UTF-8");

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<UrlInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.GET, new HttpEntity<String>(null, headers),
	    UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    UrlInfo result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertTrue(("info:doi/" + doi)
	.startsWith(result.getParams().get("rft_id")));
    assertEquals(0, result.getUrls().size());

    doi = "non-existent";
    uri = "/urls/doi/" + UriUtils.encode(doi, "UTF-8");

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.GET,
	    new HttpEntity<String>(null, headers), UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertEquals("info:doi/" + doi, result.getParams().get("rft_id"));
    assertEquals(0, result.getUrls().size());

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Runs the getUrlsOpenUrl()-related un-authenticated-specific tests.
   */
  private void getUrlsOpenUrlUnAuthenticatedTest() throws Exception {
    if (logger.isDebugEnabled()) logger.debug("Invoked.");

    String uri = "/urls/openurl";

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null, UrlInfo.class);

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

    String uri = "/urls/openurl";

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate().exchange(getTestUrl(uri), HttpMethod.GET, null,
	    UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNAUTHORIZED, statusCode);

    errorResponse = new TestRestTemplate("fakeUser", "fakePassword")
	.exchange(getTestUrl(uri), HttpMethod.GET, null, UrlInfo.class);

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

    String uri = "/urls/openurl";

    ResponseEntity<UrlInfo> errorResponse =
	new TestRestTemplate("lockss-u", "lockss-p").exchange(getTestUrl(uri),
	    HttpMethod.GET, null, UrlInfo.class);

    HttpStatus statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusCode);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    errorResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(getTestUrl(uri), HttpMethod.GET,
	    new HttpEntity<String>(null, headers), UrlInfo.class);

    statusCode = errorResponse.getStatusCode();
    assertEquals(HttpStatus.BAD_REQUEST, statusCode);

    String param = "rft_id=info:doi/10.1080/09744053.2016.1193941";

    UriComponentsBuilder builder =
	UriComponentsBuilder.fromHttpUrl(getTestUrl(uri))
	.queryParam("params", UriUtils.encodeQueryParam(param, "UTF-8"));

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    ResponseEntity<UrlInfo> successResponse =
	new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(builder.build().encode().toUri(), HttpMethod.GET,
	    new HttpEntity<String>(null, headers), UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    UrlInfo result = successResponse.getBody();
    assertEquals(1, result.getParams().size());
    assertEquals(param, "rft_id=" + result.getParams().get("rft_id"));
    assertEquals(0, result.getUrls().size());

    String param1 = "rft.issn=0974-4053";
    String param2 = "rft.volume=8";
    String param3 = "rft.spage=156";

    builder = UriComponentsBuilder.fromHttpUrl(getTestUrl(uri))
	.queryParam("params", UriUtils.encodeQueryParam(param1, "UTF-8"))
	.queryParam("params", UriUtils.encodeQueryParam(param2, "UTF-8"))
	.queryParam("params", UriUtils.encodeQueryParam(param3, "UTF-8"));

    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (logger.isDebugEnabled()) logger.debug("headers = " + headers);

    successResponse = new TestRestTemplate("lockss-u", "lockss-p")
	.exchange(builder.build().encode().toUri(), HttpMethod.GET,
	    new HttpEntity<String>(null, headers), UrlInfo.class);

    statusCode = successResponse.getStatusCode();
    assertEquals(HttpStatus.OK, statusCode);

    result = successResponse.getBody();
    assertEquals(3, result.getParams().size());
    assertEquals(param1, "rft.issn=" + result.getParams().get("rft.issn"));
    assertEquals(param2, "rft.volume=" + result.getParams().get("rft.volume"));
    assertEquals(param3, "rft.spage=" + result.getParams().get("rft.spage"));
    assertEquals(0, result.getUrls().size());

    if (logger.isDebugEnabled()) logger.debug("Done.");
  }

  /**
   * Provides the URL to be tested.
   * 
   * @param uri
   *          A String with the URI of the URL to be tested.
   * @return a String with the URL to be tested.
   */
  private String getTestUrl(String uri) {
    return "http://localhost:" + port + uri;
  }
}
