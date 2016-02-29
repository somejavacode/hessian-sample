package test.client;


import com.caucho.hessian.client.HessianProxyFactory;
import test.api.FooService;

public class FooClient {

    public static void main(String[] args) throws Exception {

        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        FooService service = (FooService) proxyFactory.create(FooService.class, "http://localhost:8080/app/foo");

        System.out.println(new String(service.getBytes(), "UTF8"));

    }

}
