package com.example.securedocumentexchange;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SecureDocExch extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SecureDocExch.class.getResource("SecureDocumentExchange.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SecureDocumentExchange");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}