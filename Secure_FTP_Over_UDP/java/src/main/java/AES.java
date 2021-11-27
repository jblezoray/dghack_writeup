import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class AES {
    private static final String ENCRYPT_ALGO = "AES/CBC/PKCS5Padding";

    private static byte[] generateArrayOfZeros(int size) {
        byte[] array = new byte[size];
        byte zero = 0x00;
        Arrays.fill(array, zero);
        return array;
    }

    public static byte[] generateKey() {
        return generateArrayOfZeros(16); // 128 bits = 16 bytes
    }

    public static byte[] generateIv() {
        return generateArrayOfZeros(16); // 128 bits = 16 bytes
    }

    public static byte[] decrypt(byte[] key, byte[] initVector, byte[] encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            var cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] plainText = cipher.doFinal(encrypted);
            return plainText;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(byte[] key, byte[] initVector, byte[] clearText) {
        try {
            var iv = new IvParameterSpec(initVector);
            var skeySpec = new SecretKeySpec(key, "AES");

            var cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(clearText);
            return encrypted;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
