package jeffaschenk.infra.it;

import jeffaschenk.infra.sbdm.DeploymentManagerApplication;
import jeffaschenk.infra.sbdm.common.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tika.io.IOUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.StringWriter;

import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DeploymentManagerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeploymentManagerAPI_IT {

    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(DeploymentManagerAPI_IT.class);

    static {
        System.setProperty(Constants.DEPLOYMENT_MANAGER_SA_DIRECTORY_PROPERTY_NAME, "target");
    }

    private static final String  UPLOAD_PROPERTIES_TEST_JSON_DATA =
            "{\"serviceName\":\"testServiceD\",\"serviceConfiguration\":{\"OneKey\":\"OneValue\",\"TwoKey\": \"TwoValue\"}}";

    @LocalServerPort
    private int localServerPort;

    @Test
    public void test01_GetAllServiceStatuses() throws Exception {
        LOGGER.info("Running test01_StatusTest ...");

        HttpHost target = new HttpHost("localhost", localServerPort, "http");

        CloseableHttpClient httpClient = HttpClients.custom().build();
        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();

        // Get Service Status API EndPoint
        HttpGet getStatus = new HttpGet("/deploymentManager/api/status");
        CloseableHttpResponse response = httpClient.execute(target,  getStatus, localContext);
        HttpEntity responseEntity = response.getEntity();

        displayContent(responseEntity);
    }

    @Test
    public void test02_GetSpecificServiceStatus() throws Exception {
        LOGGER.info("Running test01_Status ...");

        HttpHost target = new HttpHost("localhost", localServerPort, "http");

        //CredentialsProvider credsProvider = new BasicCredentialsProvider();
        //credsProvider.setCredentials(
        //        new AuthScope(target.getHostName(), target.getPort()),
        //        new UsernamePasswordCredentials("username", "password"));
        //CloseableHttpClient httpClient = HttpClients.custom()
        //        .setDefaultCredentialsProvider(credsProvider).build();

        String serviceName = "testServiceD";

        CloseableHttpClient httpClient = HttpClients.custom().build();
        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();

        // Get Service Status API EndPoint
        HttpGet getStatus = new HttpGet("/deploymentManager/api/status/"+serviceName);

        CloseableHttpResponse response = httpClient.execute(target,  getStatus, localContext);
        HttpEntity responseEntity = response.getEntity();

        displayContent(responseEntity);
    }


    @Test
    public void test03_Upload() throws Exception {
        LOGGER.info("Running test03_Upload ...");
        HttpHost target = new HttpHost("localhost", localServerPort, "http");

        //CredentialsProvider credsProvider = new BasicCredentialsProvider();
        //credsProvider.setCredentials(
        //        new AuthScope(target.getHostName(), target.getPort()),
        //        new UsernamePasswordCredentials("username", "password"));
        //CloseableHttpClient httpClient = HttpClients.custom()
        //        .setDefaultCredentialsProvider(credsProvider).build();

        String serviceName = "testServiceD";
        File testServiceDArtifact = new File("src/test/resources"+File.separator+serviceName+".jar");
        assertTrue(testServiceDArtifact.exists());

        CloseableHttpClient httpClient = HttpClients.custom().build();
        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();

        // POST to Upload API EndPoint
        HttpPost uploadFile = new HttpPost("/deploymentManager/api/upload/"+serviceName);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("uploadProperties", UPLOAD_PROPERTIES_TEST_JSON_DATA,
                ContentType.APPLICATION_JSON);

        builder.addBinaryBody("file", testServiceDArtifact,
                MULTIPART_FORM_DATA, serviceName+".jar");

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        LOGGER.info("Upload Content Type: {}", multipart.getContentType());

        CloseableHttpResponse response = httpClient.execute(target, uploadFile, localContext);
        HttpEntity responseEntity = response.getEntity();

        displayContent(responseEntity);
    }


    private void displayContent(HttpEntity responseEntity) throws Exception {
        LOGGER.info("{}",responseEntity.toString());
        if (responseEntity.getContent() instanceof EofSensorInputStream) {
            EofSensorInputStream is = (EofSensorInputStream)responseEntity.getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            String json = writer.toString();
            LOGGER.info("{}",json);
        } else {
            LOGGER.info("{}",responseEntity.getContent().toString());
        }
    }
}
