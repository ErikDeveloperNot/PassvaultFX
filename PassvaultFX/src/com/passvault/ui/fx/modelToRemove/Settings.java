package com.passvault.ui.fx.modelToRemove;

import java.util.List;

import com.passvault.ui.fx.utils.FXCBLStore;
import com.passvault.util.DefaultRandomPasswordGenerator;
import com.passvault.model.Gateways;

public class Settings {

	//private String docType;
	private General general;
	private Generator generator;
	private Database database;
	private Gateways sync;
	
	public General getGeneral() {
		return general;
	}
	public void setGeneral(General general) {
		this.general = general;
	}
	public Generator getGenerator() {
		return generator;
	}
	public void setGenerator(Generator generator) {
		this.generator = generator;
	}
	public Database getDatabase() {
		return database;
	}
	public void setDatabase(Database database) {
		this.database = database;
	}
	public Gateways getSync() {
		return sync;
	}
	public void setSync(Gateways sync) {
		this.sync = sync;
	}
	/*public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}*/
	
	public void resetDefaults() {
		general = new General();
		generator = new Generator();
		database = new Database();
		sync = new Gateways();
	}
	@Override
	public String toString() {
		return "System Settings:\n General:\n" + general + "\n Generator:\n" + generator + "\n Database:\n" + 
				database + "\n Sync:\n" + sync;
	}
	
	
	public DefaultRandomPasswordGenerator getDefaultRandomPasswordGenerator() {
		DefaultRandomPasswordGenerator rpg = new DefaultRandomPasswordGenerator();
		
		if (generator != null) {
			Properties p = generator.getProperties();
			if (p != null) {
				rpg.clearAllowedCharacters();
				rpg.setAllowedCharacters(p.getAllowedCharacters());
				rpg.setLength(p.getLength());
				
				List<Character> chars = rpg.getAllowedCharactres();
				
				//check for lower
				boolean allLower = true;
				for (char character : rpg.getDefaultLower()) {
					if (!chars.contains(character)) {
						allLower = false;
						break;
					}
				}
				
				//check for Upper
				boolean allUpper = true;
				for (char character : rpg.getDefaultUpper()) {
					if (!chars.contains(character)) {
						allUpper = false;
						break;
					}
				}
				
				//check for digits
				boolean allDigits = true;
				for (char character : rpg.getDefaultDigits()) {
					if (!chars.contains(character)) {
						allDigits = false;
						break;
					}
				}
				
				//check for special
				boolean allSpecial = true;
				for (char character : rpg.getDefaultSpecial()) {
					if (!chars.contains(character)) {
						allSpecial = false;
						break;
					}
				}
				
				rpg.setCheckSpecial(allSpecial);
				rpg.setCheckDigits(allDigits);
				rpg.setCheckLower(allLower);
				rpg.setCheckUpper(allUpper);
			}
		}
		
		return rpg;
	}
	
	
}
