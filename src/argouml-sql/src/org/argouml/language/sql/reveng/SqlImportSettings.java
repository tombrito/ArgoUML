/* $Id$
 *******************************************************************************
 * Copyright (c) 2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent BRAUD
 *******************************************************************************
 */

package org.argouml.language.sql.reveng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.argouml.configuration.Configuration;
import org.argouml.configuration.ConfigurationKey;
import org.argouml.i18n.Translator;
import org.argouml.uml.reveng.Setting;
import org.argouml.uml.reveng.SettingsTypes;

/**
 * @author BRAUD
 */
public class SqlImportSettings {
	
	private static final ConfigurationKey KEY_IMPORT_EXTENDED_SGBD_NAME = Configuration
			.makeKey("import", "extended", "sql", "sgbd", "name");
	
	private static final ConfigurationKey KEY_IMPORT_EXTENDED_MODELE_LEVEL = Configuration
			.makeKey("import", "extended", "sql", "model", "level");
	
	private static final String DEFAULT_SGBD = "MySql";
	
	public static final String LEVEL_MCD = "MCD";
	
	public static final String LEVEL_MPD = "MPD";
	
    private static SqlImportSettings theInstance;
    
    private List<SettingsTypes.Setting> settingsList;

    /**
     * SGBD Source of the dump
     */
    private SettingsTypes.UniqueSelection sgbdSetting;

    /**
     * Level (Conception, Physique)
     */
    private SettingsTypes.UniqueSelection levelSetting;
    /**
     * Gets the singleton instance.
     * 
     * @return the instance
     */
    public static synchronized SqlImportSettings getInstance() {
        if (theInstance == null) {
            theInstance = new SqlImportSettings();
        }
        return theInstance;
    }

    /**
     * Constructor
     * private for Singleton
     */
	private SqlImportSettings() {
		
	}
	
	private Map<String, String> createListSupportedDump() {
		Map<String,String> listSupportedDump = new HashMap<String, String>();
		addLabelFromParserName(listSupportedDump, DEFAULT_SGBD);
		addLabelFromParserName(listSupportedDump, "SqlServer");
		
		return listSupportedDump;
	}
	private void addLabelFromParserName(Map<String,String> lst,String parserName) {
		String label = Translator.localize("argouml-sql.import.database." + parserName);
		lst.put(parserName, label);
	}

    /*
     * Provides the implementation of
     * org.argouml.uml.reveng.ImportInterface#getImportSettings() for
     * implementors of ImportInterface.
     * 
     * @return the list of import settings
     */
    public List<SettingsTypes.Setting> getImportSettings() {

    	// TODO : is it ok with that.
    	// if yes, see javaImport code and change it
		if (settingsList == null) {
    		settingsList = new ArrayList<SettingsTypes.Setting>();
            createSgbdSetting();
            settingsList.add(sgbdSetting);
    		createLevelSetting();
    		settingsList.add(levelSetting);
    	}
        
        return settingsList;
    }

	private void createSgbdSetting() {
		Map<String, String> listSupportedDump = createListSupportedDump();
        List<String> lstCle = new ArrayList<String>(listSupportedDump.keySet());
		List<String> lstVal = new ArrayList<String>(listSupportedDump.values());
		String val = Configuration.getString(KEY_IMPORT_EXTENDED_SGBD_NAME);
		int indexSelectedSgbd;
		if (val.length() == 0) {
			indexSelectedSgbd = lstCle.indexOf(DEFAULT_SGBD);	
		} else {
			indexSelectedSgbd = new Integer(val);
		}
		
		String label = Translator.localize("argouml-sql.import.listdatabase");
		sgbdSetting = new Setting.UniqueSelection(label, lstVal, indexSelectedSgbd);
	}
	
	public String getCodeSgbd() {
		String ret = null;
		
		Map<String, String> listSupportedDump = createListSupportedDump();
		List<String> lstCle = new ArrayList<String>(listSupportedDump.keySet());
		
		String val = Configuration.getString(KEY_IMPORT_EXTENDED_SGBD_NAME);
		
		Integer indexSelectedSgbd;
		if (val.length() == 0) {
			indexSelectedSgbd = lstCle.indexOf(DEFAULT_SGBD);	
		} else {
			indexSelectedSgbd = new Integer(val);
		}
		
		ret = lstCle.get(indexSelectedSgbd);
		
		return ret;
	}
	
	public SettingsTypes.UniqueSelection getSgbdSetting() {
		return sgbdSetting;
	}
    
	private void createLevelSetting() {
		String val = Configuration.getString(KEY_IMPORT_EXTENDED_MODELE_LEVEL, "0");
		int indexSelectedLevel = new Integer(val);
		
		String label = Translator.localize("argouml-sql.import.level");
		levelSetting = new Setting.UniqueSelection(label, getLstLevel(), indexSelectedLevel);
	}
	
	public SettingsTypes.UniqueSelection getLevelSetting() {
		return levelSetting;
	}
	
	private List<String> getLstLevel() {
		List<String> listLevel = new ArrayList<String>();
		addLabelFromModele(listLevel, LEVEL_MPD);
		addLabelFromModele(listLevel, LEVEL_MCD);
		
		return listLevel;
	}
	
	public String getCodeLevel() {
		String ret = LEVEL_MPD;
		
		String val = Configuration.getString(KEY_IMPORT_EXTENDED_MODELE_LEVEL, "0");
		if (val.equals("0")) {
			ret = LEVEL_MPD;
		} else if (val.equals("1")) {
			ret = LEVEL_MCD;
		}
		
		return ret;
	}
	
	private void addLabelFromModele(List<String> lst,String parserName) {
		String label = Translator.localize("argouml-sql.import.level." + parserName);
		lst.add(label);
	}
    /**
     * Saves the settings in the configuration.
     */
    public void saveSettings() {
		if (sgbdSetting != null) {
			Configuration.setString(KEY_IMPORT_EXTENDED_SGBD_NAME, String.valueOf(sgbdSetting.getSelection()));
		}
		
		if (levelSetting != null) {
			Configuration.setString(KEY_IMPORT_EXTENDED_MODELE_LEVEL, String.valueOf(levelSetting.getSelection()));
		}
    }

    
    
}
