package test.client;


import com.caucho.hessian.client.HessianProxyFactory;
import test.api.FooService;


public class FooClient {

    public static void main(String[] args) throws Exception {

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        FooService service;

        if (args.length > 0) {

            // ssl config for commons http

//            String[] TLS_ENABLED_PROTOCOLS = new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"};
//            String cipherSuite = null; // or e.g. "TLS_RSA_WITH_AES_128_CBC_SHA256";
//
//
//            // Trust own CA and all self-signed certs
//            final SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(truststoreFile, truststorePassphrase, new
//                    TrustSelfSignedStrategy()).loadKeyMaterial(keystoreFile, keystorePassphrase, keystorePassphrase).build();
//            // define the allowed Cipher Suites
//            final String[] cipherSuites;
//            if (cipherSuite == null) {
//                cipherSuites = sslcontext.getSupportedSSLParameters().getCipherSuites();
//            } else {
//                cipherSuites = new String[]{cipherSuite};
//            }
//            // only allow TLS protocol versions and, if applicable restrict cipher suites
//            final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslcontext, TLS_ENABLED_PROTOCOLS, cipherSuites,
//                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
//
//            final CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();

            // TODO: not elegant via system properties
            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html
            System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
            System.setProperty("javax.net.ssl.trustStore", "cert/target/classes/client.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "secret2");
            service = (FooService) proxyFactory.create(FooService.class, "https://localhost:8443/app/foo");
        }
        else {
            service = (FooService) proxyFactory.create(FooService.class, "http://localhost:8080/app/foo");
        }

        System.out.println(new String(service.getBytes(), "UTF8"));

    }

}
