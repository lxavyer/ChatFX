module com.leandro.chatjavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.leandro.chatjavafx to javafx.fxml;
    exports com.leandro.chatjavafx;
}