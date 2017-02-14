import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import setup.Constants;
import sun.security.tools.keytool.CertAndKeyGen; // using keytool classes here...
import sun.security.x509.X500Name;


public class CertGenerator {

    public static void main(String[] args) throws Exception {

        // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html
        // TODO: maybe invoke Main.main("") or "Runtime.exec("keytool", "...")"

        // mimic some behavior of sun.security.tools.keytool.Main
        // A: Main.doGenKeyPair .. Generate self signed certificate
        // B: Main.doCertReq .. generate CSR
        // C: Main.doGenCert Read PKCS10 request from in, and print certificate to out

        // A: Generate self signed certificate


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

        String path = ""; // current working directory

        String mvnPath = System.getProperty("maven.multiModuleProjectDirectory");
        if (mvnPath != null) {
            // TODO: this ins only working with "mvn package" in project root path.
            path = mvnPath + "/cert/target/classes/";
        }

        String serverAlias = "server";
        String clientAlias = "client";

        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null); // initialize
        String fileName = path + "serverKey.jks";
        char[] password = Constants.TLS_SERVER_KEY_PASS.toCharArray();
        // server as key
        keyStore.setKeyEntry(serverAlias, serverPrivate, password, new Certificate[] {server});
        keyStore.store(new FileOutputStream(fileName), password); // auto flush with vm exit is ugly

        // client trusts server
        keyStore.load(null, null); // initialize again
        String fileName2 = path + "clientTrust.jks";
        char[] password2 = Constants.TLS_CLIENT_TRUST_PASS.toCharArray();
        // server as certificate
        keyStore.setCertificateEntry(serverAlias, server);
        keyStore.store(new FileOutputStream(fileName2), password2);

        if (useClientCert) {
            keyStore.load(null, null); // initialize again
            String fileName3 = path + "clientKey.jks";
            char[] password3 = Constants.TLS_CLIENT_KEY_PASS.toCharArray();
            // client as key
            keyStore.setKeyEntry(clientAlias, clientPrivate, password3, new Certificate[] {client});
            keyStore.store(new FileOutputStream(fileName3), password3);

            // server trusts client
            keyStore.load(null, null); // initialize again
            String fileName4 = path + "serverTrust.jks";
            char[] password4 = Constants.TLS_SERVER_TRUST_PASS.toCharArray();
            // client as key
            keyStore.setCertificateEntry(clientAlias, client);
            keyStore.store(new FileOutputStream(fileName4), password4);
        }

    }
}
