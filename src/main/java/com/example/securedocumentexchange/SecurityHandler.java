package com.example.securedocumentexchange;

import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPublicKey;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;

public class SecurityHandler {
   public Path getPath(String initialFilePath, Path destDirPath){
      Path path = Path.of(String.valueOf(destDirPath), String.valueOf(Path.of(initialFilePath).getFileName()));
      return path;
   }
   public String encryptDocument(File document, Path pthToDir, SecretKey secretKey) throws IOException, GeneralSecurityException{
      String encryptedFileName = document.getName()+".sde";
      String encryptedFilePath = String.valueOf(pthToDir)+File.separator+encryptedFileName;
      System.out.println(encryptedFilePath);
      FileInputStream inputStream = new FileInputStream(document);
      FileOutputStream outputStream = new FileOutputStream(new File(encryptedFilePath));
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
      int bytesRead = 0;
      byte[] bytes = new byte[8];
      while((bytesRead = inputStream.read(bytes)) != -1){
         cipherOutputStream.write(bytes, 0, bytesRead);
      }
      cipherOutputStream.flush();
      cipherOutputStream.close();
      inputStream.close();
      return encryptedFilePath;
   }
   public String decryptDocument(File document, SecretKey secretKey) throws IOException, GeneralSecurityException{
      String decryptedFileName = document.getName().replace(".sde","");
      Path decryptedFilePath = document.toPath().resolveSibling(decryptedFileName);
      FileInputStream inputStream = new FileInputStream(document);
      FileOutputStream outputStream = new FileOutputStream(String.valueOf(decryptedFilePath));
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
      int bytesRead = 0;
      byte[] bytes = new byte[8];
      while ((bytesRead = cipherInputStream.read(bytes)) != -1){
         outputStream.write(bytes, 0, bytesRead);
      }
      outputStream.flush();
      outputStream.close();
      cipherInputStream.close();
      return String.valueOf(decryptedFilePath);
   }
   public SecretKey createSessionKey() throws NoSuchAlgorithmException{
      KeyGenerator generator = KeyGenerator.getInstance("AES");
      generator.init(128);
      SecretKey secretKey = generator.generateKey();
      return secretKey;
   }
   public byte[] encryptSessionKey(File openKeyFile, SecretKey secretKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      SshPublicKey sshPublicKey = SshKeyUtils.getPublicKey(openKeyFile);
      PublicKey publicKey = sshPublicKey.getJCEPublicKey();
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.PUBLIC_KEY, publicKey);
      byte[] encryptedKey = cipher.doFinal(secretKey.getEncoded());
      return encryptedKey;
   }
   public SecretKey decryptSessionKey(File privatekeyFile, byte[] encryptedKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      PrivateKey privateKey = null;
      try {
         privateKey = SshKeyUtils.getPrivateKey(privatekeyFile,"").getPrivateKey().getJCEPrivateKey();
      } catch (InvalidPassphraseException | IOException e) {
         throw new RuntimeException(e);
      }
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.PRIVATE_KEY, privateKey);
      byte[] decryptedKey = cipher.doFinal(encryptedKey);
      SecretKey originalKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length,"AES");
      return originalKey;
   }
}