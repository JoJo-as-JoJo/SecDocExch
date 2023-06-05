package com.example.securedocumentexchange;

import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.components.SshPrivateKey;
import com.sshtools.common.ssh.components.SshPublicKey;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;

public class SecurityHandler {
   public Path getPath(String initialFilePath, Path destDirPath){
      Path path = Path.of(String.valueOf(destDirPath), String.valueOf(Path.of(initialFilePath).getFileName()));
      return path;
   }
   public byte[] encrypt(byte[] message, PublicKey publicKey) throws GeneralSecurityException{
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
      byte[] encryptedBytes = cipher.doFinal(message);
      return encryptedBytes;
   }
   public byte[] decrypt(byte[] message, PrivateKey privateKey) throws GeneralSecurityException{
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      byte[] decryptedBytes = cipher.doFinal(message);
      return decryptedBytes;
   }
   public String encryptDocument(File document, Path pthToDir, File openKeyFile) throws IOException, GeneralSecurityException{
      SshPublicKey sshPublicKey = SshKeyUtils.getPublicKey(openKeyFile);
      PublicKey publicKey = sshPublicKey.getJCEPublicKey();
      byte[] documentBytes = Files.readAllBytes(document.toPath());
      byte[] encryptedBytes = encrypt(documentBytes, publicKey);
      String encryptedFileName = document.getName()+".sde";
      String encryptedFilePath = String.valueOf(pthToDir)+"\\"+encryptedFileName;
      Files.write(Path.of(encryptedFilePath), encryptedBytes, StandardOpenOption.CREATE);
      return encryptedFilePath;
   }
   public String decryptDocument(File document, File privateKeyFile) throws IOException, GeneralSecurityException{
      PrivateKey privateKey = null;
      try {
         privateKey = SshKeyUtils.getPrivateKey(privateKeyFile,"").getPrivateKey().getJCEPrivateKey();
      } catch (InvalidPassphraseException e) {
         throw new RuntimeException(e);
      }
      byte[] encryptedBytes = Files.readAllBytes(document.toPath());
      byte[] decryptedBytes = decrypt(encryptedBytes, privateKey);
      String decryptedFileName = document.getName().replace(".sde","");
      Path decryptedFilePath = document.toPath().resolveSibling(decryptedFileName);
      Files.write(decryptedFilePath, decryptedBytes, StandardOpenOption.CREATE);
      return String.valueOf(decryptedFilePath);
   }
}