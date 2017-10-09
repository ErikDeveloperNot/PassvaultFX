package com.passvault.ui.fx.view;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

public class WebviewLayoutController {

	@FXML
	WebView webView;
	
	public WebviewLayoutController() {
		
	}
	
	@FXML
	private void initialize() {
		//webView.getEngine().load(Passvault.HELP_URL);
	}
	
	public void showURL(String url) {
		webView.getEngine().load(url);
	}
}
