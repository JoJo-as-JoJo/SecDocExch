package com.example.securedocumentexchange.Network;

import java.io.*;
import java.lang.constant.Constable;
/**
 * Handles data transmission via socket
 */
public class SocketHandler {
    /**
     * Send file via socket
     */
    public void sendFile(String path, String fileNameForReceiver, DataOutputStream dataOutputStream) throws Exception {
        dataOutputStream.writeUTF(fileNameForReceiver);
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        dataOutputStream.writeLong(file.length());
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
    /**
     * Received file handler
     */
    public String receiveFile(String path,DataInputStream dataInputStream) throws Exception {
        String fileName = dataInputStream.readUTF();
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(path+File.separator+fileName);
        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
        return path+File.separator+fileName;
    }
    /**
     * Send message via socket
     */
    public void sendMessage(String message, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(message);
    }
    /**
     * Received message handler
     */
    public String receiveMessage(DataInputStream dataInputStream) throws IOException {
        return dataInputStream.readUTF();
    }
    /**
     * Send key via socket
     */
    public void sendKey(byte[] encryptedSessionKey, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(encryptedSessionKey.length);
        dataOutputStream.write(encryptedSessionKey);
    }
    /**
     * Received key handler
     */
    public byte[] receiveKey(DataInputStream dataInputStream) throws IOException {
        int size = dataInputStream.readInt();
        byte[] encryptedSessionKey = new byte[size];
        dataInputStream.read(encryptedSessionKey, 0, size);
        return encryptedSessionKey;
    }

    /**
     * Send indicator char via socket
     */
    public void sendFlag(char flag, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeChar(flag);
    }

    /**
     * Receive data via socket
     */
    public Constable receiveData(String path, DataInputStream dataInputStream){
        try {
            char flag = dataInputStream.readChar();
            switch (flag){
                case 'F' -> {
                    return receiveFile(path, dataInputStream);
                }
                case 'K' -> {
                    receiveFile(path, dataInputStream);
                }
                case 'M' -> {
                    return receiveMessage(dataInputStream);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
