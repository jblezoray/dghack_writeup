import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class RSA {

    public static byte[] encrypt(byte[] publicKey, byte[] clearText) {
        try {
            var publicKeySpec = new X509EncodedKeySpec(publicKey);
            var keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(publicKeySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(clearText);
            return encrypted;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
