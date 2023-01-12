module os.os {
    requires javafx.controls;
    requires javafx.fxml;


    opens os.os to javafx.fxml;
    exports os.os;
}