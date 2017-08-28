package setup;

public class Constants {

    public static final int TLS_CERT_KEY_LENGTH = 256;
    public static final String TLS_CERT_KEY_ALGORITM = "EC";
    public static final String TLS_CERT_SIGNATURE_ALGORITHM = "SHA256WithECDSA";
    public static final int TLS_CERT_VALIDITY_YEARS = 2;
    public static final String TLS_SUITE = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";
    public static final String TLS_SERVER_DOMAIN = "localhost";
    public static final int TLS_SERVER_PORT = 8443;

    public static final String TLS_SERVER_KEY_ALIAS = "server1";
    public static final String TLS_SERVER_KEY_STORE = "serverKey.jks";
    public static final String TLS_SERVER_KEY_PASS = "secret1";
    public static final String TLS_SERVER_CERT = "server.cert";
    public static final String TLS_SERVER_TRUST_STORE = "serverTrust.jks";
    public static final String TLS_SERVER_TRUST_PASS = "secret2";

    public static final String TLS_CLIENT_KEY_ALIAS = "client1";
    public static final String TLS_CLIENT_KEY_STORE = "clientKey.jks";
    public static final String TLS_CLIENT_KEY_PASS = "secret3";
    public static final String TLS_CLIENT_CERT = "client.cert";
    public static final String TLS_CLIENT_TRUST_STORE = "clientTrust.jks";
    public static final String TLS_CLIENT_TRUST_PASS = "secret4";

}
