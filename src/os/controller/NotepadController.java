package os.controller;

/**
 * @package: os.controller
 * @Description: 记事本 FXML 控制类
 * @author: Brandon
 * @date: 2023/1/12 9:37
 **/

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import os.OS;
import os.manager.file.FileOperator;
import os.manager.file.OpenedFile;

import java.net.URL;
import java.util.ResourceBundle;


public class NotepadController implements Initializable {

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private TextArea content;

    private OpenedFile openedFile;

    public NotepadController() {
    }

    public void saveFile() throws Exception {
        FileOperator fileOperator = OS.fileOperator;
        String text = content.getText();
        fileOperator.write(openedFile, text.getBytes(), text.length());
    }

    public void closeFile() throws Exception {
        FileOperator fileOperator = OS.fileOperator;
        fileOperator.close(openedFile);
    }

    public void setOpenedFile(OpenedFile openedFile) {
        this.openedFile = openedFile;
    }

    public void setText(String text) {
        content.setText(text);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

