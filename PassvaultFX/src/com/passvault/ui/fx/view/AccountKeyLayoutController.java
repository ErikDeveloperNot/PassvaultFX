package com.passvault.ui.fx.view;

import com.passvault.ui.fx.utils.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class AccountKeyLayoutController {

	@FXML
	PasswordField passwordField;
	@FXML
	Button enterButton;
	@FXML
	Button cancelButton;
	
	private String password;
	private Stage dialogStage;
	
	
	public AccountKeyLayoutController() {
		
	}
	
	@FXML
	private void initialize() {
		cancelButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> cancelPressed()));
		enterButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> enterPressed()));
		passwordField.setOnKeyPressed(Utils.getEnterKeyHandler(() -> enterPressed()));
	}
	
	@FXML
	private void enterPressed() {
		password = passwordField.getText().trim();
		
		if (password != null && !password.equals("")) {
			dialogStage.close();
		} else {
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Encryption Key Error");
            alert.setHeaderText("Encryption Key Can't be Blank");
            alert.setContentText("You must enter a value for the encryption key. This should be the same key " +
            						"that was used to encrypt this password.");
			
            alert.showAndWait();*/
            Utils.showAlert(AlertType.ERROR, dialogStage, "Encryption Key Error",
            		"Encryption Key Can't be Blank", "You must enter a value for the encryption key. This should be the same key " +
            				"that was used to encrypt this password.");
		}
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
