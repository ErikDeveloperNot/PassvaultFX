package com.passvault.ui.fx.view;

import com.passvault.ui.fx.utils.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class GetNewKeyLayoutController {

	@FXML
	PasswordField passwordField;
	@FXML
	PasswordField passwordField2;
	@FXML
	Button enterButton;
	@FXML
	Button cancelButton;
	
	private String password;
	private String password2;
	private Stage dialogStage;
	
	
	public GetNewKeyLayoutController() {
		
	}
	
	@FXML
	private void initialize() {
		cancelButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> cancelPressed()));
		enterButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> enterPressed()));
		passwordField.setOnKeyPressed(Utils.getEnterKeyHandler(() -> enterPressed()));
		passwordField2.setOnKeyPressed(Utils.getEnterKeyHandler(() -> enterPressed()));
	}
	
	@FXML
	private void enterPressed() {
		password = passwordField.getText().trim();
		password2 = passwordField2.getText().trim();
		
		if (password == null || password.equals("") || password2 == null || password2.equals("")) {
			Utils.showAlert(AlertType.ERROR, dialogStage, "Encryption Key Error",
            		"Encryption Key Can't be Blank", "You must enter a value for both encrypt key fields.");
			return;
		}
		
		if (!password.equals(password2)) {
			Utils.showAlert(AlertType.ERROR, dialogStage, "Encryption Key Error",
            		"Passwords Don't Match", "Passwords must match.");
			return;
		}
		
		dialogStage.close();
	}
	
	@FXML
	private void cancelPressed() {
		password = null;
		dialogStage.close();
	}
	
	
	public String getPassword() {
		return password;
	}
	
	
	public void setStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
}
