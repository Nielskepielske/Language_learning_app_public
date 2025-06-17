package com.final_app.tools;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class AudioPlayer {
    private static MediaPlayer mediaPlayer;

    public static void playAudio(String filePath){
        Media sound = new Media(new File(filePath).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setOnEndOfMedia(() -> System.out.println("✅ Audio is volledig afgespeeld"));
        mediaPlayer.play();
    }
}
