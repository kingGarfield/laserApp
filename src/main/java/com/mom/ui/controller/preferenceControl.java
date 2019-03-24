package com.mom.ui.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.mom.imgprocess.Target;
import com.mom.persistence.GsonPersistence;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
    TextField gunNumberTextField,targetNumberTextField;

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SerialPort.getCommPorts().length != 0){
            List<SerialPort> commPorts = Arrays.asList(SerialPort.getCommPorts());
            gunPortComboBox.setItems(FXCollections.observableList(commPorts));
        }
        targetNumberTextField.setText(Integer.toString(Target.TARGET_NUMBER));
        gunNumberTextField.setText(Integer.toString(Target.GUN_NUMBER));
        if (!SerialPort.getCommPort(Target.camDescriptor).getPortDescription().equals("Bad Port"))
            gunPortComboBox.getSelectionModel().select(SerialPort.getCommPort(Target.camDescriptor));
        else {
            //display error message
        }
        saveButton.setOnMouseClicked(mouseEvent -> {
            String s = targetNumberTextField.getText();
            if (StringUtils.isNumeric(s))
                Target.TARGET_NUMBER = Integer.parseInt(s);
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
            s = gunNumberTextField.getText();
            if (StringUtils.isNumeric(s))
                Target.GUN_NUMBER = Integer.parseInt(s);
            s = (String) gunPortComboBox.getSelectionModel().getSelectedItem();
            Target.camDescriptor = s;
            GsonPersistence.persist2(targets);
        });
    }
}

