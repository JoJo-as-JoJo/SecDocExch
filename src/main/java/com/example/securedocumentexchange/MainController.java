package com.example.securedocumentexchange;

import com.example.securedocumentexchange.Network.Client;
import com.example.securedocumentexchange.Network.Server;
import com.example.securedocumentexchange.Security.SecurityHandler;
import com.sshtools.common.publickey.InvalidPassphraseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
/**
 * Controller class
 */
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
    private TextField pathToSaveDir;
    @FXML
    private TextField clientMessage;
    @FXML
    private TextField serverMessage;
    @FXML
    private ListView clientMessages;
    @FXML
    private ListView serverMessages;
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
    private ObservableList<String> clientMSG = FXCollections.observableArrayList();
    private ObservableList<String> serverMSG = FXCollections.observableArrayList();
    /**
     * Try to create connection with server
     */
    @FXML
    void connectToServer(ActionEvent event) throws IOException, InvalidPassphraseException {
        if (securityHandler.validatePubKey(new File(pathToPubKey.getText())) && securityHandler.validatePrivateKey(new File(pathToPrivateKey.getText()))) {
            client = new Client(receiverAddress.getText(), Integer.parseInt(receiverPort.getText()), pathToPubKey.getText(), pathToPrivateKey.getText(), pathToSaveDir.getText(), clientMSG);
            client.setDaemon(true);
            client.start();
            clientMessages.setItems(clientMSG);
            clientView.setDisable(false);
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
    }

    /**
     * Try to create a server
     */
    @FXML
    void startServer(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        server = new Server(Integer.parseInt(serverPort.getText()), pathToSaveDir.getText(), serverMSG);
        server.setDaemon(true);
        server.start();
        serverMessages.setItems(serverMSG);
        serverView.setDisable(false);
        serverUp.setText("Остановить сервер");
        serverUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                server.doStop();
            }
        });
    }

    /**
     * Choose a file
     */
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
    /**
     * Choose a directory
     */
    @FXML
    void chooseDirectory(ActionEvent event){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(currentWindow);
        if (file==null){
            return;
        }
        pathToSaveDir.setText(file.getAbsolutePath());
        serverUp.setDisable(false);
    }

    /**
     * Send file via established connection
     */
    @FXML
    void sendFile(ActionEvent event) throws Exception {
        Button btn = (Button) event.getSource();
        switch (btn.getId()){
            case "clientSendFile" -> client.sendFile(pathToFileClient.getText());
            case "serverSendFile" -> server.sendFile(pathToFileServer.getText());
        }
    }
    /**
     * Send message via established connection
     */
    @FXML
    void sendMessage(ActionEvent event) throws Exception{
        Button btn = (Button) event.getSource();
        switch (btn.getId()){
            case "clientSendMessage" -> client.sendMessage(clientMessage.getText());
            case "serverSendMessage" -> server.sendMessage(serverMessage.getText());
        }
    }
}