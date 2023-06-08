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
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client instance
 */
public class Client<T> extends Thread{
    private boolean doStop = false;
    private boolean updateWaiting = false;
    private final Path tmpDir;
    private Path pubKeyPath;
    private Path privateKeyPath;
    private final String saveDir;
    private final SecurityHandler securityHandler;
    private final SocketHandler socketHandler;
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;
    private SecretKey secretKey;
    private final ObservableList<String> messages;
    private final Object lock = new Object();

    /**
     * Stop thread
     */
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return doStop;
    }
    /**
     * Class constructor
     */
    public Client(String address, Integer port, String initOpenKeyPath, String initPrivateKeyPath, String saveDir, ObservableList<String> messages) throws IOException{
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        this.pubKeyPath = securityHandler.getPath(initOpenKeyPath, tmpDir);
        this.privateKeyPath = securityHandler.getPath(initPrivateKeyPath, tmpDir);
        Files.copy(Paths.get(initOpenKeyPath), pubKeyPath);
        Files.copy(Paths.get(initPrivateKeyPath), privateKeyPath);
        this.socket = new Socket(address, port);
        this.input = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.saveDir = saveDir;
        this.messages = messages;
    }
    /**
     * Working loop
     */
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
                String encMsg = socketHandler.receiveData(String.valueOf(tmpDir), input).toString();
                File receivedFile = new File(encMsg);
                if (receivedFile.isFile()) {
                    securityHandler.decryptDocument(receivedFile, secretKey, saveDir);
                }
                else {
                    String message = "Вам: " + securityHandler.decryptMessage(encMsg, secretKey);
                    publish((T) message);
                }
            }
            catch (Exception e) {
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
    /**
     * Encrypt and send message via socket
     */
    public void sendMessage(String message) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        socketHandler.sendFlag('M', out);
        socketHandler.sendMessage(securityHandler.encryptMessage(message, secretKey), out);
        String msg = "Вы: " + message;
        publish((T) msg);
    }
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