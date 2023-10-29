package com.bds.secure.secureapp;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.File;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TlsBaseTest {
    @LocalServerPort
    private int port;

    private static final char[] pw = "abc123".toCharArray();

    protected String getBody(CloseableHttpResponse response) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        return baos.toString();
    }

    CloseableHttpResponse executeGet(SSLContext sslContext, String url) throws Exception {
        String fullUrl = "https://localhost:%d%s".formatted(port, url);
        SSLConnectionSocketFactory sf = new SSLConnectionSocketFactory(sslContext);
        PoolingHttpClientConnectionManagerBuilder connMgrBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        connMgrBuilder.setSSLSocketFactory(sf);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connMgrBuilder.build())
                .build()) {

            HttpGet req = new HttpGet(fullUrl);
            return httpClient.execute(req);
        }
    }

    /**
     * alias is the alias (name) of the cert in the keystore, which is used as identity
     */
    SSLContextBuilder createSSLContextBuilder(String keyStoreFile, String alias, String trustStoreFile) throws Exception {
        SSLContextBuilder builder = SSLContexts.custom();
        if (keyStoreFile != null) {
            builder.loadKeyMaterial(new File(keyStoreFile), pw, pw, (a, b) -> alias);
        }
        if (trustStoreFile != null) {
            builder.loadTrustMaterial(new File(trustStoreFile), pw);
        }
        return builder;
    }
}
