package com.example.securedocumentexchange;

import javax.crypto.SecretKey;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Exchanger;

public class Server extends Thread {
    private boolean doStop = false;
    private Integer port;
    private Path tmpDir;
    private Path pubKeyPath;
    private Path privateKeyPath;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream out;
    private SecretKey secretKey;
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return this.doStop;
    }
    public Server(Integer port) throws IOException, NoSuchAlgorithmException {
        this.port = port;
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        this.serverSocket = new ServerSocket(port);
        this.secretKey = securityHandler.createSessionKey();
    }
    @Override
    public void run() {
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Client connected");
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            socketHandler.receiveFile(String.valueOf(tmpDir), input);
            socketHandler.sendKey(securityHandler.encryptSessionKey(new File(String.valueOf(tmpDir)+File.separator+"clientPubKey.pub"), secretKey), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        while (!keepRunning()){
            try {
                String pathToEncFile = socketHandler.receiveFile(String.valueOf(tmpDir), input);
                securityHandler.decryptDocument(new File(pathToEncFile), secretKey);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void send(String pathToFile) throws Exception {
        String pthToEncFile = securityHandler.encryptDocument(new File(pathToFile), tmpDir, secretKey);
        socketHandler.sendFile(pthToEncFile, new File(pthToEncFile).getName(), out);
    }
}