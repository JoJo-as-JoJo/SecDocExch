package com.example.securedocumentexchange;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class MainController {
    Window currentWindow;
    @FXML
    private TextField receiverAddress;
    @FXML
    private TextField receiverPort;
    @FXML
    private TextField serverPort;
    @FXML
    private TextField pathToPubKey;
    @FXML
    private TextField pathToPrivateKey;
    @FXML
    private TabPane tabs;
    @FXML
    private Tab settings;
    @FXML
    private Tab serverView;
    @FXML
    private Tab clientView;
    @FXML
    private Button choosePubKeyBtn;
    @FXML
    private Button choosePrivateKeyBtn;
    @FXML
    private Button clientUp;
    @FXML
    private Button serverUp;
    @FXML
    protected void connectToReceiver(ActionEvent event) {
        clientView.setDisable(false);
        new ClientFactory(receiverAddress.getText(),Integer.parseInt(receiverPort.getText())).start();
    }
    @FXML
    protected void startServer(ActionEvent event) throws IOException {
        System.out.println(tabs.getTabs());
        serverView.setDisable(false);
        new ServerFactory(Integer.parseInt(serverPort.getText())).start();
    }
    @FXML
    void chooseFile(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(currentWindow);
        if (file==null){
            return;
        }
        Button btn = (Button) event.getSource();
        switch (btn.getId()){
            case "choosePubKeyBtn" -> pathToPubKey.setText(file.getAbsolutePath());
            case "choosePrivateKeyBtn" -> pathToPrivateKey.setText(file.getAbsolutePath());
        }
    }
}