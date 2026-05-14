package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.io.IOException;

public class MainMenuController {

    @FXML
    public void onPlayClicked() throws IOException {
        App.switchScene("Setup");
    }

    @FXML
    public void onRulesClicked() throws IOException {
        System.out.println("Rules");
    }

    @FXML
    public void onExitClicked() {
        System.exit(0);
    }
}
