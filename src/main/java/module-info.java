module com.example.securedocumentexchange {
    requires javafx.controls;
    requires javafx.fxml;
    requires maverick.synergy.client;
    requires maverick.base;


    opens com.example.securedocumentexchange to javafx.fxml;
    exports com.example.securedocumentexchange;
}