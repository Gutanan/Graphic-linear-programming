module com.example.bp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bp to javafx.fxml;
    exports com.example.bp;
}