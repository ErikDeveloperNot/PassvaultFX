package com.passvault.ui.fx.modelToRemove;

import java.util.Map;

public class Database {

	private boolean purge;
	
	public Database() {
		purge = false;
	}
	
	public Database(Map<String, Object> values) {
		this.purge = (Boolean)values.get("purge");
	}

	public boolean isPurge() {
		return purge;
	}

	public void setPurge(boolean purge) {
		this.purge = purge;
	}

	@Override
	public String toString() {
		return "{\n purge: " + purge + "\n}";
	}
	
	
}
