module com.example.securedocumentexchange {
    requires javafx.controls;
    requires javafx.fxml;
    requires maverick.synergy.client;
    requires maverick.base;


    opens com.example.securedocumentexchange to javafx.fxml;
    exports com.example.securedocumentexchange;
    exports com.example.securedocumentexchange.Network;
    opens com.example.securedocumentexchange.Network to javafx.fxml;
    exports com.example.securedocumentexchange.Security;
    opens com.example.securedocumentexchange.Security to javafx.fxml;
}