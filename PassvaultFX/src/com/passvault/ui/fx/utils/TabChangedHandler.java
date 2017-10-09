package com.passvault.ui.fx.utils;

import java.util.logging.Logger;

import com.passvault.ui.fx.view.SettingsLayoutController;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;

public class TabChangedHandler implements EventHandler<Event> {

	private SettingsLayoutController controller;
	private String currentTab;
	
	public static final String GENERAL = "General";
	public static final String GENERATOR = "Generator";
	public static final String SYNC_FREE = "Sync Free";
	public static final String SYNC_PERSONAL = "Sync Personal";
	
	private static final Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.utils");
	}
	
	
	public TabChangedHandler() {
		currentTab = GENERAL;
	}
	
	@Override
	public void handle(Event event) {
		//System.out.println("tab event: " + event.getTarget() + ", " + event.getSource());
		//Tab source = (Tab)event.getSource();
		//System.out.println("  tab: " + source.getText());
		
		String sourceTab = ((Tab)(event.getSource())).getText();
		logger.fine("Source tab: " + sourceTab + ", current tab: " + currentTab);
		
		// tab changed, run checks on previous tab
		if (sourceTab.equalsIgnoreCase(currentTab)) {
			controller.saveTab(currentTab);
		}
		
		currentTab = sourceTab;
	}
	
	public void setController(SettingsLayoutController controller) {
		this.controller = controller;
	}

	public String getCurrentTab() {
		return currentTab;
	}
}
