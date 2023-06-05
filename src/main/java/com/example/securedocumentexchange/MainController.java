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
import java.security.GeneralSecurityException;
import java.util.concurrent.Exchanger;

public class MainController {
    Window currentWindow;
    private Server server;
    private Client client;
    private SecurityHandler securityHandler = new SecurityHandler();
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
    private TextField pathToFileServer;
    @FXML
    private TextField pathToFileClient;
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
    private Button chooseFileToSendServerBtn;
    @FXML
    private Button chooseFileToSendClientBtn;
    @FXML
    void connectToReceiver(ActionEvent event) throws IOException {
        clientView.setDisable(false);
        client = new Client(receiverAddress.getText(),Integer.parseInt(receiverPort.getText()), pathToPubKey.getText(), pathToPrivateKey.getText());
        client.start();
        choosePubKeyBtn.setDisable(true);
        choosePrivateKeyBtn.setDisable(true);
    }
    @FXML
    void startServer(ActionEvent event) throws IOException {
        serverView.setDisable(false);
        server = new Server(Integer.parseInt(serverPort.getText()), pathToPubKey.getText(), pathToPrivateKey.getText());
        server.start();
        choosePubKeyBtn.setDisable(true);
        choosePrivateKeyBtn.setDisable(true);
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
            case "chooseFileToSendServerBtn" -> pathToFileServer.setText(file.getAbsolutePath());
            case "chooseFileToSendClientBtn" -> pathToFileClient.setText(file.getAbsolutePath());
        }
    }
    @FXML
    void sendFile(ActionEvent event) throws GeneralSecurityException, IOException {
        Button btn = (Button) event.getSource();
        switch (btn.getId()){
            case "clientSend" -> {
                String pthToEncFile = client.getSecurityHandler().encryptDocument(new File(pathToFileClient.getText()), client.getTmpDir(), new File(String.valueOf(client.getTmpDir())+"\\server_id_rsa.pub"));

            }
            case "serverSend" -> {
            }
        }
    }
}