package com.passvault.ui.fx.utils;

import java.util.Collections;
import java.util.logging.Logger;

import com.passvault.ui.fx.Passvault;
import com.passvault.ui.fx.model.Settings;
import com.passvault.util.MRUComparator;

public class GeneralSettingsUpdated implements SettingsUpdated {

	private Passvault passvault;
	
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.utils");
	}
	
	public GeneralSettingsUpdated(Passvault passvault) {
		this.passvault = passvault;
	}
	
	@Override
	public void updated() {
		// nothing to do for purge, happens at startup
		
		//sortMRU needs to sort Account List
		Utils.sortAccounts(passvault.getAccounts(), passvault.getSettings());
		
		//save Key needs to save/delete key, nothing to do
		
	}

}
