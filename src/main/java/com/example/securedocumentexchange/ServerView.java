package com.example.securedocumentexchange;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerView{
    public void serverView() throws IOException {
        System.out.println("ServerView");
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("Server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }
}
