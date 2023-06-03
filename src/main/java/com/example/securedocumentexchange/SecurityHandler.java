package com.example.securedocumentexchange;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;

public interface SecurityHandler {
//     private void encryptDocument(File document, File openKey) throws IOException, GeneralSecurityException {
//        byte[] message;
//        FileInputStream fis = new FileInputStream(document);
//        message = fis.readAllBytes();
//        fis.close();
//        System.out.println(message);
//        SshPublicKey sshPublicKey = SshKeyUtils.getPublicKey(openKey);
//        publicKey = sshPublicKey.getJCEPublicKey();
//        Key generatedAes = generateFromString(128);
//        IvParameterSpec iv = generatedIv(generatedAes.getEncoded().length);
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.ENCRYPT_MODE, generatedAes, new GCMParameterSpec(128, iv.getIV()));
//        byte[] encryptedBytes = cipher.doFinal(message);
//        cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA256ANDMGF1PADDING");
//     }
}
