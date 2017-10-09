package com.passvault.ui.fx.view;

import java.util.List;
import java.util.logging.Logger;

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

public class CreateAccountLayoutController {

	@FXML
	TextField accountNameTextField;
	@FXML
	TextField userNameTextField;
	@FXML
	TextField urlTextField;
	@FXML
	PasswordField passwordTextField;
	@FXML
	PasswordField reenterPasswordTextField;
	@FXML
	Button saveButton;
	@FXML
	Button cancelButton;
	@FXML
	Button generateButton;
	@FXML
	Button optionsButton;
	
	private Passvault passvault;
	private RandomPasswordGenerator generator;
	private Stage stage;
	private Account newAccount;
	private boolean accountVaid;
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
	}
	
	
	public CreateAccountLayoutController() {
		accountVaid = false;
		newAccount = null;
		generator = passvault.getSettings().getDefaultRandomPasswordGenerator();
	}

	@FXML
	private void initialize() {
		saveButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> savePressed()));
		saveButton.setTooltip(new Tooltip("Save account"));
		cancelButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> cancelPressed()));
		cancelButton.setTooltip(new Tooltip("Cancel without saving account"));
		generateButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> generatePressed()));
		generateButton.setTooltip(new Tooltip("Generate password"));
		optionsButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> optionsPressed()));
		optionsButton.setTooltip(new Tooltip("Override the current password options"));
	}
	
	@FXML
	private void savePressed() {
		List<Account> accounts = passvault.getAccounts();
		
		String accountName = accountNameTextField.getText().trim();
		String userName = userNameTextField.getText().trim();
		String url = urlTextField.getText().trim();
		String password = passwordTextField.getText().trim();
		String reenterPassword = reenterPasswordTextField.getText().trim();
		
		if (userName == null || userName.equals("") || password == null || password.equals("") ||
				reenterPassword == null || reenterPassword.equals("") || accountName == null || 
				accountName.equalsIgnoreCase("")) {
			/*Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(stage);
            alert.setTitle("Account Error");
            alert.setHeaderText("Field(s) found with invalid value(s)");
            alert.setContentText("The account name, username, and password can't be empty");
            alert.showAndWait();*/
            
            Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Field(s) found with invalid value(s)", 
            		"The account name, username, and password can't be empty");
            return;
		}
		
		for (Account a : accounts) {
			if (accountName.equalsIgnoreCase(a.getName())) {
				accountVaid = false;
				/*Alert alert = new Alert(AlertType.ERROR);
	            alert.initOwner(stage);
	            alert.setTitle("Account Error");
	            alert.setHeaderText("Account Exists");
	            alert.setContentText("The account name already exists. Either edit the existing account or delete it.");
	            alert.showAndWait();*/
	            
	            Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Account Exists", 
	            		"The account name already exists. Either edit the existing account or delete it.");
	            
	            return;
			}
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
		
		if (url == null)
			url = "";
		
		newAccount = new Account(accountName, userName, password, password, 
				Utils.getAccountUUIDResolver().getAccountUUID(), System.currentTimeMillis(), url);
		accountVaid = true;
		logger.info("Account " + accountName + " created");
			
		stage.close();
	}
	
	@FXML
	private void cancelPressed() {
		newAccount = null;
		accountVaid = false;
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
	
	
	
	public void setPassvault(Passvault passvault) {
		this.passvault = passvault;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public Account getAccount() {
		logger.fine("Returning account: " + newAccount);
		return newAccount;
	}
	
	public boolean accountCreated() {
		return accountVaid;
	}
}
