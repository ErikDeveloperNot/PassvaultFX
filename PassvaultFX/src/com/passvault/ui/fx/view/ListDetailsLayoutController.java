package com.passvault.ui.fx.view;

import java.util.logging.Logger;

import com.passvault.crypto.AESEngine;
import com.passvault.ui.fx.Passvault;
import com.passvault.ui.fx.utils.AccountDetailsShowing;
import com.passvault.ui.fx.utils.Utils;
import com.passvault.util.Account;
import com.passvault.util.MRUComparator;
import com.passvault.util.data.Store;
//import com.passvault.util.data.couchbase.CBLStore;
import com.passvault.util.data.file.JsonStore;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class ListDetailsLayoutController {
	         
	private Passvault passvault;
	private ToolBar toolBar;
	private boolean showDetails;
	private Account currentAccount;
	private AccountDetailsShowing accountDetailsShowing;
	private ChangeListener<Number> dividerListener;
	
	
	@FXML
	ListView<Account> listView;
	@FXML
	Label accountNameLabel;
	@FXML
	Label userNameLabel;
	@FXML
	Label urlLabel;
	@FXML
	ToolBar detailsToolBar;
	@FXML
	Button copyPasswordButton;
	/*@FXML
	Button copyOldPasswordButton;*/
	@FXML
	Button launchBrowserButton;
	@FXML
	Button editButton;
	@FXML
	Button deleteButton;
	@FXML
	Button backButton;
	@FXML
	Label cantDecryptLabel;
	@FXML
	Button decryptButton;
	@FXML
	Pane fillerPane;
	
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
	}
	
	public ListDetailsLayoutController() {
		showDetails = false;
		dividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			passvault.getListDetailDivider().setPosition(1.0); };
	}
	

	@FXML
	private void initialize() {
		// customize cell appearance
		listView.setCellFactory((ListView<Account> a) -> new AccountCell());
		
		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Account toShow = listView.getSelectionModel().getSelectedItem();
				
				if (toShow != null)
					showAccountDetails(toShow);
			
			}
		});
		
		listView.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ENTER) {
					Account toShow = listView.getSelectionModel().getSelectedItem();
					
					if (toShow != null)
						showAccountDetails(toShow);
				} else if(event.getCode() == KeyCode.RIGHT) {
					event.consume();
				}
			}
		});
		
		listView.focusedProperty().addListener(
				(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		    
					if (newValue) {
						
						if (listView.getSelectionModel().getSelectedIndex() == -1) {
							listView.getSelectionModel().select(0);
						}
					} else {
						
						if (!showDetails) {
							listView.getSelectionModel().clearSelection();
						}
					}
					
		});
		
		backButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> backButtonPressed()));
		backButton.setTooltip(new Tooltip("Back to accounts list"));
		copyPasswordButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> copyButtonPressed()));
		copyPasswordButton.setTooltip(new Tooltip("Copy password to clipboard"));
		/*copyOldPasswordButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> copyOldButtonPressed()));
		copyOldPasswordButton.setTooltip(new Tooltip("Copy old password to clipboard"));*/
		deleteButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> deleteButtonPressed()));
		deleteButton.setTooltip(new Tooltip("Delete account"));
		editButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> editButtonPressed()));
		editButton.setTooltip(new Tooltip("Edit account details"));
		decryptButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> decryptButtonPressed()));
		decryptButton.setTooltip(new Tooltip("Enter this accounts encryption\nkey to decrypt it"));
		launchBrowserButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> browserButtonPressed()));
		launchBrowserButton.setTooltip(new Tooltip("Copy account password to clipboard\nand launch browser"));
		
		// space account control buttons on right and back button on left
		HBox.setHgrow(fillerPane, Priority.SOMETIMES);
		
		setDetailsButtonsFocusTransferable(false);
	}
	
	
	public void setPassvaultApp(Passvault passvault, ToolBar toolBar) {
		this.passvault = passvault;
		this.toolBar = toolBar;
	}
	
	
	private void showAccountDetails(Account account) {
		currentAccount = account;
		showDetails = true;
		setPane();
		accountNameLabel.setText(account.getName());
		userNameLabel.setText(account.getUser());
		
		String url = account.getUrl();
		
		if (url != null && !url.equalsIgnoreCase("http://")) {
			launchBrowserButton.setDisable(false);
			urlLabel.setText(account.getUrl());
		} else {
			urlLabel.setText("");
			launchBrowserButton.setDisable(true);
		}
		
		if (account.isValidEncryption()) {
			copyPasswordButton.setDisable(false);
			editButton.setDisable(false);
			//launchBrowserButton.setDisable(false);
			//copyOldPasswordButton.setDisable(false);
			decryptButton.setVisible(false);
			cantDecryptLabel.setVisible(false);
		} else {
			copyPasswordButton.setDisable(true);
			//copyOldPasswordButton.setDisable(true);
			editButton.setDisable(true);
			launchBrowserButton.setDisable(true);
			decryptButton.setVisible(true);
			cantDecryptLabel.setVisible(true);
		}
		
		setDetailsButtonsFocusTransferable(true);
		
	}
	
	
	public void setPane() {
		
		if (showDetails) {
			passvault.getListDetailDivider().positionProperty().removeListener(dividerListener);
			
			dividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
				passvault.getListDetailDivider().setPosition(0.0); };
			
			passvault.getListDetailDivider().positionProperty().addListener(dividerListener);
			passvault.getListDetailDivider().setPosition(0.0);
			accountDetailsShowing.isShowing(true);
		} else {
			passvault.getListDetailDivider().positionProperty().removeListener(dividerListener);
			
			dividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
				passvault.getListDetailDivider().setPosition(1.0); };
			
			passvault.getListDetailDivider().positionProperty().addListener(dividerListener);	
			passvault.getListDetailDivider().setPosition(1.0);
			currentAccount = null;
			accountDetailsShowing.isShowing(false);
		}
	}
	
	
	public void setAccountsList(ObservableList<Account> accountsList) {
		listView.setItems(accountsList);
	}
	
	/*
	public void PopulateListTest() {
		listView.setItems(loadTestData());
	}
	*/

	/* 
	 * Button Callbacks
	 */
	@FXML
	protected void deleteButtonPressed() {
		logger.info("Deleting account: " + currentAccount.getName());
		MRUComparator mruComparator = MRUComparator.getInstance();
		listView.getItems().remove(currentAccount);
		currentAccount.setUpdateTime(System.currentTimeMillis());
		mruComparator.accountRemoved(currentAccount.getName());
		mruComparator.saveAccessMap(passvault.getDatabase());
		passvault.getDatabase().deleteAccount(currentAccount);
		showDetails = false;
		setPane();
		setDetailsButtonsFocusTransferable(false);
		listView.getSelectionModel().clearSelection();
	}

	@FXML
	protected void copyButtonPressed() {
		Utils.copyPassword(currentAccount.getPass());
		MRUComparator.getInstance().accountAccessed(currentAccount.getName());
		Utils.sortAccounts(passvault.getAccounts(), passvault.getSettings());
		
		
//only for testing, will only save on app exit
MRUComparator.getInstance().saveAccessMap(passvault.getDatabase());
	}
	
	@FXML
	protected void copyOldButtonPressed() {
		Utils.copyPassword(currentAccount.getOldPass());
		MRUComparator.getInstance().accountAccessed(currentAccount.getName());
		MRUComparator.getInstance().saveAccessMap(passvault.getDatabase());
	}
	
	@FXML
	protected void editButtonPressed() {
		logger.finest("Editing account: " + currentAccount);
		boolean accountSaved = passvault.editAccount(currentAccount);
		//System.out.println(accountSaved);
		
		if (accountSaved) {
			userNameLabel.setText(currentAccount.getUser());
			urlLabel.setText(currentAccount.getUrl());
			currentAccount.setUpdateTime(System.currentTimeMillis());
			passvault.getDatabase().saveAccount(currentAccount);
			
			if (currentAccount.getUrl().equalsIgnoreCase("http://")) 
				launchBrowserButton.setDisable(true);
			else
				launchBrowserButton.setDisable(false);
		}
	}
	
	@FXML
	private void browserButtonPressed() {
		Utils.copyPassword(currentAccount.getPass());
		MRUComparator.getInstance().accountAccessed(currentAccount.getName());
		MRUComparator.getInstance().saveAccessMap(passvault.getDatabase());
		Utils.launchBrowser(currentAccount.getUrl());
	}
	
	@FXML
	private void backButtonPressed() {
		showDetails = false;
		//passvault.getListDetailDivider().setPosition(1.0);
		setPane();
		accountDetailsShowing.isShowing(false);
		setDetailsButtonsFocusTransferable(false);
		//listView.requestFocus();
		listView.getSelectionModel().clearSelection();
	}
	

	@FXML
	private void decryptButtonPressed() {
		String oldKey = passvault.getOldKey();
		//CBLStore cblStore = passvault.getDatabase();
		Store store = passvault.getDatabase();
		String password = null;
		String oldPassword = null;
		
		if (oldKey != null && !oldKey.equalsIgnoreCase("")) {
			try {
				oldKey = AESEngine.finalizeKey(oldKey, AESEngine.KEY_LENGTH_256);
				//cblStore = passvault.getDatabase();
				
				password = 
						AESEngine.getInstance().decryptBytes(oldKey, store.decodeString(currentAccount.getPass()));
				oldPassword = 
						AESEngine.getInstance().decryptBytes(oldKey, store.decodeString(currentAccount.getOldPass()));
			} catch(Exception e) {
				logger.warning("Error decrypting password with old key for account: " + currentAccount + "\n" + e.getMessage());
				e.printStackTrace();
			}
			
			if (password != null) {
				currentAccount.setPass(password);
			} else {
				Utils.showAlert(AlertType.ERROR, passvault.getPrimaryStage(), "Decryption Error", 
	            		"Error decrypting password", "Unable to decrypt the password with the key supplied.");
				return;
			}
			
			
			if (oldPassword != null) {
				currentAccount.setOldPass(oldPassword);
			} else {
				// fallback old to current instead of failing
				logger.info("Was unable to decrypt the old password, so using the current password as the old");
				currentAccount.setOldPass(password);
			}
			
			currentAccount.setUpdateTime(System.currentTimeMillis());
			currentAccount.setValidEncryption(true);
			// save account to encrypt the password with the current key
			store.saveAccount(currentAccount);
			decryptButton.setVisible(false);
			cantDecryptLabel.setVisible(false);
			copyPasswordButton.setDisable(false);
			Utils.sortAccounts(passvault.getAccounts(), passvault.getSettings());
		}
	}
	
	
	public void setAccountDetailsShowing(AccountDetailsShowing accountDetailsShowing) {
		this.accountDetailsShowing = accountDetailsShowing;
	}
	
	
	protected Account getCurrentAccount() {
		return currentAccount;
	}
	
	
	protected void setDetailsButtonsFocusTransferable(boolean canFocus) {
		deleteButton.setFocusTraversable(canFocus);
		backButton.setFocusTraversable(canFocus);
		listView.setFocusTraversable(!canFocus);
		
		if (canFocus && !currentAccount.isValidEncryption())
			decryptButton.setFocusTraversable(canFocus);
		else
			decryptButton.setFocusTraversable(false);
		
		if (canFocus && currentAccount.isValidEncryption()) {
			copyPasswordButton.setFocusTraversable(canFocus);
			editButton.setFocusTraversable(canFocus);
			
			if (!currentAccount.getUrl().equalsIgnoreCase("http://"))
				launchBrowserButton.setFocusTraversable(canFocus);
			else
				launchBrowserButton.setFocusTraversable(false);
		} else {
			copyPasswordButton.setFocusTraversable(false);
			editButton.setFocusTraversable(false);
			launchBrowserButton.setFocusTraversable(false);
		}
	}
	
	/*
	 * Used to customize listview cells
	 */
	private class AccountCell extends ListCell<Account> {

		@Override
		protected void updateItem(Account account, boolean empty) {
			// as of docs the call to super needs to happen along with following check
			super.updateItem(account, empty);
			
			if (account == null || empty) {
				setText(null);
		        setGraphic(null);
			} else {
				setPrefWidth(listView.getWidth() - 20.0);
				setMaxWidth(listView.getWidth() - 20.0);
				setText(account.toString());
//System.out.println(">> Account: " + account + ", isValidEncryptioin: " + account.isValidEncryption());				
				if (account.isValidEncryption()) {
					setStyle("-fx-text-fill: black;");// font-weight: regular;");
					
				} else {
					setStyle("-fx-text-fill: lightgray;");// -fx-font-weight: regular;");
				}
			}
			
			
		}
		
		
	}
	
	
	/*
	 * For Testing
	 
	private ObservableList<Account> loadTestData() {
		List<Account> accounts = new ArrayList<>();
		
		for (int i=1; i<= 30; i++) {
			accounts.add(new Account("Test Accounts " + i,
									"user" + i, 
									"password", 
									"oldPassword", 
									null, 
									System.currentTimeMillis(), 
									"www.yahoo.com"));
		}
		
		accounts.add(new Account("No URL Association for Account",
				"user", 
				"password", 
				"oldPassword", 
				null, 
				System.currentTimeMillis(), 
				""));

		
		return FXCollections.observableArrayList(accounts);
	}
	*/
	
}
