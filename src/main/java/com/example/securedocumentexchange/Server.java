package com.example.securedocumentexchange;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.*;
import java.io.*;
import java.util.List;

public class Server {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    @FXML
    private ListView<String> chatPane;
    public Server(Integer port)
    {
        try{
            InitServerView();
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            socket = server.accept();
            System.out.println("Client accepted");
            in = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            String line = "";
            while (!line.equals("End"))
            {
                try
                {
                    line = in.readUTF();
                    System.out.println(line);
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
            socket.close();
            in.close();
        }
        catch(IOException i){
            System.out.println(i);
        }
    }
    public void InitServerView(){
        try {
            new ServerView();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}