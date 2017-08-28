import setup.Constants;
import setup.PathUtil;

import java.lang.reflect.Method;

/**
 * this  CertGenerator will invoke "keytool"
 * <p>
 * Invocation happens currently via reflection (assuming class is "just there" at runtime).
 * More defensive would be to use ProcessBuilder to run keytool command.
 * <p>
 * Note that all this ugliness is caused by the fact that keytool does not offer a proper ("non command line") API
 */
public class CertGeneratorKT {

    public static void main(String[] args) throws Exception {

        // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html

        // could also be argument ${project.build.directory} in pom.xml, but would not work with shaded jar.
        String path = PathUtil.getTargetPath(CertGeneratorKT.class) + "classes/";

        invokeKeyTool("-genkeypair" +
                " -keystore " + path + Constants.TLS_SERVER_KEY_STORE +
                " -storepass " + Constants.TLS_SERVER_KEY_PASS +
                " -validity " + 365 * Constants.TLS_CERT_VALIDITY_YEARS +
                " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
                " -keyalg " + Constants.TLS_CERT_KEY_ALGORITM +
                " -sigalg " + Constants.TLS_CERT_SIGNATURE_ALGORITHM +
                " -keysize " + Constants.TLS_CERT_KEY_LENGTH +
                " -keypass " + Constants.TLS_SERVER_KEY_PASS +
                " -dname CN=server01"
        );

        invokeKeyTool("-exportcert" +
                " -keystore " + path + Constants.TLS_SERVER_KEY_STORE +
                " -storepass " + Constants.TLS_SERVER_KEY_PASS +
                " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
                " -file " + path + Constants.TLS_SERVER_CERT  // what format?
        );

        invokeKeyTool("-importcert" +
                " -keystore " + path + Constants.TLS_CLIENT_TRUST_STORE +
                " -storepass " + Constants.TLS_CLIENT_TRUST_PASS +
                " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
                " -file " + path + Constants.TLS_SERVER_CERT +
                " -trustcacerts" +
                " -noprompt"
        );

        if (args.length > 0) {
            invokeKeyTool("-genkeypair" +
                    " -keystore " + path + Constants.TLS_CLIENT_KEY_STORE +
                    " -storepass " + Constants.TLS_CLIENT_KEY_PASS +
                    " -validity " + 365 * Constants.TLS_CERT_VALIDITY_YEARS +
                    " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                    " -keyalg " + Constants.TLS_CERT_KEY_ALGORITM +
                    " -sigalg " + Constants.TLS_CERT_SIGNATURE_ALGORITHM +
                    " -keysize " + Constants.TLS_CERT_KEY_LENGTH +
                    " -keypass " + Constants.TLS_CLIENT_KEY_PASS +
                    " -dname CN=client01"
            );

            invokeKeyTool("-exportcert" +
                    " -keystore " + path + Constants.TLS_CLIENT_KEY_STORE +
                    " -storepass " + Constants.TLS_CLIENT_KEY_PASS +
                    " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                    " -file " + path + Constants.TLS_CLIENT_CERT
            );

            invokeKeyTool("-importcert" +
                    " -keystore " + path + Constants.TLS_SERVER_TRUST_STORE +
                    " -storepass " + Constants.TLS_SERVER_TRUST_PASS +
                    " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                    " -file " + path + Constants.TLS_CLIENT_CERT +
                    " -trustcacerts" +
                    " -noprompt"
            );
        }
    }

    /**
     * invoke keytool via reflection
     *
     * @param argLine blank separated list of parameters (like on command line)
     */
    private static void invokeKeyTool(String argLine) throws Exception {
        System.out.println("using argLine: " + argLine);
        String[] args = argLine.split(" ");
        Class<?> clazz = Class.forName("sun.security.tools.keytool.Main");
        Method method = clazz.getMethod("main", String[].class);
        method.invoke(null, (Object) args);
    }
}
