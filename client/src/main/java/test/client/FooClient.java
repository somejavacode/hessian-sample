package test.client;


import com.caucho.hessian.client.HessianProxyFactory;
import test.api.FooService;

public class FooClient {

    public static void main(String[] args) throws Exception {

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        FooService service;

        if (args.length > 0) {
            // TODO: not elegant via system properties
            System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
            System.setProperty("javax.net.ssl.trustStore", "client.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "secret2");
            service = (FooService) proxyFactory.create(FooService.class, "https://localhost:8443/app/foo");
        }
        else {
            service = (FooService) proxyFactory.create(FooService.class, "http://localhost:8080/app/foo");
        }

        System.out.println(new String(service.getBytes(), "UTF8"));

    }

}
