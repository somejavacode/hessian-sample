import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import sun.security.tools.keytool.CertAndKeyGen; // using keytool here...
import sun.security.x509.X500Name;


public class CertGenerator {

    public static void main(String[] args) throws Exception {

        CertAndKeyGen keyGen = new CertAndKeyGen("EC", "SHA256WithECDSA");
        keyGen.generate(384); // in fact: secp384r1, cannot change curve easily
//        keyGen.generate(256);  // secp256r1
        PrivateKey serverPrivate = keyGen.getPrivateKey();

        //Generate self signed certificate
        int years = 2;
        long validity = years * 365 * 24 * 3600; // a year in seconds
        // validity starts "now"
//        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=server01"), validity);
        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=localhost"), validity);
        System.out.println(server);

        boolean useClient = args.length > 0;

        // client certificate
        keyGen.generate(384); // otherwise same key?
        PrivateKey clientPrivate = keyGen.getPrivateKey();
        X509Certificate client = keyGen.getSelfCertificate(new X500Name("CN=client01"), validity);
        System.out.println(client);


        String path = ""; // current working directory
//        System.getProperties().list(System.out);

        String mvnPath = System.getProperty("maven.multiModuleProjectDirectory");
        if (mvnPath != null) {
            // TODO: this ins only working with "mvn package" in project root path.
            path = mvnPath + "/cert/target/classes/";
        }

        String serverAlias = "server";
        String clientAlias = "client";

        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null); // initialize
        String fileName = path + "server.jks";
        char[] password = "secret".toCharArray();
        // server as key
        keyStore.setKeyEntry(serverAlias, serverPrivate, password, new Certificate[]{server});
        if (useClient) {
            // client as certificate
            keyStore.setCertificateEntry(clientAlias, client);
        }
        keyStore.store(new FileOutputStream(fileName), password); // auto flush with vm exit is ugly

        keyStore.load(null, null); // initialize again
        String fileName2 = path + "client.jks";
        char[] password2 = "secret2".toCharArray();
        // server as certificate
        keyStore.setCertificateEntry(serverAlias, server);
        if (useClient) {
            // client as key
            keyStore.setKeyEntry(clientAlias, clientPrivate, password2, new Certificate[]{client});
        }
        keyStore.store(new FileOutputStream(fileName2), password2);

    }
}
