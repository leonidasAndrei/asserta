package com.leonidasAndrei.asserta.utils;

import com.leonidasAndrei.asserta.model.Card;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class AssetLoader {
    public static ImageView loadCupImage(int index, double w, double h) {
        return loadImage("/com/leonidasAndrei/asserta/images/bottles/" + index + ".png", w, h);
    }

    public static ImageView loadCardBack(double w, double h) {
        return loadImage("/com/leonidasAndrei/asserta/images/cards/back/0.png", w, h);
    }

    public static ImageView loadCardFront(Card card, double w, double h) {
        String path = "/com/leonidasAndrei/asserta/images/cards/front/"
                + card.getSuit().name().toLowerCase() + "/" + card.getRank() + ".png";
        return loadImage(path, w, h);
    }

    private static ImageView loadImage(String path, double w, double h) {
        InputStream is = AssetLoader.class.getResourceAsStream(path);
        ImageView iv = new ImageView();
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        if (is != null) {
            iv.setImage(new Image(is, w, h, false, false));
            iv.setSmooth(true);
        } else {
            System.out.println("❌ MISSING IMAGE PATH: " + path);
        }
        return iv;
    }
}
