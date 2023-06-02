package com.example.securedocumentexchange;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
public class MainController {

    @FXML
    private TextField receiverAddress;

    @FXML
    private TextField receiverPort;

    @FXML
    protected void connectToReceiver(ActionEvent event) {
       new ClientFactory(receiverAddress.getText(),Integer.parseInt(receiverPort.getText())).start();
    }
    @FXML
    protected void startServer(ActionEvent event) {
        new ServerFactory(5000).start();
    }
}