package com.example.securedocumentexchange;

import java.io.*;
import java.nio.ByteBuffer;

public class SocketHandler {
    public static void sendFile(String path, String fileNameForReceiver, DataOutputStream dataOutputStream) throws Exception {
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
    public static void receiveFile(String path,DataInputStream dataInputStream) throws Exception {
        String fileName = dataInputStream.readUTF();
        int bytes = 0;
        System.out.println(path+"\\"+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(path+"\\"+fileName);
        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        System.out.println("File is Received");
        fileOutputStream.close();
    }
}
