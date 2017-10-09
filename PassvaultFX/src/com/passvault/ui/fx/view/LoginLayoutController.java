package com.passvault.ui.fx.view;

import com.passvault.ui.fx.utils.EnterPressed;
import com.passvault.ui.fx.utils.Utils;

import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class LoginLayoutController implements EnterPressed {

	Stage dialogStage;
	String password;
	
	@FXML
	PasswordField passwordField;
	@FXML
	Button loginButton;
	@FXML
	Button exitButton;
	
	public LoginLayoutController() {
		
	}
	
	
	@FXML
	private void initialize() {
		loginButton.setOnKeyPressed(Utils.getEnterKeyHandler(this));
		passwordField.setOnKeyPressed(Utils.getEnterKeyHandler(this));
		exitButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> exitPressed()));
	}
	
	
	@FXML
	private void loginPressed() {
		password = passwordField.getText().trim();
		
		if (password != null && !password.equals("")) {
			dialogStage.close();
		} else {
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Encryption Key Error");
            alert.setHeaderText("Encryption Key Can't be Blank");
            alert.setContentText("You must enter a value for the encryption key. This should be the same key each time.");

            alert.showAndWait();*/
            
            Utils.showAlert(AlertType.ERROR, dialogStage, "Encryption Key Error", "Encryption Key Can't be Blank",
            		"You must enter a value for the encryption key. This should be the same key each time.");
		}
	}
	
	
	@FXML
	private void exitPressed() {
		System.exit(0);
	}
	
	
	public String getPassword() {
		return password;
	}
	
	
	
	public void setStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}


	@Override
	public void process() {
		loginPressed();
	}
}
