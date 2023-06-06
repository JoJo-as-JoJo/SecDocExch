package com.example.securedocumentexchange;

import java.io.*;
public class SocketHandler {
    public void sendFile(String path, String fileNameForReceiver, DataOutputStream dataOutputStream) throws Exception {
        System.out.println("trying to send file");
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
        System.out.println("file sent");
    }
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
        System.out.println("File is Received");
        System.out.println("File written in:"+path+File.separator+fileName);
        fileOutputStream.close();
        return new String(path+File.separator+fileName);
    }
    public void sendKey(byte[] encryptedSessionKey, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(encryptedSessionKey.length);
        dataOutputStream.write(encryptedSessionKey);
    }
    public byte[] receiveKey(DataInputStream dataInputStream) throws IOException {
        int size = dataInputStream.readInt();
        byte[] encryptedSessionKey = new byte[size];
        dataInputStream.read(encryptedSessionKey, 0, size);
        return encryptedSessionKey;
    }
}
