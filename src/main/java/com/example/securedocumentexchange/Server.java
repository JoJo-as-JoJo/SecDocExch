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
    public Server(Integer port, String initOpenKeyPath, String initPrivateKeyPath) throws IOException, NoSuchAlgorithmException {
        this.port = port;
        this.tmpDir = Files.createTempDirectory("");
        this.securityHandler = new SecurityHandler();
        this.socketHandler = new SocketHandler();
        this.pubKeyPath = securityHandler.getPath(initOpenKeyPath, tmpDir);
        this.privateKeyPath = securityHandler.getPath(initPrivateKeyPath, tmpDir);
        try {
            Files.copy(Paths.get(initOpenKeyPath), pubKeyPath);
            Files.copy(Paths.get(initPrivateKeyPath), privateKeyPath);
        }
        catch (Exception ec){
            System.out.println(ec);
        }
        this.serverSocket = new ServerSocket(port);
        this.secretKey = securityHandler.createSessionKey();
    }
    public synchronized Path getTmpDir(){
        return tmpDir;
    }
    public synchronized DataInputStream getInput(){
        return input;
    }
    public synchronized DataOutputStream getOut(){
        return out;
    }
    public synchronized SocketHandler getSocketHandler(){
        return socketHandler;
    }
    public synchronized SecurityHandler getSecurityHandler(){
        return securityHandler;
    }
    public synchronized SecretKey getSecretKey(){
        return secretKey;
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
}