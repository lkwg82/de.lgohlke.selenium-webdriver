package de.lgohlke.selenium.webdriver.chrome;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class ChromeDriverVersionTestAPI {

    @Test
    public void weShouldHaveLatestVersionOfChromedriver() {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet("http://chromedriver.storage.googleapis.com/LATEST_RELEASE?" + System.currentTimeMillis());
            CloseableHttpResponse response = client.execute(request);
            String versionInResponse = EntityUtils.toString(response.getEntity());
            assertThat(versionInResponse).isEqualTo("2.19");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
