package com.passvault.ui.fx.view;

import java.util.Collections;
import java.util.logging.Logger;

import com.passvault.crypto.AESEngine;
import com.passvault.ui.fx.Passvault;
import com.passvault.data.Store;
import com.passvault.data.file.model.Settings;
import com.passvault.ui.fx.utils.AccountDetailsShowing;
//import com.passvault.ui.fx.utils.FXCBLStore;
import com.passvault.ui.fx.utils.Utils;
import com.passvault.util.Account;
import com.passvault.util.MRUComparator;
import com.passvault.model.Gateway;
import com.passvault.model.Gateways;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class RootLayoutController {
	
	@FXML
	ToolBar toolBar;

	// toolbar buttons
	@FXML
	Button createButton;
	@FXML
	Button settingsButton;
	@FXML
	Button syncButton;
	@FXML
	Button helpButton;
	/*
	@FXML
	Button sortAZButton;
	@FXML
	Button sortMOAButton;
	*/
	@FXML
	Pane fillerPane;
	
	// menu items
	@FXML
	MenuBar menuBar;
	@FXML
	Menu fileMenu;
	@FXML
	Menu actionsMenu;
	@FXML
	Menu helpMenu;
	@FXML
	MenuItem syncFreeMenuItem;
	/*@FXML
	MenuItem syncPersonalMenuItem;*/
	@FXML
	MenuItem editMenuItem;
	@FXML
	MenuItem deleteMenuItem;
	@FXML
	MenuItem copyMenuItem;
	@FXML
	MenuItem copyOldMenuItem;
	@FXML
	MenuItem launchBrowserMenuItem;
	@FXML
	MenuItem changeKeyMenuItem;
	
	//menu button stuff
	@FXML
	MenuButton sortMenuButton;
	@FXML
	MenuItem sortMOAMenuItem;
	@FXML
	MenuItem sortAZMenuItem;
	
	
	private Passvault passvault;
	private static Logger logger;
	private AccountDetailsShowing accountDetailsShowing;
	private ObservableList<Node> barButtons;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.view");
	}
	
	
	public RootLayoutController() {
	}

	
	@FXML
	private void initialize() {
		createButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> createPressed()));
		createButton.setTooltip(new Tooltip("Create a new account"));
		settingsButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> settingsPressed()));
		settingsButton.setTooltip(new Tooltip("Settings"));
		/*sortMOAButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> sortPressed()));
		sortMOAButton.setTooltip(new Tooltip("Sort accounts using MOA\n(most often accessed)"));
		sortAZButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> sortPressed()));
		sortAZButton.setTooltip(new Tooltip("Sort accounts using Alphabetically"));*/
		syncButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> syncPressed()));
		syncButton.setTooltip(new Tooltip("Synchronize with free service"));
		syncButton.setDisable(true);
		syncFreeMenuItem.setDisable(true);
		//syncPersonalMenuItem.setDisable(true);
		helpButton.setOnKeyPressed(Utils.getEnterKeyHandler(() -> helpPressed()));
		helpButton.setTooltip(new Tooltip("Open up help"));
		editMenuItem.setDisable(true);
		deleteMenuItem.setDisable(true);
		copyMenuItem.setDisable(true);
		copyOldMenuItem.setDisable(true);
		launchBrowserMenuItem.setDisable(true);
		
		// allows help button to always remain on right
		HBox.setHgrow(fillerPane, Priority.SOMETIMES);
		
		Settings settings = passvault.getSettings();
		Gateways gateways = settings.getSync();
		
		if (gateways != null) {
			Gateway gateway = gateways.getRemote();
			
			if (gateway != null) {
				if (gateway.getServer() != null && !gateway.getServer().equalsIgnoreCase("")) {
					syncButton.setDisable(false);
					syncFreeMenuItem.setDisable(false);
				}
			}
			
			Gateway[] pGateways = gateways.getLocal();
			
			if (pGateways != null && pGateways.length > 0) {
				if (pGateways[0] != null && pGateways[0].getServer() != null && !pGateways[0].getServer().equalsIgnoreCase("")) {
					//syncPersonalMenuItem.setDisable(false);
				}
			}
		}
		
		//setup sort button depending on current sort
		/*
		barButtons = toolBar.getItems();
		
		if (settings.getGeneral().isSortMRU()) {
			barButtons.remove(sortAZButton);
		} else {
			barButtons.remove(sortMOAButton);
		}
		*/
		
		//sort Menu Button
		sortMenuButton.setTooltip(new Tooltip("Sort Accounts Alphabetically (A-Z), or\nby Most often Accessed (MOA)"));
		
		menuBar.setFocusTraversable(true);
	}
	

	
	@FXML
	private void createPressed() {
		Account account = passvault.createAccount();
		
		if (account != null) {
			logger.fine("Account " + account.getName() + " returned");
			MRUComparator.getInstance().accountAdded(account.getName());
			logger.fine("Account added to access map");
			passvault.getDatabase().saveAccount(account);
			logger.fine("Account saved to database");
			passvault.getAccounts().add(account);
			logger.fine("Account added to List");
			passvault.getAccounts().sort(MRUComparator.getInstance());
		} else {
			logger.fine("No account created");
		}
	}
	
	@FXML
	private void settingsPressed() {
		passvault.showSettings();
		
		// if sync free has changed make sure button reflects state
		Settings settings = passvault.getSettings();
		Gateways gateways = settings.getSync();
		syncButton.setDisable(true);
		syncFreeMenuItem.setDisable(true);
		//syncPersonalMenuItem.setDisable(true);
		
		if (gateways != null) {
			Gateway gateway = gateways.getRemote();
			
			if (gateway != null) {
				if (gateway.getServer() != null && !gateway.getServer().equalsIgnoreCase("")) {
					syncButton.setDisable(false);
					syncFreeMenuItem.setDisable(false);
				}
			}
			
			Gateway[] pGateways = gateways.getLocal();
			
			if (pGateways != null && pGateways.length > 0) {
				if (pGateways[0] != null && pGateways[0].getServer() != null && !pGateways[0].getServer().equalsIgnoreCase("")) {
					//syncPersonalMenuItem.setDisable(false);
				}
			}
		}

	}
	
	@FXML
	private void syncPressed() {
		// no null checks since the button was enabled
		Gateway free = passvault.getSettings().getSync().getRemote();
		Task<Object> task = Utils.createSyncTask(free, passvault);
		Utils.runSync(task, passvault);
		logger.info("Sync Complete");
	}
	
	@FXML
	private void helpPressed() {
		passvault.showHelp();
	}
	
	@FXML
	private void changeKeyPressed() {
		String newKey = passvault.getNewKey();
		
		try {
			newKey = AESEngine.finalizeKey(newKey, AESEngine.KEY_LENGTH_256);
		} catch (Exception e) {
			logger.warning("There was an error updating the encryption key: " + e.getMessage());
			Utils.showAlert(AlertType.ERROR, passvault.getPrimaryStage(), "Change Key Error", "Error updating Encryption Key", 
					"There was an error updating the encryption key: " + e.getMessage() +
					"\nThe key will not be updated.");
			e.printStackTrace();
			return;
		}
		
		Store store = passvault.getDatabase();
		logger.finest("Setting new encryption key");
		store.setEncryptionKey(newKey);
		store.saveAccounts(passvault.getAccounts());
		logger.info("Accounts saved with new Key");
		
		Settings settings = passvault.getSettings();
		
		if (settings.getGeneral().isSaveKey()) {
			logger.finest("Saving new key to settings");
			newKey = new String(store.encodeBytes(AESEngine.getInstance()
					.encryptString(passvault.getCOMMON_KEY(), newKey)));
			settings.getGeneral().setKey(newKey);
			store.saveSettings(settings);
			logger.info("New key saved to settings");
		}
	}
	
	/*
	@FXML
	private void sortPressed() {
		Settings settings = passvault.getSettings();
		General general = settings.getGeneral();
		
		if (general.isSortMRU()) {
			barButtons.remove(sortMOAButton);
			barButtons.add(1, sortAZButton);
			Collections.sort(passvault.getAccounts());
			general.setSortMRU(false);
			((FXCBLStore)passvault.getDatabase()).saveSettings(settings);
		} else {
			barButtons.remove(sortAZButton);
			barButtons.add(1, sortMOAButton);
			Collections.sort(passvault.getAccounts(), MRUComparator.getInstance());
			general.setSortMRU(true);
			((FXCBLStore)passvault.getDatabase()).saveSettings(settings);
		}
	}
	*/
	
	
	@FXML 
	private void sortMOAMenuItemPressed() {
		passvault.getSettings().getGeneral().setSortMRU(true);
		Collections.sort(passvault.getAccounts(), MRUComparator.getInstance());
		passvault.getDatabase().saveSettings(passvault.getSettings());
	}
	
	
	@FXML 
	private void sortAZMenuItemPressed() {
		passvault.getSettings().getGeneral().setSortMRU(false);
		Collections.sort(passvault.getAccounts());
		passvault.getDatabase().saveSettings(passvault.getSettings());
	}
	
	
	public void setPassvault(Passvault passvault) {
		this.passvault = passvault;
	}
	
	@FXML
	private void syncPersonlMenuItem() {
		// no null checks since the menu item was enabled
		Gateway personal = passvault.getSettings().getSync().getLocal()[0];
		Task<Object> task = Utils.createSyncTask(personal, passvault);
		Utils.runSync(task, passvault);
		logger.info("Sync Complete");
	}
	
	@FXML
	private void exitMenuItem() {
		// TODO - look into if CBL should be shutdown first since it keeps non deamon threads once sync is done
		passvault.getPrimaryStage().close();
		System.exit(0);
	}
	
	@FXML
	private void editMenuItem() {
		passvault.getListDetailsController().editButtonPressed();
	}
	
	@FXML
	private void deleteMenuItem() {
		passvault.getListDetailsController().deleteButtonPressed();
	}
	
	@FXML
	private void aboutMenuItem() {
		passvault.showAbout();
	}
	
	@FXML
	private void copyPassMenuItem() {
		passvault.getListDetailsController().copyButtonPressed();
	}
	
	@FXML
	private void copyOldPassMenuItem() {
		passvault.getListDetailsController().copyOldButtonPressed();
	}
	
	@FXML
	private void launchBrowserMenuItem() {
		Utils.launchBrowser(passvault.getListDetailsController().getCurrentAccount().getUrl());
		//passvault.getListDetailsController().
	}
	
	
	public void setAccountDetailsShowingHandler() {
		accountDetailsShowing = new AccountDetailsShowing() {
			@Override
			public void isShowing(boolean showing) {
				if (showing) {
					//System.out.println("Enabling edit/delete menu items");
					deleteMenuItem.setDisable(false);
					Account account = passvault.getListDetailsController().getCurrentAccount();
					
					if (account.isValidEncryption()) {
						copyMenuItem.setDisable(false);
						copyOldMenuItem.setDisable(false);
						editMenuItem.setDisable(false);
					
						if (account.getUrl() != null && !account.getUrl().equalsIgnoreCase("http://"))
							launchBrowserMenuItem.setDisable(false);
					}
					
				} else {
					//System.out.println("Disabling edit/delete menu items");
					editMenuItem.setDisable(true);
					deleteMenuItem.setDisable(true);
					copyMenuItem.setDisable(true);
					copyOldMenuItem.setDisable(true);
					launchBrowserMenuItem.setDisable(true);
				}
				
			}
		};
		
		passvault.getListDetailsController().setAccountDetailsShowing(accountDetailsShowing);
	}
	
	
}
