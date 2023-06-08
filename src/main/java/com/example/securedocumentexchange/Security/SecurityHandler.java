package com.example.securedocumentexchange.Security;

import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPrivateKey;
import com.sshtools.common.ssh.components.SshPublicKey;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

public class SecurityHandler {
   public Path getPath(String initialFilePath, Path destDirPath){
      return Path.of(String.valueOf(destDirPath), String.valueOf(Path.of(initialFilePath).getFileName()));
   }
   public String encryptDocument(File document, Path pthToDir, SecretKey secretKey) throws IOException, GeneralSecurityException{
      String encryptedFileName = document.getName()+".sde";
      String encryptedFilePath = String.valueOf(pthToDir)+File.separator+encryptedFileName;
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
   public String decryptDocument(File document, SecretKey secretKey, String saveDirPath) throws IOException, GeneralSecurityException{
      String decryptedFileName = document.getName().replace(".sde","");
      Path decryptedFilePath = Path.of(saveDirPath, decryptedFileName);
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
      document.delete();
      return String.valueOf(decryptedFilePath);
   }
   public String encryptMessage(String message, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] cipherText = cipher.doFinal(message.getBytes());
      return Base64.getEncoder().encodeToString(cipherText);
   }
   public String decryptMessage(String message, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(message));
      return new String(plainText);
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
   public SecretKey decryptSessionKey(File privateKeyFile, byte[] encryptedKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      PrivateKey privateKey = null;
      try {
         privateKey = SshKeyUtils.getPrivateKey(privateKeyFile,"").getPrivateKey().getJCEPrivateKey();
      } catch (InvalidPassphraseException | IOException e) {
         throw new RuntimeException(e);
      }
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.PRIVATE_KEY, privateKey);
      byte[] decryptedKey = cipher.doFinal(encryptedKey);
      SecretKey originalKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length,"AES");
      return originalKey;
   }
   public boolean validatePubKey(File pubKeyFile) throws IOException {
      SshPublicKey sshPublicKey = SshKeyUtils.getPublicKey(pubKeyFile);
      return true;
   }
   public boolean validatePrivateKey(File privateKeyFile) throws IOException, InvalidPassphraseException {
      SshPrivateKey sshPrivateKey = SshKeyUtils.getPrivateKey(privateKeyFile, "").getPrivateKey();
      return true;
   }
}