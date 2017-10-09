package com.passvault.ui.fx.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.passvault.ui.fx.model.Database;
import com.passvault.ui.fx.model.General;
import com.passvault.ui.fx.model.Generator;
import com.passvault.ui.fx.model.Settings;
import com.passvault.util.couchbase.CBLStore;
import com.passvault.util.model.Gateways;

public class FXCBLStore extends CBLStore {
	
	public static String SETTINGS_DTYPE = "settings";
	private static Logger logger;
	
	static {
		logger = Logger.getLogger("com.passvault.ui.fx.utils");
	}

	public FXCBLStore() {
		super();
	}

	public FXCBLStore(String databaseName, DatabaseFormat dbFormat, String key) {
		super(databaseName, dbFormat, key);
	}
	
	public void saveSettings(Settings settings) {
		logger.info("Saving Settings");
		final Map saveDoc = new HashMap<>();
		saveDoc.put("docType", SETTINGS_DTYPE);
		saveDoc.put("general", settings.getGeneral());
		saveDoc.put("generator", settings.getGenerator());
		saveDoc.put("database", settings.getDatabase());
		saveDoc.put("sync", settings.getSync());
		Document mapDoc = database.getDocument("__" + SETTINGS_DTYPE);
		
		try {
		if (mapDoc.getCurrentRevisionId() != null) {
			mapDoc.update(new Document.DocumentUpdater() {
				
				@Override
				public boolean update(UnsavedRevision arg0) {
					arg0.setUserProperties(saveDoc);
					return true;
				}
			});
		} else {
			mapDoc.putProperties(saveDoc);
		}
		} catch(CouchbaseLiteException e) {
			logger.warning("Error saving Settings: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Settings loadSettings() {
		Settings settings = null;
		
		logger.info("Loading Settings");
		Query query = database.createAllDocumentsQuery();
		//Collection toReturn = null;
		
		try {
			QueryEnumerator enumerator = query.run();
			QueryRow queryRow = null;
			
			while ((queryRow = enumerator.next()) != null) {
				Document document = queryRow.getDocument();
				
				if (document.getProperty("docType") != null && document.getProperty("docType").equals(SETTINGS_DTYPE)) {
					logger.fine("Retrieved Settings from database");
					settings = new Settings();
					
					General general = new General((Map)document.getProperty("general"));
					settings.setGeneral(general);
					
					Generator generator = new Generator((Map)document.getProperty("generator"));
					settings.setGenerator(generator);
					
					Database database = new Database((Map)document.getProperty("database"));
					settings.setDatabase(database);
					
					Gateways gateways = new Gateways((Map)document.getProperty("sync"));
					settings.setSync(gateways);
				}
			}
			
			if (settings == null) {
				settings = new Settings();
				settings.resetDefaults();
			}
			
		} catch (CouchbaseLiteException e) {
			logger.warning("Error loading Settings: " + e.getMessage());
			e.printStackTrace();
		}
		
		return settings;
	}
	
	
	public void updatateAccountUUID(String uuid) {
		accountUUID = uuid;
	}
	
	public String getKey() {
		return encryptionKey;
	}
	
}
