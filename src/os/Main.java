package os;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import os.controller.MainController;

/**
 * @package: os
 * @Description:
 * @author: Brandon
 * @date: 2023/1/12 9:43
 **/
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/os.fxml"));
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();
        OS os = OS.getInstance();
        os.setMainController(mainController);
        mainController.setOS(os);
        // 设置标题大小等等
        primaryStage.setTitle("Operation System Project");
        primaryStage.setScene(new Scene(root, 1000, 580));
        primaryStage.setResizable(false);
        // 操作系统的关闭事件
        primaryStage.setOnCloseRequest(event -> {
            mainController.closeOS();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
