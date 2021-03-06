package com.mom.ui.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.mom.imgprocess.Target;
import com.mom.persistence.GsonPersistence;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Modality;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class preferenceControl implements Initializable,ControllerInterface {

    public void setTargets(List<Target> targets) {
        this.targets = targets;
        gunPortComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object port) {
                if (port != null)
                    Target.camDescriptor = ((SerialPort)(port)).getDescriptivePortName();
            }
        });
    }

    List<Target> targets;

    @FXML
    Button saveButton;

    @FXML
    ComboBox gunPortComboBox;

    @FXML
    TextField gunNumberTextField,targetNumberTextField,ipcameraTextField;

    @FXML
    ListView ipcameraListView;

    @Override
    public void shutdown() {

    }

    List<SerialPort> commPorts;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SerialPort.getCommPorts().length != 0){
            commPorts = Arrays.asList(SerialPort.getCommPorts());
            gunPortComboBox.setItems(FXCollections.observableList(commPorts));
        }
        targetNumberTextField.setText(Integer.toString(Target.TARGET_NUMBER));
        gunNumberTextField.setText(Integer.toString(Target.GUN_NUMBER));
        for (int i = 0; i < SerialPort.getCommPorts().length; i++) {
            if (Target.camDescriptor.equals(commPorts.get(i).getPortDescription())){
                gunPortComboBox.getSelectionModel().select(commPorts.get(i));
            }
        }
        if (Target.camDescriptor.isEmpty() || Target.camDescriptor.isBlank()) {
            gunPortComboBox.getSelectionModel().selectFirst();
        }
        saveButton.setOnMouseClicked(mouseEvent -> {
            String s = targetNumberTextField.getText();
            if (StringUtils.isNumeric(s)) {
                if (Integer.parseInt(s) < 1 || Integer.parseInt(s) > 20) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("توجه");
                    alert.setHeaderText("");
                    alert.setContentText("تعداد سیبل بین ۱ تا ۲۰ باید باشد.");
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.showAndWait();
                    return;
                }
            }
            else return;
            //
            String s0 = gunNumberTextField.getText();
            if (StringUtils.isNumeric(s0)) {
                if (Integer.parseInt(s0) < 1 || Integer.parseInt(s0) > 8) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("توجه");
                    alert.setHeaderText("");
                    alert.setContentText("تعداد سلاح بین ۱ تا ۸ باید باشد.");
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.showAndWait();
                    return;
                }
            }
            else return;
            if (gunPortComboBox.getSelectionModel().isEmpty()){
                //display error message
            }
            else{
                String s2 = ((SerialPort) gunPortComboBox.getSelectionModel().getSelectedItem()).getPortDescription();
                Target.camDescriptor = s2;
            }
            Target.TARGET_NUMBER = Integer.parseInt(s);
            Target.GUN_NUMBER = Integer.parseInt(s0);
            int targetSize = targets.size();
            if (targetSize > Target.TARGET_NUMBER){
                for (int i = 0; i < targetSize - Target.TARGET_NUMBER; i++) {
                    targets.remove(targets.size() - 1);
                }
            }
            if (targetSize < Target.TARGET_NUMBER){
                for (int i = 0; i < Target.TARGET_NUMBER - targetSize; i++) {
                    Target target = new Target();
                    target.name = Integer.toString(targets.size());
                    targets.add(target);
                }
            }
            GsonPersistence.persist(targets);
        });
    }
}

