
package com.mom.ui.controller;

import com.mom.BoardConnection.Arduino;
import com.mom.cam.CameraControl;
import com.mom.imgprocess.DetectRedDot;
import com.mom.imgprocess.Target;
import com.mom.persistence.GsonPersistence;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable, ControllerInterface {

    @FXML
    HBox activeTargetHbox;

    @FXML
    private Button shooting0Button;

    @FXML
    private MenuItem targetMenuItem, preferencesMenuItem, gunMenuItem,aboutUsMenuItem;

    private FXMLLoader fxmlLoader;

    private List<Stage> stages;

    private List<CheckBox> boxes;

    public static List<Target> targets;

    public Arduino arduino;

    public CameraControl cameraControl;

    public void setActiveTargetHbox(boolean reset) {
        if (reset){
            for (int i = 0; i < boxes.size(); i++) {
                activeTargetHbox.getChildren().clear();
            }
            boxes.clear();
        }
        targets = GsonPersistence.load();
        for (int i = 0; i < Target.GUN_NUMBER; i++) {
            CheckBox checkBox = new CheckBox(Integer.toString(i));
            checkBox.setStyle("-fx-padding: 8 8 8 8");
            boxes.add(checkBox);
            activeTargetHbox.getChildren().add(checkBox);
        }
    }

    public void afterShow(){
        cameraControl = cameraControl.getInstance();
        targets = GsonPersistence.load();
        arduino = Arduino.getInstance();
        stages = new ArrayList<>();
        boxes = new ArrayList<>();
        setActiveTargetHbox(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        aboutUsMenuItem.setOnAction(actionEvent -> {
            Pair<Stage, ControllerInterface> pair = loadLayoutController("about.fxml");
            pair.getKey().initModality(Modality.APPLICATION_MODAL);
            pair.getKey().show();
        });
//font 20
        targetMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            private SettingTargetController controller;

            @Override
            public void handle(ActionEvent actionEvent) {
                closeTargetWindows();
                Pair<Stage, ControllerInterface> pair = loadLayoutController("settingTarget.fxml");
                controller = ((SettingTargetController) pair.getValue());
                controller.setDetectRedDot(new DetectRedDot());
                Stage s = pair.getKey();
                s.initModality(Modality.APPLICATION_MODAL);
                s.show();
                controller.afterShow();
            }
        });

        preferencesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            preferenceControl controller;
            Stage stage;

            @Override
            public void handle(ActionEvent actionEvent) {
                closeTargetWindows();
                Pair<Stage, ControllerInterface> pair = loadLayoutController("preference.fxml");
                controller = ((preferenceControl) pair.getValue());
                stage = pair.getKey();
                stages.add(stage);
                stage.setOnCloseRequest(windowEvent -> setActiveTargetHbox(true));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
                controller.setTargets(targets);
            }
        });

        shooting0Button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            TargetController controller;

            @Override
            public void handle(MouseEvent mouseEvent) {
                closeTargetWindows();
                initializeDetection();
                Stage stage;
                for (int i = 0; i < Target.TARGET_NUMBER; i++) {
                    if (!targets.get(i).active)
                        continue;
                    if (!(targets.get(i).valid)) {
                        System.out.println("target " + targets.get(i).getName() + " is invalid");
                        continue;
                    }
                    if (cameraControl.getCamera(targets.get(i).webCamName) == null) {
                        System.out.println("target " + targets.get(i).getName() + " camera is not connected.");
                        continue;
                    }

                    Pair<Stage, ControllerInterface> pair = loadLayoutController("target.fxml");
                    controller = ((TargetController) pair.getValue());
                    stage = pair.getKey();
                    DetectRedDot detectRedDot = new DetectRedDot();
                    detectRedDot.setIndex(i);
                    controller.setDetectRedDot(detectRedDot);
                    stages.add(stage);
                    stage.show();
                    arduino.setActive(true);
                }
            }
        });

        gunMenuItem.setOnAction(new EventHandler<>() {
            private SettingGunController controller;
            Stage stage;

            @Override
            public void handle(ActionEvent actionEvent) {
                closeTargetWindows();
                Pair<Stage, ControllerInterface> pair = loadLayoutController("settingGun.fxml");
                controller = ((SettingGunController) pair.getValue());
                stage = pair.getKey();
                stage.initModality(Modality.APPLICATION_MODAL);
                pair.getKey().show();
                controller.afterShow();
            }
        });
    }


    private void closeTargetWindows() {
        for (Stage stage :
                stages) {
            stage.close();
        }
        stages.clear();
    }

    private void initializeDetection() {
        targets = GsonPersistence.load();
        for (int i = 0; i < targets.size(); i++) {
            targets.get(i).active = false;
        }
        for (int i = 0; i < boxes.size(); i++) {
            if (boxes.get(i).isSelected()) {
                for (int j = 0; j < targets.size(); j++) {
                    if (targets.get(j).gunId == i)
                        targets.get(j).active = true;
                }
            }
        }
        GsonPersistence.persist(targets);
    }

    private Pair<Stage, ControllerInterface> loadLayoutController(String resourceName) {
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/" + resourceName));
        AnchorPane pane = null;
        try {
            pane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ControllerInterface controller = fxmlLoader.getController();
        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.getIcons().add(new Image(getClass().getResource("/img/program icon.png").toExternalForm()));
        stage.setOnCloseRequest(windowEvent -> controller.shutdown());
        stage.setScene(scene);
        return new Pair<>(stage, controller);
    }

    public void shutdown() {
        cameraControl.stopCameras();
    }
}
