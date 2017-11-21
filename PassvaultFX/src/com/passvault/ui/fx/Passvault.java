package com.passvault.ui.fx;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.passvault.crypto.AESEngine;
import com.passvault.util.data.file.JsonStore;
import com.passvault.util.data.file.model.Settings;
import com.passvault.ui.fx.utils.FXCBLStore;
import com.passvault.ui.fx.utils.SettingsUpdated;
import com.passvault.ui.fx.utils.TabChangedHandler;
import com.passvault.ui.fx.view.AboutLayoutController;
import com.passvault.ui.fx.view.AccountKeyLayoutController;
import com.passvault.ui.fx.view.CreateAccountLayoutController;
import com.passvault.ui.fx.view.EditAccountLayoutController;
import com.passvault.ui.fx.view.GeneratorOptionsLayoutController;
import com.passvault.ui.fx.view.GetNewKeyLayoutController;
import com.passvault.ui.fx.view.ListDetailsLayoutController;
import com.passvault.ui.fx.view.LoginLayoutController;
import com.passvault.ui.fx.view.ProgressLayoutController;
import com.passvault.ui.fx.view.RootLayoutController;
import com.passvault.ui.fx.view.SettingsLayoutController;
import com.passvault.ui.fx.view.WebviewLayoutController;
import com.passvault.util.Account;
import com.passvault.util.AccountUUIDResolver;
import com.passvault.util.MRUComparator;
import com.passvault.util.RandomPasswordGenerator;
import com.passvault.util.Utils;
import com.passvault.util.data.Store;
import com.passvault.util.data.couchbase.CBLStore;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Passvault extends Application {
	
	
	private Stage primaryStage;
	private MenuBar menuBar;
	private ToolBar toolBar;
	private AnchorPane listDetailsLayout;
	private BorderPane rootLayout;
	private SplitPane.Divider divider;
	private RootLayoutController controller;
	
	private ListDetailsLayoutController listDetailsController;
	private ObservableList<Account> accounts;
	//private FXCBLStore cblStore;
	private Store store;
	private static Settings settings;
	
	private static Logger logger;
	private static String COMMON_KEY;
	public static final String HELP_URL = "https://github.com/ErikDeveloperNot/PassvaultFX/wiki";
	
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx");
		
		try {
			COMMON_KEY = AESEngine.finalizeKey("hSDFnv9q>/z_", AESEngine.KEY_LENGTH_256);
		} catch (Exception e) {
			logger.warning("Error finalizing key: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	@Override
	public void start(Stage primaryStage) {
		logger.finest("Passvault is starting up...");
		this.primaryStage = primaryStage;
		initialize();
	}
	
	
	private void initialize() {
		
		try {
			// bad design leads to bad hack, reset below with the real one before accounts are loaded
	        Utils.setAccountUUIDResolver(new AccountUUIDResolver() {
				@Override
				public String getAccountUUID() {
					return "";
				}
			});
	        
	        //cblStore = new FXCBLStore("pass_vault", CBLStore.DatabaseFormat.SQLite, "");
	        store = new JsonStore();
	        //settings = cblStore.loadSettings();
	        settings = store.loadSettings();
	        
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/RootLayout.fxml"));
	        rootLayout = (BorderPane) loader.load();
	        controller = loader.getController();
	        controller.setPassvault(this);
	        
	        Scene scene = new Scene(rootLayout);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Passvault");
	        primaryStage.getIcons().add(new Image("file:resources/images/vault-mpi.png"));
	        
	        BorderPane innerBorderPane = (BorderPane)rootLayout.getChildren().get(1);
	        toolBar = (ToolBar) innerBorderPane.getChildren().get(0);
	        
	        FXMLLoader loader2 = new FXMLLoader();
	        loader2.setLocation(Passvault.class.getResource("view/ListDetailsLayout.fxml"));
	        listDetailsLayout = (AnchorPane) loader2.load();
	        divider = ((SplitPane)listDetailsLayout.getChildren().get(0)).getDividers().get(0);
	        innerBorderPane.setCenter(listDetailsLayout);
	        
	        listDetailsController = loader2.getController();
	        listDetailsController.setPassvaultApp(this, toolBar);
	        
	        scene.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					listDetailsController.setPane();
					//System.out.println(scene.getWidth() + " x " + scene.getHeight());
				}
            	});
	        
	        controller.setAccountDetailsShowingHandler();
	        String key = null;
	        
	        if (settings.getGeneral().isSaveKey()) {
	        		key = settings.getGeneral().getKey();
	        		key = AESEngine.getInstance().decryptBytes(COMMON_KEY, Utils.decodeString(key));
	        } else {
	           do {
		        		key = getLoginKey();
		        } while (key == null || key.equalsIgnoreCase(""));
		        	
		        key = AESEngine.finalizeKey(key, AESEngine.KEY_LENGTH_256);
	        }
	        
	        //cblStore.setEncryptionKey(key);
	        store.setEncryptionKey(key);
	        
	        //key = AESEngine.finalizeKey(key, AESEngine.KEY_LENGTH_256);
	        //cblStore = new FXCBLStore("pass_vault", CBLStore.DatabaseFormat.SQLite, key);
	        //settings = cblStore.loadSettings();
	        Utils.setAccountUUIDResolver(com.passvault.ui.fx.utils.Utils.getAccountUUIDResolver());
	        //cblStore.updatateAccountUUID(com.passvault.ui.fx.utils.Utils.getAccountUUIDResolver().getAccountUUID());
	        store.updateAccountUUID(null, com.passvault.ui.fx.utils.Utils.getAccountUUIDResolver().getAccountUUID());
	        accounts = FXCollections.observableArrayList();
	        //new MRUComparator(cblStore).setReverse(false);
	        new MRUComparator(store).setReverse(false);
	        logger.fine("Loading accounts from database");
	        //cblStore.loadAccounts(accounts);
	        store.loadAccounts(accounts);
	        logger.fine(accounts.size() + " accounts were loaded");
	        
	        // check if purge, if so do it
	        if (settings.getDatabase().isPurge()) {
	        		//cblStore.purgeDeletes();
	        		store.purgeDeletes();
	        }
	        
	        // debug any conflicts, shouldn't be any
	        //cblStore.printConflicts();
	        store.printConflicts();    //does nothing but keep it in case it is used later
	        
	        com.passvault.ui.fx.utils.Utils.sortAccounts(accounts, settings);
	   
	        //for testing
	        //listDetailsController.PopulateListTest();

	        listDetailsController.setAccountsList(accounts);
	        listDetailsController.setPane();
	        primaryStage.show();
		} catch (Exception e) {
			logger.severe("Error in initialization: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public AnchorPane getListDetailPane() {
		return listDetailsLayout;
	}
	
	
	public SplitPane.Divider getListDetailDivider() {
		return divider;
	}
	
	
	public ListDetailsLayoutController getListDetailsController() {
		return listDetailsController;
	}
	
	
	/*
	 * Get Account Key
	 */
	private String getLoginKey() {
		
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/LoginLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Passvault Key");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        LoginLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        
	        dialogStage.showAndWait();
	        return controller.getPassword();
        
		} catch (IOException e) {
			logger.warning("Error obtaining encryption key: " + e.getMessage());
			e.printStackTrace();
		}
 
		return null;
	}
	
	
	public String getOldKey() {
		
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/AccountKeyLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Acoount Old Key");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        AccountKeyLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        
	        dialogStage.showAndWait();
	        return controller.getPassword();
        
		} catch (IOException e) {
			logger.warning("Error obtaining old encryption key: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public String getNewKey() {
		
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/GetNewKeyLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Acoount New Key");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        GetNewKeyLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        
	        dialogStage.showAndWait();
	        return controller.getPassword();
        
		} catch (IOException e) {
			logger.warning("Error obtaining new encryption key: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public boolean editAccount(Account account) {
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/EditAccountLayout.fxml"));
	        VBox page = (VBox) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit " + account.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        EditAccountLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        controller.setAccount(account);
	        controller.setPassvault(this);
	        
	        dialogStage.showAndWait();
	        return controller.accountSaved();
        
		} catch (IOException e) {
			logger.warning("Error editing account: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	public Account createAccount() {
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/CreateAccountLayout.fxml"));
	        VBox page = (VBox) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Create Account");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        CreateAccountLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        controller.setPassvault(this);
	        
	        dialogStage.showAndWait();
	        
	        if (controller.accountCreated())
	        		return controller.getAccount();
	        else
	        		return null;
        
		} catch (IOException e) {
			logger.warning("Error creating account: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public void showHelp() {
		showWebview("Passvault Help", HELP_URL);
	}
	
	
	public void showWebview(String title, String url) {
		
		if (!com.passvault.ui.fx.utils.Utils.launchBrowser(url)) {
			try {
				FXMLLoader loader = new FXMLLoader();
		        loader.setLocation(Passvault.class.getResource("view/WebviewLayout.fxml"));
		        AnchorPane page = (AnchorPane) loader.load();
		        
		        Stage dialogStage = new Stage();
	            dialogStage.setTitle(title);
	            dialogStage.initModality(Modality.WINDOW_MODAL);
	            dialogStage.initOwner(primaryStage);
	            Scene scene = new Scene(page);
	            dialogStage.setScene(scene);
		        WebviewLayoutController controller = loader.getController();
		        controller.showURL(url);
		        dialogStage.showAndWait();
		        
			} catch (IOException e) {
				logger.warning("Error showing " + title + " window: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	public void showAbout() {
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/AboutLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("About Passvault");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        AboutLayoutController controller = loader.getController();
	        controller.setStageAndPassvault(dialogStage, this);
	        
	        dialogStage.showAndWait();
	        
		} catch (IOException e) {
			logger.warning("Error showing about window: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public void showProgress(String title, Stage owner, Task<Object> task) {
		
		if (owner == null)
			owner = primaryStage;
		
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/ProgressLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
        
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(owner);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        ProgressLayoutController controller = loader.getController();
	        controller.setTitle(title);
	        controller.setStage(dialogStage);
	        controller.setTask(task);
	        
	        dialogStage.setAlwaysOnTop(true);
	        dialogStage.setResizable(false);
	        
	        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		        	@Override
					public void handle(WindowEvent event) {
						event.consume();
					}
			});
	        
	        dialogStage.showAndWait();
        
		} catch (IOException e) {
			logger.warning("Error running progress wind for " + title + ", error: " + e.getMessage());
			e.printStackTrace();
		}
		
		//return null;
	}
	
	
	public RandomPasswordGenerator getCustomGenerator() {
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/GeneratorOptionsLayout.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();
	        
	        Stage dialogStage = new Stage();
            dialogStage.setTitle("Password Generator Options");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        GeneratorOptionsLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        
	        dialogStage.showAndWait();
	        
	        if (controller.useCustomGenerator()) {
	        		logger.info("Returning custom password generator");
	        		return controller.getGenerator();
	        }
        
		} catch (IOException e) {
			logger.warning("Error setting up a custom password generator: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	 *  as tabs are cycled through each previous tab will be checked, report any errors.
	 *  the current tab will be kept and on window closed its state will be checked for errors
	 *  if no errors the Settings object will be persisted and any call backs registered will be 
	 *  called.
	 */
	public void showSettings() {
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Passvault.class.getResource("view/SettingsLayout.fxml"));
	        TabPane page = (TabPane) loader.load();
	        Stage dialogStage = new Stage();
	        
			page.setOnKeyPressed((KeyEvent e) -> {
					if (e.getCode() == KeyCode.ESCAPE)
						dialogStage.close();
			});
       
	        TabChangedHandler handler = new TabChangedHandler();
	        GeneratorOptionsLayoutController optionsController = null;
	       
	        for (Tab tab : page.getTabs()) {
				//System.out.println(tab.getText());
				tab.setOnSelectionChanged(handler);
				
				if (tab.getText().equalsIgnoreCase(TabChangedHandler.GENERATOR)) {
					FXMLLoader loader2 = new FXMLLoader();
			        loader2.setLocation(Passvault.class.getResource("view/GeneratorOptionsLayout.fxml"));
			        AnchorPane page2 = (AnchorPane) loader2.load();
			        page2.setStyle("-fx-border-width: 0 0 0 0;");
			        page2.setMaxWidth(Double.MAX_VALUE);
			        page2.setPrefWidth(392.0);
			        
			        optionsController = loader2.getController();
			        List<Node> nodesToRemove = new ArrayList<>();
			        
			        for (Node node : page2.getChildren()) {
						
						String id = node.getId();
						
						if (id != null) {
							if (id.contains("Button")) {
								// Remove save/cancel buttons from settings
								nodesToRemove.add(node);
							} else if (id.contains("lengthTextField")) {
								// add code to make tabPane get focus after last node
								node.setOnKeyPressed((KeyEvent e) -> {
									if (e.getCode() == KeyCode.TAB)  {
										page.requestFocus();
										e.consume();
									} 
								});
							}
						}
					}
			        
			        for (Node node : nodesToRemove) {
			        		page2.getChildren().remove(node);
					}
			        
			        tab.setContent(page2);
				}
			}
	        
	        //end testing
	        
	        dialogStage.setTitle("Settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
	        
	        SettingsLayoutController controller = loader.getController();
	        controller.setStage(dialogStage);
	        controller.setPassvault(this);
	        controller.setOptionsController(optionsController);
	        controller.setTabChangedHandler(handler);
	        dialogStage.showAndWait();
	        
	        // after window is dismissed run check on the last tab accessed and then save Settings to database
	        controller.saveTab(controller.getLastTab());
	        logger.info("Saving settings to database");
	        //cblStore.saveSettings(settings);
	        store.saveSettings(settings);
	        
	        // finally run through update list and run any needed actions
	        for (SettingsUpdated update : controller.getSettingsUpdatedList()) {
				update.updated();
			}
        
		} catch (IOException e) {
			logger.warning("Error setting up Settings Window: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public Store getDatabase() { //CBLStore getDatabase() {
		return store;
	}
	
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	
	public List<Account> getAccounts() {
		return accounts;
	}
	
	
	public static Settings getSettings() {
		return settings;
	}

	
	public static void main(String[] args) {
		launch(args);
	}


	public static String getCOMMON_KEY() {
		return COMMON_KEY;
	}
}
