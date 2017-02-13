package test.server;

import com.caucho.hessian.server.HessianServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.Servlet;
import java.io.File;

/**
 * start server as embedded tomcat
 */
public class TomcatMain {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        boolean useTLS = args.length > 0;
        boolean useClientCert = args.length > 1;

        if (useTLS) {
            Connector connector = tomcat.getConnector();
            // https://tomcat.apache.org/tomcat-8.0-doc/config/http.html
            connector.setPort(8443);
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setAttribute("SSLEnabled", true);
            connector.setAttribute("keyAlias", "server");
            connector.setAttribute("keystorePass", "secret");
            connector.setAttribute("keystoreType", "JKS");
            // TODO: fix unstable hack. tomcat cannot load this from classpath, why?
            connector.setAttribute("keystoreFile", "../cert/target/classes/serverKey.jks");
            connector.setAttribute("truststorePass", "secret4");
            connector.setAttribute("truststoreType", "JKS");
            connector.setAttribute("truststoreFile", "../cert/target/classes/serverTrust.jks");
            if (useClientCert) {
                // "Set to true if you want the SSL stack to require a valid
                // certificate chain from the client before accepting a connection."
                connector.setAttribute("clientAuth", "true");
            }
            connector.setAttribute("sslProtocol", "TLSv1.2");
            connector.setAttribute("protocol", "HTTP/1.1");

            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
            // pick one single "proper" cipher suite
            connector.setAttribute("ciphers",
                     "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"); // working OK
//                     "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"); // requires unlimited crypto!
        }

        // needed for "work" dir etc..
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir") + File.separator + "tomcat.tmp");

        File docBase = new File(".");  // only needed for "statics", just use working path path for now
        Context ctx = tomcat.addContext("/app", docBase.getAbsolutePath());

        String servletName = "foo-servlet";
        Servlet servlet = new HessianServlet();
        Wrapper wrapper = Tomcat.addServlet(ctx, servletName, servlet);
        // same parameters as in web.xml
        wrapper.addInitParameter("home-api", "test.api.FooService");
        wrapper.addInitParameter("home-class", "test.server.FooServiceImpl");
        wrapper.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/foo", servletName);

        // add log filter
        String filterName ="log";
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(LogFilter.class.getName());
        ctx.addFilterDef(filterDef);

        // map log filter
        FilterMap filterMap = new FilterMap();
        filterMap.addServletName(servletName);
        filterMap.addURLPattern("*");
        filterMap.setFilterName(filterName);
        ctx.addFilterMap(filterMap);

        tomcat.start();
        tomcat.getServer().await();

    }
}
