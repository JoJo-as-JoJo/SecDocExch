package com.example.securedocumentexchange;

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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server extends Thread {
    private boolean doStop = false;
    private Path tmpDir;
    private String saveDir;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream out;
    private SecretKey secretKey;
    public ObservableList<String> messages = FXCollections.observableArrayList("Сообщения");
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return this.doStop;
    }
    public Server(Integer port, String saveDir) throws IOException, NoSuchAlgorithmException {
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        this.serverSocket = new ServerSocket(port);
        this.secretKey = securityHandler.createSessionKey();
        this.saveDir = saveDir;
    }
    @Override
    public void run() {
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            socketHandler.receiveFlag(String.valueOf(tmpDir), input);
            socketHandler.sendKey(securityHandler.encryptSessionKey(new File(String.valueOf(tmpDir)+File.separator+"clientPubKey.pub"), secretKey), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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