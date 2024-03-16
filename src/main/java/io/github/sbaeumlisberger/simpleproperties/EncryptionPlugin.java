package io.github.sbaeumlisberger.simpleproperties;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptionPlugin implements PropertiesPlugin {
    public static final String DEFAULT_ALGORITHM = "AES/CBC/PKCS5Padding";

    public static final String PREFIX = "{enc}";

    private final Cipher cipherDecrypt;
    private final Cipher cipherEncrypt;

    public EncryptionPlugin(SecretKey key) {
        this(key, DEFAULT_ALGORITHM, new SecureRandom());
    }

    public EncryptionPlugin(SecretKey key, String algorithm, SecureRandom secureRandom) {
        try {
            this.cipherDecrypt = Cipher.getInstance(algorithm);
            cipherDecrypt.init(Cipher.DECRYPT_MODE, key, secureRandom);

            this.cipherEncrypt = Cipher.getInstance(algorithm);
            cipherEncrypt.init(Cipher.ENCRYPT_MODE, key, secureRandom);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public EncryptionPlugin(Cipher cipherDecrypt, Cipher cipherEncrypt) {
        this.cipherDecrypt = cipherDecrypt;
        this.cipherEncrypt = cipherEncrypt;
    }

    @Override
    public String onPropertyRead(String key, String value) {
        if (!value.startsWith(PREFIX)) {
            return value;
        }
        try {
            return new String(cipherDecrypt.doFinal(value.substring(PREFIX.length()).getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    @Override
    public String onPropertyWrite(String key, String value) {
        try {
            return PREFIX + new String(cipherEncrypt.doFinal(value.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
