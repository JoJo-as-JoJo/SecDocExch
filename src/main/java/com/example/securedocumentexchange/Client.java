package com.example.securedocumentexchange;
import com.sshtools.common.publickey.InvalidPassphraseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Client extends Thread{
    private boolean doStop = false;
    private Path tmpDir;
    private Path pubKeyPath;
    private Path privateKeyPath;
    private String saveDir;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream out;
    private SecretKey secretKey;
    public ObservableList<String> messages = FXCollections.observableArrayList("Сообщения");
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return doStop;
    }
    public Client(String address, Integer port, String initOpenKeyPath, String initPrivateKeyPath, String saveDir) throws IOException, InvalidPassphraseException {
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        if (securityHandler.validatePubKey(new File(initOpenKeyPath))==true && securityHandler.validatePrivateKey(new File(initPrivateKeyPath))==true){
            this.pubKeyPath = securityHandler.getPath(initOpenKeyPath, tmpDir);
            this.privateKeyPath = securityHandler.getPath(initPrivateKeyPath, tmpDir);
            Files.copy(Paths.get(initOpenKeyPath), pubKeyPath);
            Files.copy(Paths.get(initPrivateKeyPath), privateKeyPath);
        }
        this.socket = new Socket(address, port);
        this.input = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.saveDir = saveDir;
    }
    @Override
    public void run() {
        try {
            socketHandler.sendFlag('K', out);
            socketHandler.sendFile(String.valueOf(pubKeyPath), "clientPubKey.pub", out);
            byte[] encryptedSessionKey = socketHandler.receiveKey(input);
            secretKey = securityHandler.decryptSessionKey(new File(String.valueOf(privateKeyPath)), encryptedSessionKey);
        } catch (Exception e) {
            doStop = true;
        }
        while (!keepRunning()){
            try {
                String EncMsg = socketHandler.receiveFlag(String.valueOf(tmpDir), input).toString();
                File receivedFile = new File(EncMsg);
                if (receivedFile.isFile()) {
                    securityHandler.decryptDocument(receivedFile, secretKey, saveDir);
                    messages.add("Вам прислали файл!");
                }
                else {
                    String message = "Вам: " + securityHandler.decryptMessage(EncMsg, secretKey);
                    messages.add(message);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void sendFile(String pathToFile) throws Exception {
        socketHandler.sendFlag('F', out);
        String pthToEncFile = securityHandler.encryptDocument(new File(pathToFile), tmpDir, secretKey);
        socketHandler.sendFile(pthToEncFile, new File(pthToEncFile).getName(), out);
    }
    public void sendMessage(String message) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socketHandler.sendFlag('M', out);
        socketHandler.sendMessage(securityHandler.encryptMessage(message, secretKey), out);
        String msg = "Вы: " + message;
        messages.add(msg);
    }
}