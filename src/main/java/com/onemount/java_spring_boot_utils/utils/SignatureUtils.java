package com.onemount.java_spring_boot_utils.utils;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.base64url.Base64;
import org.jose4j.keys.RsaKeyUtil;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@Slf4j
public class SignatureUtils {

    private static final String SHA_256_WITH_RSA = "SHA256withRSA";
    private static final String PRIVATE_KEY_BEGIN = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PRIVATE_KEY_END = "-----END RSA PRIVATE KEY-----";
    private static final String RSA = "RSA";

    /**
     * Verifies a SHA-256 with RSA signature.
     *
     * @param publicKey PEM-encoded public key
     * @param signature Base64 encoded signature
     * @param data      Data to verify
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verifySHA256WithRSA(String publicKey, String signature, String data) throws Exception {
        PublicKey pub = new RsaKeyUtil().fromPemEncoded(publicKey);
        Signature dsa = Signature.getInstance(SHA_256_WITH_RSA);
        dsa.initVerify(pub);
        dsa.update(data.getBytes());
        return dsa.verify(Base64.decode(signature));
    }

    /**
     * Signs a message using SHA-256 with RSA.
     *
     * @param privateKey PEM-encoded private key
     * @param message    Message to sign
     * @return Base64 encoded signature
     */
    public static String signSHA256WithRSA(String privateKey, String message) throws Exception {
        PrivateKey privateKeyObj = getPrivateKeyFromPem(privateKey);
        Signature dsa = Signature.getInstance(SHA_256_WITH_RSA);
        dsa.initSign(privateKeyObj);
        dsa.update(message.getBytes());
        return Base64.encode(dsa.sign());
    }

    /**
     * Converts a PKCS#1 private key to PKCS#8 format and returns a PrivateKey object.
     *
     * @param pkcs1Bytes PKCS#1 encoded private key
     * @return PrivateKey object
     */
    private static PrivateKey getPrivateKeyFromPem(String privateKeyPem) throws Exception {
        String privateKeyStr = privateKeyPem.replace(PRIVATE_KEY_BEGIN, "")
                .replace(PRIVATE_KEY_END, "")
                .replaceAll("\\s", "");

        byte[] pkcs1Bytes = Base64.decode(privateKeyStr);
        byte[] pkcs8Bytes = convertPkcs1ToPkcs8(pkcs1Bytes);

        return readPkcs8PrivateKey(pkcs8Bytes);
    }

    /**
     * Converts a PKCS#1 private key to PKCS#8 format.
     *
     * @param pkcs1Bytes PKCS#1 private key bytes
     * @return PKCS#8 formatted private key bytes
     */
    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;

        byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff),
                0x2, 0x1, 0x0, 0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86,
                (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, 0x4, (byte) 0x82,
                (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff)
        };

        return join(pkcs8Header, pkcs1Bytes);
    }

    /**
     * Joins two byte arrays.
     *
     * @param byteArray1 First byte array
     * @param byteArray2 Second byte array
     * @return Joined byte array
     */
    private static byte[] join(byte[] byteArray1, byte[] byteArray2) {
        byte[] result = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, result, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, result, byteArray1.length, byteArray2.length);
        return result;
    }

    /**
     * Reads a PKCS#8 encoded private key.
     *
     * @param pkcs8Bytes PKCS#8 private key bytes
     * @return PrivateKey object
     */
    private static PrivateKey readPkcs8PrivateKey(byte[] pkcs8Bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
