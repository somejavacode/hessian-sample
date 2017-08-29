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

        boolean useClientCert = args.length > 0;
        boolean useClientCertChain = args.length > 1;

        // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html

        // could also be argument ${project.build.directory} in pom.xml, but would not work with shaded jar.
        String path = PathUtil.getTargetPath(CertGeneratorKT.class) + "classes/";

        String certGenParam =
                " -validity " + Constants.TLS_CERT_VALIDITY_DAYS +
                " -keyalg " + Constants.TLS_CERT_KEY_ALGORITM +
                " -sigalg " + Constants.TLS_CERT_SIGNATURE_ALGORITHM +
                " -keysize " + Constants.TLS_CERT_KEY_LENGTH;

        invokeKeyTool("-genkeypair" +
                " -keystore " + path + Constants.TLS_SERVER_KEY_STORE +
                " -storepass " + Constants.TLS_SERVER_KEY_PASS +
                certGenParam +
                " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
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

        if (useClientCert) {
            // make this a CA for chain certificates
            String ext = useClientCertChain ? " -ext BC:critical=ca:true -ext KU:critical=keyCertSign" : "";
            invokeKeyTool("-genkeypair" +
                    " -keystore " + path + Constants.TLS_CLIENT_KEY_STORE +
                    " -storepass " + Constants.TLS_CLIENT_KEY_PASS +
                    certGenParam +
                    " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                    " -keypass " + Constants.TLS_CLIENT_KEY_PASS +
                    " -dname CN=client01" +
                    ext
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
            // use client cert as "CA", request certificate to create "chain"
            if (useClientCertChain) {
                // crate chain certificate
                invokeKeyTool("-genkeypair" +
                        " -keystore " + path + Constants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS +
                        certGenParam +
                        " -keypass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -alias " + Constants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -dname CN=client01chain"
                );
                // crate CSR (Certificate Signing Request) for chain certificate
                invokeKeyTool("-certreq" +
                        " -keystore " + path + Constants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -alias " + Constants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -file " + path + Constants.TLS_CLIENT_CHAIN_CSR
                );
                // client CA confirms request
                invokeKeyTool("-gencert" +
                        " -keystore " + path + Constants.TLS_CLIENT_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_KEY_PASS +
                        " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                        " -infile " + path + Constants.TLS_CLIENT_CHAIN_CSR +
                        " -outfile " + path + Constants.TLS_CLIENT_CHAIN_CERT
                        // " -ext " // do we need an extension?
                );

                // also import root
                invokeKeyTool("-importcert" +
                        " -keystore " + path + Constants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -alias " + Constants.TLS_CLIENT_KEY_ALIAS +
                        " -file " + path + Constants.TLS_CLIENT_CERT +
                        " -trustcacerts" +
                        " -noprompt"
                );

                // import certificate in client chain store
                invokeKeyTool("-importcert" +
                        " -keystore " + path + Constants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS +
                        // must use same alias: "Certificate reply was installed in keystore"
                        " -alias " + Constants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -file " + path + Constants.TLS_CLIENT_CHAIN_CERT +
                        " -trustcacerts" +
                        " -noprompt"
                );
                // finally list all entries
                invokeKeyTool("-list" +
                        " -keystore " + path + Constants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + Constants.TLS_CLIENT_CHAIN_KEY_PASS
                );
            }
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
