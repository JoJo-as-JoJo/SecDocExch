package com.example.securedocumentexchange;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Exchanger;

public class Server extends Thread {
    private boolean doStop = false;
    private Integer port;
    public Path tmpDir;
    private Path pubKeyPath;
    private Path privateKeyPath;
    private SecurityHandler securityHandler;
    private SocketHandler socketHandler;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream out;
    public synchronized void doStop(){
        this.doStop = true;
    }
    private synchronized boolean keepRunning(){
        return this.doStop = false;
    }
    public Server(Integer port, String initOpenKeyPath, String initPrivateKeyPath) throws IOException {
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
    }
    public synchronized Path getTmpDir(){
        return tmpDir;
    }
    public synchronized DataInputStream getInput(){
        return input;
    }
    public synchronized SocketHandler getSocketHandler(){
        return socketHandler;
    }
    public synchronized SecurityHandler getSecurityHandler(){
        return securityHandler;
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
            socketHandler.receiveFile(String.valueOf(tmpDir),input);
            socketHandler.sendFile(String.valueOf(pubKeyPath), "serverPubKey.pub", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        while (!keepRunning()){

        }
    }
}
//String.valueOf(tmpDir)+"\\client_id_rsa.pub"