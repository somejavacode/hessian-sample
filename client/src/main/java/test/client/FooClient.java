package test.client;


import com.caucho.hessian.client.HessianProxyFactory;
import setup.Constants;
import test.api.FooService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class FooClient {

    public static void main(String[] args) throws Exception {

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        FooService service;

        boolean useTLS = args.length > 0;
        boolean useClientCert = args.length > 1;

        if (useTLS) {
            // TODO: not elegant via system properties
            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html

            System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
            System.setProperty("javax.net.ssl.trustStore", "cert/target/classes/clientTrust.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", Constants.TLS_CLIENT_TRUST_PASS);
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
            System.setProperty("https.protocols", "TLSv1.2");
            System.setProperty("https.cipherSuites",  Constants.TLS_SUITE);
            // logs a lot...
            System.setProperty("javax.net.debug", "ssl");

            // disable host name validation ....
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            if (useClientCert) {
                System.setProperty("javax.net.ssl.keyStore", "cert/target/classes/clientKey.jks");
                System.setProperty("javax.net.ssl.keyStorePassword", Constants.TLS_CLIENT_KEY_PASS);
            }
            String url = "https://" + Constants.TLS_SERVER_DOMAIN + ":" + Constants.TLS_SERVER_PORT + "/app/foo";
            service = (FooService) proxyFactory.create(FooService.class, url);
        }
        else {
            service = (FooService) proxyFactory.create(FooService.class, "http://localhost:8080/app/foo");
        }

        System.out.println(new String(service.getBytes(), "UTF8"));
    }


}
