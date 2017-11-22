package com.passvault.ui.fx.view;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.passvault.crypto.AESEngine;
import com.passvault.ui.fx.Passvault;
import com.passvault.util.data.Store;
import com.passvault.util.data.file.JsonStore;
import com.passvault.util.data.file.model.Generator;
import com.passvault.util.data.file.model.Properties;
import com.passvault.util.data.file.model.Settings;
//import com.passvault.ui.fx.utils.FXCBLStore;
import com.passvault.ui.fx.utils.GeneralSettingsUpdated;
import com.passvault.ui.fx.utils.SettingsUpdated;
import com.passvault.ui.fx.utils.TabChangedHandler;
import com.passvault.ui.fx.utils.Utils;
import com.passvault.util.DefaultRandomPasswordGenerator;
import com.passvault.util.model.Gateway;
import com.passvault.util.model.Gateways;
import com.passvault.util.register.RegisterAccount;
import com.passvault.util.register.RegisterAccount.StoreType;
import com.passvault.util.register.RegisterResponse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SettingsLayoutController {
	
	// General tab components
	@FXML
	ChoiceBox<Boolean> saveKeyChoiceBox;
	@FXML
	ChoiceBox<Boolean> sortChoiceBox;
	@FXML
	ChoiceBox<Boolean> purgeChoiceBox;
	
	// Free service sync tab components
	@FXML
	Label freeServiceMessageLabel;
	@FXML
	TextField freeServiceEmailTextField;
	@FXML
	PasswordField freeServicePasswordField;
	@FXML
	Button freeServiceLeftButton;
	@FXML
	Button freeServiceRightButton;
	
	// Personal sync tab components
	@FXML
	TextField personalServerTextField;
	@FXML
	TextField personalProtocolTextField;
	@FXML
	TextField personalDBNameTextField;
	@FXML
	TextField personalUserNameTextField;
	@FXML
	PasswordField personalPasswordField;
	@FXML
	TextField personalPortTextField;
	@FXML
	Button personalSaveButton;
	@FXML
	Button personalDeleteButton;
	
	@FXML
	TabPane tabPane;


	private GeneratorOptionsLayoutController optionsController;
	private Passvault passvault;
	private Settings settings;
	private Stage stage;
	private TabChangedHandler tabHandler;
	private List<SettingsUpdated> settingsUpdatedList;

	
	private static final String NO_FREE_SERVICE = "In order to sync passwords with the free service an account is " +
			"needed. In order to create an account enter a valid email address/password and click Create. If a account " + 
			"already exists for this email address enter it along with the existing password and click Configure.";
	private static final String NO_FREE_SERVICE_LEFT_BUTTON = "Create";
	private static final String NO_FREE_SERVICE_RIGHT_BUTTON = "Configure";
	private static final String YES_FREE_SERVICE = "An account to sync with the free service is already configured. " +
			"To delete the account on this device along with removing the account on the free service click Delete. " + 
			"To remove the account from this device but keep the account active on the free service click Remove.";
	private static final String YES_FREE_SERVICE_LEFT_BUTTON = "Delete";
	private static final String YES_FREE_SERVICE_RIGHT_BUTTON = "Remove";
	
	private static String REGISTER_SERVER = "ec2-13-56-39-109.us-west-1.compute.amazonaws.com:8443";
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
		REGISTER_SERVER = System.getProperty("com.passvault.register.server", REGISTER_SERVER);
		logger.finest("Registration server URL set to: " + REGISTER_SERVER);
	}
	
	public SettingsLayoutController() {
		settings = Passvault.getSettings();
		settingsUpdatedList = new ArrayList<>();
	}
	
	
	@FXML
	private void initialize() {
		/*
		 * Json will not allow own sync for now, so remove it but keep it in the code possibly to be added back
		 */
		Tab syncPersonalTab = null;
		
		for (Tab tab : tabPane.getTabs()) {
			if (tab.getText().equalsIgnoreCase("Sync Personal")) {
				syncPersonalTab = tab;
			}
		}
		
		if (syncPersonalTab != null)
			tabPane.getTabs().remove(syncPersonalTab);
		
		// setup General tab components
		ObservableList<Boolean> booleanList = FXCollections.observableArrayList();
		booleanList.add(true);
		booleanList.add(false); 
		saveKeyChoiceBox.setItems(booleanList);
		saveKeyChoiceBox.setValue(settings.getGeneral().isSaveKey());
		sortChoiceBox.setItems(booleanList);
		sortChoiceBox.setValue(settings.getGeneral().isSortMRU());
		purgeChoiceBox.setItems(booleanList);
		purgeChoiceBox.setValue(settings.getDatabase().isPurge());
		purgeChoiceBox.setOnKeyPressed((KeyEvent e) -> {
			System.out.println("purgeChoice: " + e.getCode().getName());
			if (e.getCode() == KeyCode.TAB)  {
				//saveKeyChoiceBox.setFocusTraversable(false);
				//sortChoiceBox.setFocusTraversable(false);
				//purgeChoiceBox.setFocusTraversable(false);
				tabPane.requestFocus();
				e.consume();
				
			} 
		});
		
		// setup free service sync tab
		Gateways gateways = settings.getSync();
		
		if (gateways != null) {
			Gateway gateway = gateways.getRemote();
			
			if (gateway != null && gateway.getUserName() != null && !gateway.getUserName().equalsIgnoreCase("") &&
					gateway.getPassword() != null && !gateway.getPassword().equalsIgnoreCase("")) {
			
				flipToYesFreeService(gateway.getUserName(), gateway.getPassword());
			} else {
				flipToNoFreeService();
			}
			
			freeServiceLeftButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> freeServiceLeftButtonPressed()));
			//freeServiceRightButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> freeServiceRightButtonPressed()));
			freeServiceRightButton.setOnKeyPressed((KeyEvent e) -> {
				if (e.getCode() == KeyCode.TAB)  {
					tabPane.requestFocus();
					e.consume();
				} else if (e.getCode() == KeyCode.ENTER)  {
					freeServiceRightButtonPressed();
				}
			});
		} else {
			settings.setSync(new Gateways());
			flipToNoFreeService();
		}
		
		
		// setup personal sync tab
		if (gateways != null) {
			Gateway[] local = gateways.getLocal();
			
			if (local != null && local.length > 0 && local[0] != null) {
				String server = local[0].getServer();
				String protocol = local[0].getProtocol();
				String db = local[0].getBucket();
				int port = local[0].getPort();
				String userName = local[0].getUserName();
				String password = local[0].getPassword();
				
				if (verifyField(server))
					personalServerTextField.setText(server);
				
				if (verifyField(protocol))
					personalProtocolTextField.setText(protocol);
				
				if (verifyField(db))
					personalDBNameTextField.setText(db);
				
				if (verifyField(userName))
					personalUserNameTextField.setText(userName);
				
				if (verifyField(password))
					personalPasswordField.setText(password);
				
				if (port > 0)
					personalPortTextField.setText(Integer.toString(port));
			}
		}
		
		personalSaveButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> personalSaveButtonPressed()));
		personalDeleteButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> personalDeleteButtonPressed()));
		personalSaveButton.setTooltip(new Tooltip("Save the personal sync gateway configuration"));
		personalDeleteButton.setTooltip(new Tooltip("Delete the personal sync gateway configuration"));
		personalSaveButton.setOnKeyPressed((KeyEvent e) -> {
			if (e.getCode() == KeyCode.TAB)  {
				tabPane.requestFocus();
				e.consume();
			} 
		});
	}
	
	
	
	public void setPassvault(Passvault passvault) {
		this.passvault = passvault;
	}
	
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	public void setOptionsController(GeneratorOptionsLayoutController optionsController) {
		this.optionsController = optionsController;
	}
	
