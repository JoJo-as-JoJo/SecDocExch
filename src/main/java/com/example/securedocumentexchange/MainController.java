package com.example.securedocumentexchange;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.security.NoSuchAlgorithmException;
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
    void connectToServer(ActionEvent event) throws IOException {
        clientView.setDisable(false);
        client = new Client(receiverAddress.getText(),Integer.parseInt(receiverPort.getText()), pathToPubKey.getText(), pathToPrivateKey.getText());
        client.start();
        choosePubKeyBtn.setDisable(true);
        choosePrivateKeyBtn.setDisable(true);
        clientUp.setText("Остановить соединение");
        clientUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                client.doStop();
            }
        });
    }
    @FXML
    void startServer(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        serverView.setDisable(false);
        server = new Server(Integer.parseInt(serverPort.getText()), pathToPubKey.getText(), pathToPrivateKey.getText());
        server.start();
        choosePubKeyBtn.setDisable(true);
        choosePrivateKeyBtn.setDisable(true);
        serverUp.setText("Остановить сервер");
        serverUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                server.doStop();
            }
        });
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
    void sendFile(ActionEvent event) throws Exception {
        Button btn = (Button) event.getSource();
        switch (btn.getId()){
            case "clientSend" -> {
                String pthToEncFile = client.getSecurityHandler().encryptDocument(new File(pathToFileClient.getText()), client.getTmpDir(), client.getSecretKey());
                client.getSocketHandler().sendFile(pthToEncFile, new File(pthToEncFile).getName(), client.getOut());
            }
            case "serverSend" -> {
                String pthToEncFile = server.getSecurityHandler().encryptDocument(new File(pathToFileServer.getText()), server.getTmpDir(), server.getSecretKey());
                server.getSocketHandler().sendFile(pthToEncFile, new File(pthToEncFile).getName(), server.getOut());
            }
        }
    }
}