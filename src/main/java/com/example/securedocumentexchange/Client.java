package com.example.securedocumentexchange;
import javax.crypto.SecretKey;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Exchanger;

public class Client extends Thread{
    private boolean doStop = false;
    private String address;
    private Integer port;
    private Path tmpDir;
    private Path pubKeyPath;
    private Path privateKeyPath;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream out;
    private SecretKey secretKey;

    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return doStop;
    }
    public Client(String address, Integer port, String initOpenKeyPath, String initPrivateKeyPath) throws IOException {
        this.address = address;
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
        this.socket = new Socket(address, port);
        this.input = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
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
            socketHandler.sendFile(String.valueOf(pubKeyPath), "clientPubKey.pub", out);
            byte[] encryptedSessionKey = socketHandler.receiveKey(input);
            secretKey = securityHandler.decryptSessionKey(new File(String.valueOf(privateKeyPath)), encryptedSessionKey);
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