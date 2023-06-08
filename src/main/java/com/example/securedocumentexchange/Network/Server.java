package com.example.securedocumentexchange.Network;

import com.example.securedocumentexchange.Security.SecurityHandler;
import javafx.application.Platform;
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
import java.util.List;

/**
 * Server instance
 */
public class Server<T> extends Thread{
    private boolean doStop = false;
    private boolean updateWaiting = false;
    private Path tmpDir;
    private String saveDir;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream out;
    private SecretKey secretKey;
    private final ObservableList<String> messages;
    private final Object lock = new Object();

    /**
     * Stop Thread
     */
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return this.doStop;
    }
    /**
     * Class constructor
     */
    public Server(Integer port, String saveDir, ObservableList<String> messages) throws IOException, NoSuchAlgorithmException {
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        this.serverSocket = new ServerSocket(port);
        this.secretKey = securityHandler.createSessionKey();
        this.saveDir = saveDir;
        this.messages = messages;
    }

    /**
     * Working loop
     */
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
            socketHandler.receiveData(String.valueOf(tmpDir), input);
            socketHandler.sendKey(securityHandler.encryptSessionKey(new File(String.valueOf(tmpDir)+File.separator+"clientPubKey.pub"), secretKey), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        while (!keepRunning()){
            try {
                String encMsg = socketHandler.receiveData(String.valueOf(tmpDir), input).toString();
                File receivedFile = new File(encMsg);
                if (receivedFile.isFile()) {
                    securityHandler.decryptDocument(receivedFile, secretKey, saveDir);
                }
                else {
                    String message = "Вам: " + securityHandler.decryptMessage(encMsg, secretKey);
                    publish((T) message);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * Encrypt and send file via socket
     */
    public void sendFile(String pathToFile) throws Exception {
        socketHandler.sendFlag('F', out);
        String pthToEncFile = securityHandler.encryptDocument(new File(pathToFile), tmpDir, secretKey);
        socketHandler.sendFile(pthToEncFile, new File(pthToEncFile).getName(), out);
    }
    public void sendMessage(String message) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socketHandler.sendFlag('M', out);
        socketHandler.sendMessage(securityHandler.encryptMessage(message, secretKey), out);
        String msg = "Вы: " + message;
        publish((T) msg);
    }
    /**
     * Encrypt and send message via socket
     */
    private void publish(T... values){
        synchronized (lock){
            for (T v : values){
                messages.add((String) v);
            }
            if (!updateWaiting) {
                updateWaiting = true;
                Platform.runLater(this::update);
            }
        }
    }
    private void update(){
        List<String> chunks;
        synchronized (lock){
            chunks = new ArrayList<>(messages);
            messages.clear();
            updateWaiting = false;
        }
        process(chunks);
    }
    private void process(List<String> chunks){
        messages.addAll(chunks);
    }
}