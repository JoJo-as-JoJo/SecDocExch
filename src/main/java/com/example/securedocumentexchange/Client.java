package com.example.securedocumentexchange;
import com.sshtools.common.publickey.InvalidPassphraseException;

import javax.crypto.SecretKey;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class Client extends Thread{
    private boolean doStop = false;
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
    public Client(String address, Integer port, String initOpenKeyPath, String initPrivateKeyPath) throws IOException, InvalidPassphraseException {
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
    }
    @Override
    public void run() {
        try {
            socketHandler.sendFile(String.valueOf(pubKeyPath), "clientPubKey.pub", out);
            byte[] encryptedSessionKey = socketHandler.receiveKey(input);
            secretKey = securityHandler.decryptSessionKey(new File(String.valueOf(privateKeyPath)), encryptedSessionKey);
        } catch (Exception e) {
            doStop = true;
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