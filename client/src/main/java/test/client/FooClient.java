package test.client;


import com.caucho.hessian.client.HessianProxyFactory;
import test.api.FooService;


public class FooClient {

    public static void main(String[] args) throws Exception {

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        FooService service;

        if (args.length > 0) {

            // TODO: not elegant via system properties
            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html

            System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
            System.setProperty("javax.net.ssl.trustStore", "cert/target/classes/clientTrust.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "secret2");
            // logs a lot...
            // System.setProperty("javax.net.debug", "ssl");
            if (args.length > 1) {
                // http://emo.sourceforge.net/cert-login-howto.html actually does create a client certificate (using "openssl")
                System.setProperty("javax.net.ssl.keyStore", "cert/target/classes/clientKey.jks");
                System.setProperty("javax.net.ssl.keyStorePassword", "secret3");
            }

            service = (FooService) proxyFactory.create(FooService.class, "https://localhost:8443/app/foo");
        }
        else {
            service = (FooService) proxyFactory.create(FooService.class, "http://localhost:8080/app/foo");
        }

        System.out.println(new String(service.getBytes(), "UTF8"));

    }

}
