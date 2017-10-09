package com.passvault.ui.fx.view;

import com.passvault.ui.fx.Passvault;
import com.passvault.ui.fx.utils.Utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

public class AboutLayoutController {

	@FXML
	Button dismissButton;
	@FXML
	Hyperlink hyperLink;
	
	private Stage stage;
	private Passvault passvault;
	private static final String ICON_LINK = "https://www.flaticon.com/authors/dave-gandy";
	
	
	public AboutLayoutController() {
		// TODO Auto-generated constructor stub
	}
	
	
	@FXML
	private void initialize() {
		hyperLink.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				if (!Utils.launchBrowser(ICON_LINK)) {
					passvault.showWebview("Icons Credit", ICON_LINK);
				}
			}
		});
		
		dismissButton.setOnKeyPressed((Utils.getEnterKeyHandler(() -> dismissPressed())));
		
		hyperLink.setOnKeyPressed((Utils.getEnterKeyHandler(() ->  {
				if (!Utils.launchBrowser(ICON_LINK)) {
					passvault.showWebview("Icons Credit", ICON_LINK);
				}
			}))); 
	}
	
	
	public void setStageAndPassvault(Stage stage, Passvault passvault) {
		this.stage = stage;
		this.passvault = passvault;
	}
	
	@FXML
	private void dismissPressed() {
		stage.close();
	}
}
