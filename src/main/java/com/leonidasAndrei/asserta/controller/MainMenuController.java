package com.leonidasAndrei.asserta.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.io.IOException;

public class MainMenuController {

    @FXML
    public void onPlayClicked() throws IOException {
        System.out.println("Play");
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
