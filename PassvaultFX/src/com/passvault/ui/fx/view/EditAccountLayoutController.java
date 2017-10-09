package com.passvault.ui.fx.view;

import com.passvault.ui.fx.Passvault;
import com.passvault.ui.fx.utils.Utils;
import com.passvault.util.Account;
import com.passvault.util.DefaultRandomPasswordGenerator;
import com.passvault.util.RandomPasswordGenerator;

import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class EditAccountLayoutController {

	@FXML
	TextField userNameTextField;
	@FXML
	TextField urlTextField;
	@FXML
	PasswordField passwordTextField;
	@FXML
	PasswordField reenterPasswordTextField;
	@FXML
	Button generateButton;
	@FXML
	Button optionsButton;
	@FXML
	Button saveButton;
	@FXML
	Button cancelButton;
	
	private Account account;
	private Stage stage;
	private boolean accountSaved;
	private RandomPasswordGenerator generator;
	private Passvault passvault;
	
	public EditAccountLayoutController() {
		generator = passvault.getSettings().getDefaultRandomPasswordGenerator();
	}
	
	@FXML
	private void initialize() {
		//generator = null;
		accountSaved = false;
		saveButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> savePressed()));
		saveButton.setTooltip(new Tooltip("Save changes to account"));
		cancelButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> cancelPressed()));
		cancelButton.setTooltip(new Tooltip("Cancel without saving account changes"));
		generateButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> generatePressed()));
		generateButton.setTooltip(new Tooltip("Generate password"));
		optionsButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> optionsPressed()));
		optionsButton.setTooltip(new Tooltip("Override the current password options"));
	}
	
	
	@FXML
	private void cancelPressed() {
		accountSaved = false;
		stage.close();
	}
	
	
	@FXML
	private void savePressed() {
		boolean accountChanged = false;
		
		String userName = userNameTextField.getText().trim();
		String url = urlTextField.getText().trim();
		String password = passwordTextField.getText().trim();
		String reenterPassword = reenterPasswordTextField.getText().trim();
		
		if (userName == null || userName.equals("") || password == null || password.equals("") ||
				reenterPassword == null || reenterPassword.equals("")) {
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Account Error");
            alert.setHeaderText("Field(s) found with invalid value(s)");
            alert.setContentText("The account username and password can't be empty");
            alert.showAndWait();*/
            
            Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Field(s) found with invalid value(s)", 
            		"The account name, username, and password can't be empty");
            
            return;
		}
		
		if (!password.equals(reenterPassword)) {
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Account Error");
            alert.setHeaderText("Passwords fields don't match");
            alert.setContentText("Both passwords must match each other");
            alert.showAndWait();*/
            
            Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Passwords fields don't match", 
            		"Both passwords must match each other");
            
            return;
		}
		
		if (!userName.equals(account.getUser())) {
			account.setUser(userName);
			accountChanged = true;
		}
		
		if (url != null && !url.equals(account.getUrl())) {
			if (!(url.equalsIgnoreCase("") && account.getUrl().equalsIgnoreCase("http://"))) {
				account.setUrl(url);
				accountChanged = true;
			}
		}
		
		if (!password.equals(account.getPass())) {
			account.setOldPass(account.getPass());
			account.setPass(password);
			accountChanged = true;
		}
			
		if (accountChanged)
			accountSaved = true;
			
		stage.close();
	}
	
	
	@FXML
	private void generatePressed() {
		
		if (generator == null)
			generator = DefaultRandomPasswordGenerator.getInstance();
		
		String password = generator.generatePassword();
		passwordTextField.setText(password);
		reenterPasswordTextField.setText(password);
	}
	
	
	@FXML
	private void optionsPressed() {
		generator = passvault.getCustomGenerator();
		
		if (generator == null)
			generator = passvault.getSettings().getDefaultRandomPasswordGenerator();
	}
	
	
	public void setAccount(Account account) {
		this.account = account;
		populateFields();
	}
	
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	public void setPassvault(Passvault passvault) {
		this.passvault = passvault;
	}
	
	
	public boolean accountSaved() {
		return accountSaved;
	}
	
	
	private void populateFields() {
		userNameTextField.setText(account.getUser());
		passwordTextField.setText(account.getPass());
		reenterPasswordTextField.setText(account.getPass());
		
		if (!account.getUrl().equalsIgnoreCase("http://"))
			urlTextField.setText(account.getUrl());
	}
}
