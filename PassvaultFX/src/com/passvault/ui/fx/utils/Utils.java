package com.passvault.ui.fx.utils;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.passvault.ui.fx.Passvault;
import com.passvault.ui.fx.model.Settings;
import com.passvault.util.Account;
import com.passvault.util.AccountUUIDResolver;
import com.passvault.util.MRUComparator;
import com.passvault.util.couchbase.AccountsChanged;
import com.passvault.util.couchbase.CBLStore;
import com.passvault.util.couchbase.SyncGatewayClient.ReplicationStatus;
import com.passvault.util.model.Gateway;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/* 
 * Static helper methods
 */
public class Utils {
	
	public static final String ACCOUNTS_UPDATED = "updated";
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.utils");
	}

	
	public static AccountUUIDResolver getAccountUUIDResolver() {
		return new AccountUUIDResolver() {
			
			@Override
			public String getAccountUUID() {
				String toReturn = Passvault.getSettings().getGeneral().getAccountUUID();
				return (toReturn == null) ? "" : toReturn;
			}
		};
	}
	
	
	public static EventHandler<KeyEvent> getEnterKeyHandler(EnterPressed enterPressed) {
		return (new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				
				if (event.getCode() == KeyCode.ENTER) {
					enterPressed.process();
				}
			}
		});
	}
	
	
	public static boolean launchBrowser(String url) {
		
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			
			if (desktop.isSupported(Action.BROWSE)) {
				try {
					desktop.browse(new URI(url));
					return true;
				} catch (Exception e) {
					logger.warning("Error opening browser to: " + url + "\n" + e.getMessage());
					e.printStackTrace();
				}
			} else {
				logger.warning("Browser open action not supported");
			}
		} else {
			logger.warning("Browser launch not supported on this platform");
		}
		
		return false;
	}
	
	
	public static void copyPassword(String password) {
		Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(password);
		clipBoard.setContents(stringSelection, stringSelection);
	}
	
	
	public static void showAlert(AlertType type, Stage stage, String title, String header, String content) {
		Alert alert = new Alert(type);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(Utils.class.getResource("../view/passvault-default.css").toExternalForm());
		dialogPane.getStyleClass().add("BorderPane");
		alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
	}
	
	
	public static void runSync(Task<Object> task, Passvault passvault) {
		String error = "";
		passvault.showProgress("Syncing Accounts", null, task);
		
		ReplicationStatus status = (ReplicationStatus)task.getValue();
		
		if (task.getMessage().equalsIgnoreCase(Utils.ACCOUNTS_UPDATED)) {
			logger.info("Accounts Updated");
			List<Account> accounts = passvault.getAccounts();
			accounts.clear();
			passvault.getDatabase().loadAccounts(accounts);
			sortAccounts(accounts, passvault.getSettings());
		}
		
		if (status.getPullError() != null) {
			error = "Pull Errors: " + status.getPullError().getMessage() + "\n";
			status.getPullError().printStackTrace();
		}
		
		if (status.getPushError() != null) {
			error += "Push Errors: " + status.getPushError().getMessage();
			status.getPushError().printStackTrace();
		}
		
		if (!error.equalsIgnoreCase("")) {
			Utils.showAlert(AlertType.ERROR, passvault.getPrimaryStage(), "Sync Error", "Synchronize Accounts Error"
					, error);
			logger.warning("Synchronize Accounts Error: " + error);
		} else {
			Utils.showAlert(AlertType.CONFIRMATION, passvault.getPrimaryStage(), "Sync Complete", "Synchronize Finished"
					, "Accounts up to date");
			logger.info("Synchronize Finished, Accounts up to date");
		}
	}
	
	
	public static Task<Object> createSyncTask(Gateway gateway, Passvault passvault) {
		
		return new Task<Object>() {
			// assume server, port, proto, db/bucket are in correct format
			String user = gateway.getUserName();
			String password = gateway.getPassword();
			ReplicationStatus status = null;
			
			AccountsChanged handler = new AccountsChanged() {
				@Override
				public void onAccountsChanged() {
					updateMessage(ACCOUNTS_UPDATED);
				}
			};
			
			@Override
			protected Object call() throws Exception {
				Object toReturn;
				
				if (user != null && !user.equalsIgnoreCase("")) {
					if (password == null)
						password = "";
		
					status = passvault.getDatabase().syncAccounts(gateway.getServer(), 
																  gateway.getProtocol(), 
																  gateway.getPort(), 
																  gateway.getBucket(), 
																  user, 
																  password, 
																  handler);
				} else {
					status = passvault.getDatabase().syncAccounts(gateway.getServer(), 
							  gateway.getProtocol(), 
							  gateway.getPort(), 
							  gateway.getBucket(), 
							  handler);
				}
				
				while(status.isRunning()) {
					logger.finest(">>>>>>>>>>>>>>>>Still Running<<<<<<<<<<<<<<<<");
					Thread.sleep(200L);
				}

				logger.finest(">>>>>>>>>>>>>>>>Done<<<<<<<<<<<<<<<<");
				return status;
			}
		};
	}
	
	
	public static void sortAccounts(List<Account> accounts, Settings settings) {
		if (settings.getGeneral().isSortMRU())
    			Collections.sort(accounts, MRUComparator.getInstance());
		else
    			Collections.sort(accounts);
	}
	
	
	public static Settings getSettings(CBLStore store) {
		
		return null;
	}
	
	
	public static void saveSettings(CBLStore store, Settings settings) {
		
	}
}