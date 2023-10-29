package com.bds.secure.secureapp;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// See Readme.md on cert setup


@ActiveProfiles("ssl-test-clientauth")
public class TlsWithClientAuthTest extends TlsBaseTest {

    @BeforeEach
    void setUp() {
        // To enable full TLS logging
        // System.setProperty("javax.net.debug", "all");
    }

    @Test
    void defaultClientRejectsSelfSignedServerCertThrowingPKIXException() throws Exception {
        // Client doesn't accept self-signed cert from the server by defaujlt

        SSLContext sslContext = createSSLContextBuilder(null, null, null).build();
        SSLHandshakeException sslHandshakeException = assertThrows(SSLHandshakeException.class,
                () -> executeGet(sslContext, "/api/health"),
                "Expected SSLHandshakeException, but was not thrown"
        );
        assertThat(sslHandshakeException.getMessage()).isEqualTo("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target");
    }

    @Test
    void clientAcceptSelfSignedServerCertServerRejectsDefaultClientCert() throws Exception {
        // Client accept self-signed cert, server rejects client's default cert

        SSLContext sslContext = createSSLContextBuilder(null, null, null)
                .loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE)
                .build();
        SSLHandshakeException sslHandshakeException = assertThrows(SSLHandshakeException.class,
                () -> executeGet(sslContext, "/api/health"),
                "Expected SSLHandshakeException, but was not thrown"
        );
        assertThat(sslHandshakeException.getMessage()).isEqualTo("Received fatal alert: bad_certificate");
    }

    @Test
    void clientAcceptSelfSignedServerCertServerRejectsIncorrectClientCert() throws Exception {
        // Client accept self-signed cert, server rejects invalid client cert

        SSLContext sslContext = createSSLContextBuilder("ssl/client/clistore_invalid.p12", "invalid", null)
                .loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE)
                .build();
        SSLHandshakeException sslHandshakeException = assertThrows(SSLHandshakeException.class,
                () -> executeGet(sslContext, "/api/health"),
                "Expected SSLHandshakeException, but was not thrown"
        );
        assertThat(sslHandshakeException.getMessage()).isEqualTo("Received fatal alert: certificate_unknown");
    }

    @Test
    void trustingSelfSignedCertInClientSucceeds() throws Exception {
        SSLContext sslContext = createSSLContextBuilder("ssl/client/clistore.p12", "bds", null)
                .loadTrustMaterial(null, TrustSelfSignedStrategy.INSTANCE)
                .build();

        CloseableHttpResponse response = executeGet(sslContext, "/api/health");
        assertThat(getBody(response)).isEqualTo("OK");
    }

    @Test
    void serverAcceptsClientCert() throws Exception {
        SSLContext sslContext = createSSLContextBuilder("ssl/client/clistore.p12", "bds", "ssl/client/truststore.p12")
                .build();

        CloseableHttpResponse response = executeGet(sslContext, "/api/health");
        assertThat(getBody(response)).isEqualTo("OK");
    }

    @Test
    void serverAcceptsOtherClientCert() throws Exception {
        SSLContext sslContext = createSSLContextBuilder("ssl/client/clistore2.p12", "other", "ssl/client/truststore.p12")
                .build();

        CloseableHttpResponse response = executeGet(sslContext, "/api/health");
        assertThat(getBody(response)).isEqualTo("OK");
    }

    @Test
    void serverRejectsThirdClientCert() throws Exception {
        SSLContext sslContext = createSSLContextBuilder("ssl/client/clistore_invalid.p12", "invalid", "ssl/client/truststore.p12")
                .build();

        SSLHandshakeException sslHandshakeException = assertThrows(SSLHandshakeException.class,
                () -> executeGet(sslContext, "/api/health"),
                "Expected SSLHandshakeException, but was not thrown"
        );
        assertThat(sslHandshakeException.getMessage()).isEqualTo("Received fatal alert: certificate_unknown");
    }
}

