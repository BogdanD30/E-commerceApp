package com.ecomm;

import com.ecomm.util.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(440);
        stage.setMinHeight(520);
        Navigator.show(stage, Navigator.LOGIN, "Login - E-Commerce Auto", 460, 560);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
