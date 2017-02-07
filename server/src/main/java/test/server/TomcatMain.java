package test.server;

import com.caucho.hessian.server.HessianServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.Servlet;
import java.io.File;

/**
 * start server as embedded tomcat
 */
public class TomcatMain {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        if (args.length > 0) {
//            Connector connector = new Connector();
            Connector connector = tomcat.getConnector();
            connector.setPort(8443);
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setAttribute("SSLEnabled", true);
            connector.setAttribute("keyAlias", "server");
            connector.setAttribute("keystorePass", "secret");
            connector.setAttribute("keystoreType", "JKS");
            connector.setAttribute("keystoreFile", "../server.jks"); // hack
            connector.setAttribute("clientAuth", "false");
            connector.setAttribute("sslProtocol", "TLS");
            connector.setAttribute("protocol", "HTTP/1.1");
            connector.setAttribute("maxThreads", "200");
//            tomcat.getService().addConnector(connector);
        }

//        tomcat.getService().addConnector(connector);
        // needed for "work" dir etc..
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir") + File.separator + "tomcat.tmp");

        File docBase = new File(".");  // only needed for "statics", just use working path path for now
        Context ctx = tomcat.addContext("/app", docBase.getAbsolutePath());

        final String servletName = "foo-servlet";
        Servlet servlet = new HessianServlet();
        Wrapper wrapper = Tomcat.addServlet(ctx, servletName, servlet);
        // same parameters as in web.xml
        wrapper.addInitParameter("home-api", "test.api.FooService");
        wrapper.addInitParameter("home-class", "test.server.FooServiceImpl");
        wrapper.setLoadOnStartup(1);

        ctx.addServletMappingDecoded("/foo", servletName);

        tomcat.start();
        tomcat.getServer().await();

    }
}