/*	
public void setTabPane(TabPane tabPane) {
	this.tabPane = tabPane;
}
*/	
	
	public void setTabChangedHandler(TabChangedHandler tabHandler) {
		this.tabHandler = tabHandler;
		tabHandler.setController(this);
	}
	
	/*
	 * Verify tab content and save to Settings (not persist) if ok. If not show alert dialog and don't save
	 */
	public void saveTab(String tab) {
		logger.info("Checking tab: " + tab);
		
		switch (tab) {
		case TabChangedHandler.GENERAL:
			settings.getGeneral().setSaveKey(saveKeyChoiceBox.getValue());
			
			if (settings.getGeneral().isSaveKey()) {
				//String key = ((FXCBLStore)passvault.getDatabase()).getKey();
				String key = passvault.getDatabase().getEncryptionKey();
				
				String _key = new String(passvault.getDatabase().encodeBytes(AESEngine.getInstance()
						.encryptString(passvault.getCOMMON_KEY(), key)));
				settings.getGeneral().setKey(_key);
			} else {
				settings.getGeneral().setKey("");
			}
			
			settings.getGeneral().setSortMRU(sortChoiceBox.getValue());
			settings.getDatabase().setPurge(purgeChoiceBox.getValue());
			settingsUpdatedList.add(new GeneralSettingsUpdated(passvault));
			
			break;
		case TabChangedHandler.GENERATOR:
			
			if (optionsController.verifyState()) {
				optionsController.setGeneratorOptions();
				DefaultRandomPasswordGenerator rpg = (DefaultRandomPasswordGenerator)optionsController.getGenerator();
				Generator generator = settings.getGenerator();
				
				if (generator == null)
					generator = new Generator();
				
				Properties properties = generator.getProperties();
				
				if (properties == null)
					properties = new Properties();
				
				properties.setAllowedCharacters(rpg.getAllowedCharactres());
				properties.setLength(rpg.getLength());
				generator.setProperties(properties);	
				generator.setOverRide(true);
				settings.setGenerator(generator);
			} else {
				logger.warning("There were errors with the Generator tab values");
				/*Alert alert = new Alert(AlertType.ERROR);
	            alert.initOwner(stage);
	            alert.setTitle("Save Error");
	            alert.setHeaderText("Generator Tab willl not be saved");
	            alert.setContentText("The contents of the Generator tab will not be saved until errors are fixed.");
	            alert.showAndWait();*/
	            
	            Utils.showAlert(AlertType.ERROR, stage, "Save Error", "Generator Tab willl not be saved",
	            		"The contents of the Generator tab will not be saved until errors are fixed.");
			}

			break;
		case TabChangedHandler.SYNC_FREE:
			// this tab requires interaction with back end and all settings savings take place then, nothing to do
			break;
		case TabChangedHandler.SYNC_PERSONAL:
			
			break;
		default:
			logger.warning("Invalid tab: " + tab);
			break;
		}
	}
	
	
	public String getLastTab() {
		return tabHandler.getCurrentTab();
	}


	public List<SettingsUpdated> getSettingsUpdatedList() {
		return settingsUpdatedList;
	}
	
	
	@FXML
	private void freeServiceLeftButtonPressed() {
		if (freeServiceLeftButton.getText().equalsIgnoreCase(YES_FREE_SERVICE_LEFT_BUTTON)) {
			/*
			 * Delete
			 */
			Task<Object> deleteTask = new Task<Object>() {
				@Override
				protected Object call() throws Exception {
					RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER, StoreType.JSON);
					return registerAccount.deleteAccount(freeServiceEmailTextField.getText(), 
														freeServicePasswordField.getText());
				}
			};
			
			passvault.showProgress("Delete Free Service Account", stage, deleteTask);
			RegisterResponse response = (RegisterResponse) deleteTask.getValue();
			
			//RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER);
			//RegisterResponse response = 
			//		registerAccount.deleteAccount(freeServiceEmailTextField.getText(), freeServicePasswordField.getText());
			
			if (response.success()) {
				Utils.showAlert(AlertType.CONFIRMATION, stage, "Register Response", "Success", response.getMessage());
			} else {
				Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Error during call", response.getError() +
						"\n\nThe local configuration will still be removed.");
			}
			
			// success or not remove gateway config and update accountUUID
			settings.getSync().setRemote(null);
			settings.getGeneral().setAccountUUID("");
			flipToNoFreeService();
			//settingsUpdatedList.add(new FreeSyncSettingsUpdated(passvault, ""));
			Store store = passvault.getDatabase();
			store.saveSettings(settings);
			store.updateAccountUUID(passvault.getAccounts(), "");
		} else {
			/*
			 * Create
			 */
			String email = freeServiceEmailTextField.getText().trim();
			String password = freeServicePasswordField.getText().trim();
			
			if (email == null || email.equalsIgnoreCase("") || password == null || password.equalsIgnoreCase("")) {
				Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Value missing", 
						"Both email and password must have a value");
				return;
			}
			
			Task<Object> createTask = new Task<Object>() {
				@Override
				protected Object call() throws Exception {
					RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER, StoreType.JSON);
					return registerAccount.registerV1(email, password);
				}
			};
			
			passvault.showProgress("Create Free Service Account", stage, createTask);
			RegisterResponse response = (RegisterResponse) createTask.getValue();
			
			//RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER);
			//RegisterResponse response = registerAccount.registerV1(email, password);
			
			if (response.success()) {
				
				if (response.hasReturnValue()) {
					Object gateway = response.getReturnValue();
					
					if (!(gateway instanceof Gateway)) {
						Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Inalid return type", 
								response.getMessage());
						return;
					}
					
					settings.getSync().setRemote((Gateway)gateway);
					settings.getGeneral().setAccountUUID(email);
					//settingsUpdatedList.add(new FreeSyncSettingsUpdated(passvault, email));
					Store store = passvault.getDatabase();
					store.saveSettings(settings);
					store.updateAccountUUID(passvault.getAccounts(), email);
					flipToYesFreeService(email, password);
					Utils.showAlert(AlertType.CONFIRMATION, stage, "Register Response", "Success", response.getMessage());
				} else {
					Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Inalid return type", 
							response.getMessage());
				}
				
			} else {
				Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Error during call", response.getError() +
						"\n\nThe account was not created.");
			}
		}
	}
	
	@FXML
	private void freeServiceRightButtonPressed() {
		if (freeServiceRightButton.getText().equalsIgnoreCase(YES_FREE_SERVICE_RIGHT_BUTTON)) {
			/*
			 * Remove
			 */
			settings.getSync().setRemote(null);
			settings.getGeneral().setAccountUUID("");
			flipToNoFreeService();
			//settingsUpdatedList.add(new FreeSyncSettingsUpdated(passvault, ""));
			Store store = passvault.getDatabase();
			store.saveSettings(settings);
			store.updateAccountUUID(passvault.getAccounts(), "");
			Utils.showAlert(AlertType.CONFIRMATION, stage, "Account Removal", "Success", 
					"The account is no longer configured");
		} else {
			/*
			 * Configure
			 */
			String email = freeServiceEmailTextField.getText().trim();
			String password = freeServicePasswordField.getText().trim();
			
			if (email == null || email.equalsIgnoreCase("") || password == null || password.equalsIgnoreCase("")) {
				Utils.showAlert(AlertType.ERROR, stage, "Account Error", "Value missing", 
						"Both email and password must have a value");
				return;
			}
			
			Task<Object> configureTask = new Task<Object>() {
				@Override
				protected Object call() throws Exception {
					RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER, StoreType.JSON);
					return registerAccount.getGatewatConfig();
				}
			};
			
			passvault.showProgress("Configure Free Service Account", stage, configureTask);
			RegisterResponse response = (RegisterResponse) configureTask.getValue();
			
			//RegisterAccount registerAccount = new RegisterAccount(REGISTER_SERVER);
			//RegisterResponse response = registerAccount.getGatewatConfig();
			
			if (response.success()) {
				
				if (response.hasReturnValue()) {
					Object gateway = response.getReturnValue();
					
					if (!(gateway instanceof Gateway)) {
						Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Inalid return type", 
								response.getMessage());
						return;
					}
					
					((Gateway)gateway).setUserName(email);
					((Gateway)gateway).setPassword(password);
					settings.getSync().setRemote((Gateway)gateway);
					settings.getGeneral().setAccountUUID(email);
					Store store = passvault.getDatabase();
					store.saveSettings(settings);
					store.updateAccountUUID(passvault.getAccounts(), email);
					flipToYesFreeService(email, password);
					Utils.showAlert(AlertType.CONFIRMATION, stage, "Register Response", "Success", response.getMessage());
				} else {
					Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Inalid return type", 
							response.getMessage());
				}
			} else {
				Utils.showAlert(AlertType.ERROR, stage, "Register Response", "Error during call", response.getError() +
						"\n\nThe account was not configured.");
			}
		}
	}
	
	@FXML
	private void personalSaveButtonPressed() {
		String server = personalServerTextField.getText().trim();
		String protocol = personalProtocolTextField.getText().trim();
		String port = personalPortTextField.getText().trim();
		String db = personalDBNameTextField.getText().trim();
		
		if (!(verifyField(server) && verifyField(protocol) && verifyField(port) && verifyField(db))) {
			Utils.showAlert(AlertType.ERROR, stage, "Input Error", "Field(s) not present", 
					"Server, Protocol, Port, and Database fields must be filled out");
			return;
		}
		
		int portNumber;
		
		try {
			portNumber = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			Utils.showAlert(AlertType.ERROR, stage, "Input Error", "Port Invalid Value", 
					"The port number needs to be an integer value");
			return;
		}
		
		if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https")) {
			Utils.showAlert(AlertType.ERROR, stage, "Input Error", "Protocol Invalid Value", 
					"The protocol value needs to be either http or https");
			return;
		}

		// For local sync there is no confirmation until sync time, set it and forget it
		Gateway local = new Gateway();
		local.setServer(server);
		local.setProtocol(protocol);
		local.setPort(portNumber);
		local.setBucket(db);
		
		if (verifyField(personalUserNameTextField.getText().trim()))
			local.setUserName(personalUserNameTextField.getText().trim());
		
		if (verifyField(personalPasswordField.getText().trim()))
			local.setPassword(personalPasswordField.getText().trim());
		
		settings.getSync().setLocal(new Gateway[] {local});
		// go ahead and persist when gateway is entered before showing confirmation
		passvault.getDatabase().saveSettings(settings);
		
		Utils.showAlert(AlertType.CONFIRMATION, stage, "Gateway Saved", "Gateway Save", 
				"The personal gateway settings have been saved and are ready to use");
	}
	
	@FXML
	private void personalDeleteButtonPressed() {
		Gateway[] local = new Gateway[1];
		local[0] = null;
		settings.getSync().setLocal(local);
		personalServerTextField.clear();
		personalProtocolTextField.clear();
		personalPortTextField.clear();
		personalDBNameTextField.clear();
		personalUserNameTextField.clear();
		personalPasswordField.clear();
		//for personal let persistence happen when the other settings get saved
	}
	
	
	private boolean verifyField(String field) {
		return (field != null && !field.equalsIgnoreCase(""));
	}
	
	
	private void flipToNoFreeService() {
		freeServiceEmailTextField.setText("");
		freeServiceEmailTextField.setEditable(true);
		freeServicePasswordField.setText("");
		freeServicePasswordField.setEditable(true);
		freeServiceLeftButton.setText(NO_FREE_SERVICE_LEFT_BUTTON);
		freeServiceLeftButton.setTooltip(new Tooltip("Create a new free service account"));
		freeServiceRightButton.setText(NO_FREE_SERVICE_RIGHT_BUTTON);
		freeServiceRightButton.setTooltip(new Tooltip("Configure passvault to use an existing\nfree service account"));
		freeServiceMessageLabel.setText(NO_FREE_SERVICE);
	}
	
	private void flipToYesFreeService(String email, String password) {
		freeServiceEmailTextField.setText(email);
		freeServiceEmailTextField.setEditable(false);
		freeServicePasswordField.setText(password);
		freeServicePasswordField.setEditable(false);
		freeServiceLeftButton.setText(YES_FREE_SERVICE_LEFT_BUTTON);
		freeServiceLeftButton.setTooltip(new Tooltip("Delete the free service account from\nthe free service"));
		freeServiceRightButton.setText(YES_FREE_SERVICE_RIGHT_BUTTON);
		freeServiceRightButton.setTooltip(new Tooltip("Remove the free service account from\npassvault but keep the account" + 
													"\non the server"));
		freeServiceMessageLabel.setText(YES_FREE_SERVICE);
	}
}
