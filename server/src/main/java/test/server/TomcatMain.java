package test.server;

import com.caucho.hessian.server.HessianServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import setup.Constants;

import javax.servlet.Servlet;
import java.io.File;

/**
 * start server as embedded tomcat
 */
public class TomcatMain {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        // needed for "work" dir etc..
//        tomcat.setBaseDir(System.getProperty("java.io.tmpdir") + File.separator + "tomcat.tmp");
        tomcat.setBaseDir("./target/tomcat/tmp");

        tomcat.setPort(8080);

        boolean useTLS = args.length > 0;
        boolean useClientCert = args.length > 1;

        if (useTLS) {
            Connector connector = tomcat.getConnector();
            // https://tomcat.apache.org/tomcat-8.0-doc/config/http.html
            connector.setPort(Constants.TLS_SERVER_PORT);
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setAttribute("SSLEnabled", true);
            connector.setAttribute("keyAlias", "server");
            connector.setAttribute("keystorePass", Constants.TLS_SERVER_KEY_PASS);
            connector.setAttribute("keystoreType", "JKS");
            // TODO: fix unstable hack. tomcat cannot load this from classpath, why?
            // path is relative to baseDir
            connector.setAttribute("keystoreFile", "../../../cert/target/classes/serverKey.jks");
            connector.setAttribute("truststorePass", Constants.TLS_SERVER_TRUST_PASS);
            connector.setAttribute("truststoreType", "JKS");
            connector.setAttribute("truststoreFile", "../../../cert/target/classes/serverTrust.jks");
            if (useClientCert) {
                // "Set to true if you want the SSL stack to require a valid
                // certificate chain from the client before accepting a connection."
                connector.setAttribute("clientAuth", "true");
            }
            connector.setAttribute("sslProtocol", "TLSv1.2");
            connector.setAttribute("protocol", "HTTP/1.1");

            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
            // pick one single "proper" cipher suite
            connector.setAttribute("ciphers", Constants.TLS_SUITE);
//                     "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"); // working OK
//                     "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"); // requires unlimited crypto!
        }


        File docBase = new File("./target/tomcat/docs");  // only needed for "statics", just use working path path for now
        docBase.mkdirs();
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
