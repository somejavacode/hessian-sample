import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import sun.security.tools.keytool.CertAndKeyGen; // using keytool here...
import sun.security.x509.X500Name;


public class CertGenerator {

    public static void main(String[] args) throws Exception {

        CertAndKeyGen keyGen = new CertAndKeyGen("EC", "SHA256WithECDSA");
        keyGen.generate(384);  // in fact: secp384r1, cannot change curve easily
//        keyGen.generate(256);  // secp256r1

        //Generate self signed certificate
        int years = 2;
        long validity = years * 365 * 24 * 3600; // a year in seconds
        // validity starts "now"
//        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=server01"), validity);
        X509Certificate server = keyGen.getSelfCertificate(new X500Name("CN=localhost"), validity);
        System.out.println(server);

//        TODO client certificate
//        X509Certificate client = keyGen.getSelfCertificate(new X500Name("CN=client01"), validity);
//        System.out.println(client);

        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(null, null); // initialize
        String alias = "server";
        String fileName = "server.jks";
        char[] password = "secret".toCharArray();
        keyStore.setKeyEntry(alias, keyGen.getPrivateKey(), password, new Certificate[] {server});
        keyStore.store(new FileOutputStream(fileName), password);

        keyStore.load(null, null); // initialize again
        String fileName2 = "client.jks";
        char[] password2 = "secret2".toCharArray();
        keyStore.setCertificateEntry(alias, server); // server as certificate
        keyStore.store(new FileOutputStream(fileName2), password2);

    }
}
