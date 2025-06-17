package com.final_app.views.components;

import com.final_app.events.PromptEvent;
import com.final_app.globals.GlobalVariables;
import com.final_app.tools.SVGUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class PromptBarView extends HBox {
    @FXML private TextField promptField;
    @FXML private VBox btnRecord;
    @FXML private ImageView btnIcon;

    @FXML private VBox btnSend;
    @FXML private ImageView btnSendIcon;



    public boolean recording = false;
    public boolean sendBtnClicked = false;

    private Consumer<Void> onButtonClick;

    private BooleanProperty isEnabled = new SimpleBooleanProperty(true);



    public PromptBarView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/final_app/views/components/PromptBar.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            this.setMaxWidth(Double.MAX_VALUE);
            btnIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/mic_off_light.svg", 20, 20));


            btnRecord.setOnMouseClicked(e -> {
                recordingAction();
            });

            isEnabled.addListener((obs, oldVal, newVal) -> {
               if(newVal){
                   promptField.setDisable(false);
                   btnSend.setDisable(false);
                   btnRecord.setDisable(false);
               }else{
                   promptField.setDisable(true);
                   btnSend.setDisable(true);
                   btnRecord.setDisable(true);
                   promptField.setText("");
                   sendBtnClicked = false;
               }
            });
            btnSendIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/send_dark.svg", 20, 20));
            btnSend.setOnMouseClicked(e -> {
                if(!sendBtnClicked){
                    sendBtnClicked = true;

                    btnSendIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/sending_dark.svg", 20, 20));
                    this.fireEvent(new PromptEvent(promptField.getText()));
                    promptField.setText("");
                    Thread t = new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            btnSendIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/send_dark.svg", 20, 20));
                            sendBtnClicked = false;
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    t.start();

                }else{
                    btnSendIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/send_dark.svg", 20, 20));
                }

            });

            //btnRecord.prefWidthProperty().bind(btnRecord.heightProperty());

            promptField.setOnKeyReleased(
                    keyEvent -> {
                        if(keyEvent.getCode() == KeyCode.ENTER){
                            if(promptField.getText() != "") this.fireEvent(new PromptEvent(promptField.getText()));
                            promptField.setText("");
                        }
                    }
            );
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public void recordingAction(){
        recording = !recording;

        System.out.println("Button clicked!!!");

        if(recording){
            btnRecord.setStyle("-fx-background-color: white");
            btnIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/microphone_dark.svg", 20, 20));
        }else{
            btnRecord.setStyle("-fx-background-color: transparent");
            btnIcon.setImage(SVGUtil.loadSVG(GlobalVariables.BASE_PATH + "icons/mic_off_light.svg", 20, 20));
        }

        Thread t = new Thread(() -> {
            if(onButtonClick != null){
                onButtonClick.accept(null);
            }
        });
        t.start();

        promptField.setText("");
    }
    public void setPromptBarEnabled(boolean enabled){
        isEnabled.set(enabled);
    }
    public boolean isPromptBarEnabled(){
        return isEnabled.get();
    }
    public BooleanProperty promptBarEnabledProperty(){
        return isEnabled;
    }

    public boolean isPromptBarFocused(){
        return promptField.isFocused();
    }

    public void setOnButtonClick(Consumer<Void> listener){
        this.onButtonClick = listener;
    }
}
