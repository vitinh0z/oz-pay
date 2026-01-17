package com.ozpay.infra.security;

import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class CryptoService {

    private final String SECRET_KEY = "forabolsonaro";

    public String encrypted (String data){

        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));

        } catch (InvalidKeyException
                 | NoSuchPaddingException
                 | NoSuchAlgorithmException
                 | IllegalBlockSizeException
                 | BadPaddingException e
        ) {
            throw new RuntimeException(e);
        }
    }

    public String descrypt(String data){

        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedBytes = Base64.getDecoder().decode(data);
            return new String(cipher.doFinal(decodedBytes));

        } catch (NoSuchPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | InvalidKeyException e
        ){
            throw new RuntimeException(e);
        }
    }
}
