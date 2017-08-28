import setup.Constants;
import setup.PathUtil;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;



/**
 * this new variant of CertGenerator will invoke "keytool" instead of using internal classes.
 * <p>
 * invocation can happen in different ways, currently "main" is executed via reflection (assuming class is "just there" at runtime).
 * Even more defensive would be to use ProcessBuilder to run keytool command in separate process.
 * <p>
 * this might also be an option https://github.com/mojohaus/keytool
 * <p>
 * Note that all this ugliness is caused by the fact that keytool does not offer a proper ("non command line") API
 */
public class CertGeneratorKT {

    public static void main(String[] args) throws Exception {



        // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html
        // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html


        // mimic some behavior of sun.security.tools.keytool.Main
        // A: Main.doGenKeyPair .. Generate self signed certificate
        // B: Main.doCertReq .. generate CSR
        // C: Main.doGenCert Read PKCS10 request from in, and print certificate to out

        // A: Generate self signed certificate

//        invokeKeyTool("--help"); // works OK. help is written on "stderr"
//        invokeKeyTool("-list"); // command errors are printed to "stdout", keytool does elegant System.exit(1)
//        invokeKeyTool("-debug -list"); // this throws an exception


        // args[0] in pom:  ${project.build.directory}
        // path will be "../cert/target/classes"
        String path = PathUtil.getTargetPath(CertGeneratorKT.class) + "classes/";

        invokeKeyTool("-genkeypair" +
                       " -keystore " + path + Constants.TLS_SERVER_KEY_STORE +
                       " -storepass " + Constants.TLS_SERVER_KEY_PASS +
                       " -validity " + 365 * Constants.TLS_CERT_VALIDITY_YEARS +
                       " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
                       " -keyalg EC" +
                       " -sigalg SHA256WithECDSA" +
                       " -keysize 256" +
                       " -keypass " + Constants.TLS_SERVER_KEY_PASS +
                       " -dname CN=server01"

        );

        invokeKeyTool("-exportcert" +
                       " -keystore " + path + Constants.TLS_SERVER_KEY_STORE +
                       " -storepass " + Constants.TLS_SERVER_KEY_PASS +
                       " -alias " + Constants.TLS_SERVER_KEY_ALIAS +
                       " -file " + path + "server.pub"  // what format?
        );

        if (0==0) {
            return;
        }

        CertAndKeyGen keyGen = new CertAndKeyGen("EC", "SHA256WithECDSA");
        //   256 : secp256r1, 384: secp384r1, cannot change curve easily
        int keyBits = Constants.TLS_CERT_KEY_LENGTH;
        keyGen.generate(keyBits);
        PrivateKey serverPrivate = keyGen.getPrivateKey();

        int years = Constants.TLS_CERT_VALIDITY_YEARS;
        long validity = years * 365 * 24 * 3600; // a year in seconds
        // validity starts "now"
        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=server01"), validity);
//        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=" + Constants.TLS_SERVER_DOMAIN), validity);
        System.out.println(server);

        boolean useClientCert = args.length > 0;

        // client certificate (issued by server?)
        keyGen.generate(keyBits);
        PrivateKey clientPrivate = keyGen.getPrivateKey();
        // create certificate signing request (CSR)
        // PKCS10 request = keyGen.getCertRequest(new X500Name("CN=client01"));

        X509Certificate client = keyGen.getSelfCertificate(new X500Name("CN=client01"), validity);
        System.out.println(client);

        // todo: create CA for multiple clients


        String serverAlias = Constants.TLS_SERVER_KEY_ALIAS;
        String clientAlias = "client";

        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null); // initialize
        String fileName = path + Constants.TLS_SERVER_KEY_STORE;
        char[] password = Constants.TLS_SERVER_KEY_PASS.toCharArray();
        // server as key
        keyStore.setKeyEntry(serverAlias, serverPrivate, password, new Certificate[] {server});
        keyStore.store(new FileOutputStream(fileName), password); // auto flush with vm exit is ugly

        // client trusts server
        keyStore.load(null, null); // initialize again
        String fileName2 = path + Constants.TLS_CLIENT_TRUST_STORE;
        char[] password2 = Constants.TLS_CLIENT_TRUST_PASS.toCharArray();
        // server as certificate
        keyStore.setCertificateEntry(serverAlias, server);
        keyStore.store(new FileOutputStream(fileName2), password2);

        if (useClientCert) {
            keyStore.load(null, null); // initialize again
            String fileName3 = path + Constants.TLS_CLIENT_KEY_STORE;
            char[] password3 = Constants.TLS_CLIENT_KEY_PASS.toCharArray();
            // client as key
            keyStore.setKeyEntry(clientAlias, clientPrivate, password3, new Certificate[] {client});
            keyStore.store(new FileOutputStream(fileName3), password3);

            // server trusts client
            keyStore.load(null, null); // initialize again
            String fileName4 = path + Constants.TLS_SERVER_TRUST_STORE;
            char[] password4 = Constants.TLS_SERVER_TRUST_PASS.toCharArray();
            // client as key
            keyStore.setCertificateEntry(clientAlias, client);
            keyStore.store(new FileOutputStream(fileName4), password4);
        }

    }


    /**
     * invoke keytool via reflection
     * @param argLine blank separated list of parameters (like on command line)
     */
    private static void invokeKeyTool(String argLine) throws Exception {
        System.out.println("using argLine: " + argLine);
        invokeKeyTool(argLine.split(" "));
    }

    /**
     * invoke keytool via reflection
     * @param args args to hand over to keytool
     */
    private static void invokeKeyTool(String[] args) throws Exception {
        Class clazz = Class.forName("sun.security.tools.keytool.Main");
        Method meth = clazz.getMethod("main", String[].class);
        meth.invoke(null, (Object) args); // static method doesn't have an instance
    }
}
