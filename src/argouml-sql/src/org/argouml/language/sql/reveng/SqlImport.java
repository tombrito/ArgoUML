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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenStream;
import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.uml.reveng.SettingsTypes.Setting;
import org.argouml.util.SuffixFilter;

/**
 * 
 *
 * @author BRAUD
 */
public class SqlImport implements ImportInterface {

	 /**
     * New model elements that were added
     */
    private Collection<Object> newElements;
    
    
    /*
     * Sql profile model.(not used now)
     
    private Profile sqlProfile = null;
    */
    /**
     * 
     * @link SqlImportSettings#getCodeLevel()
     */
    private String settingLevel;
	/*
     * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project,
     *      java.util.Collection, org.argouml.uml.reveng.ImportSettings,
     *      org.argouml.application.api.ProgressMonitor)
     */
	@Override
	public Collection<Object> parseFiles(Project p, Collection<File> files,
			ImportSettings settings, ProgressMonitor monitor)
			throws ImportException {
		
		SqlImportSettings.getInstance().saveSettings();
		
		SqlImportSettings sqlSettings = SqlImportSettings.getInstance();
		String sgbd = sqlSettings.getCodeSgbd();
		settingLevel = sqlSettings.getCodeLevel();
		
		newElements = new HashSet<Object>();
		monitor.updateMainTask(Translator.localize("dialog.import.pass1"));
		// sqlProfile = getSqlProfile(p);
		
		try {
			monitor.setMaximumProgress(files.size());
			doImportPass(p, files, settings, monitor, 0, 0, sgbd);
			
		} finally {
	          
		}
		return newElements;
	}
	
	
	private void doImportPass(Project p, Collection<File> files,
            ImportSettings settings, ProgressMonitor monitor, int startCount,
            int pass, String sgbdName) {

        int count = startCount;
        for (File file : files) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(Translator
                        .localize("dialog.import.cancelled"));
                return;
            }
            try {
                parseFile(p, file, settings, pass, sgbdName);
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new java.io.PrintWriter(sw);
                e.printStackTrace(pw);
                monitor.notifyMessage(
                    Translator.localize(
                            "dialog.title.import-problems"), //$NON-NLS-1$
                    Translator.localize("label.import-problems"), //$NON-NLS-1$
                    sw.toString());
                if (monitor.isCanceled()) {
                    break;
                }
            }
            monitor.updateProgress(count++);
            monitor.updateSubTask(Translator.localize(
                    "dialog.import.parsingAction", 
                    new Object[] { 
                        file.getAbsolutePath() 
                    }));

        }

        return;
    }
	
	
	 /**
     * Do a single import pass of a single file.
     * 
     * @param p the project
     * @param f the source file
     * @param settings the user provided import settings
     * @param pass current import pass - 0 = single pass, 1 = pass 1 of 2, 2 =
     *                pass 2 of 2
     */
	private void parseFile(Project p, File f, ImportSettings settings, int pass, String sgbdName)
        throws ImportException {

        try {
            // Create a scanner that reads from the input stream
            String encoding = settings.getInputSourceEncoding();
            FileInputStream in = new FileInputStream(f);
            InputStreamReader isr;
            try {
                isr = new InputStreamReader(in, encoding);
            } catch (UnsupportedEncodingException e) {
                // fall back to default encoding
                isr = new InputStreamReader(in);
            }
            
            // Create a modeller for the parser
			Modeller modeller = new Modeller(
					p.getUserDefinedModelList().get(0), null, f.getName());
			modeller.setLevel(this.settingLevel);
			
            try {
            	// MySqlLexer lexer = new MySqlLexer(new ANTLRNoCaseInputStreamReader(isr));
            	Class<Lexer> lexerClass = (Class<Lexer>) Class.forName("org.argouml.language.sql.reveng." + sgbdName + "Lexer");
            	Class[] argTypes = {CharStream.class};
            	Constructor<Lexer> constructor = lexerClass.getDeclaredConstructor(argTypes);
            	Object[] argumentsLexer = {new ANTLRNoCaseInputStreamReader(isr)};
            	Lexer instanceLexer = constructor.newInstance(argumentsLexer);
            	
            	// Create a parser that reads from the scanner
                //MySqlParser parser = new MySqlParser(new CommonTokenStream(lexer));
            	Class<Parser> parserClass = (Class<Parser>) Class.forName("org.argouml.language.sql.reveng." + sgbdName  + "Parser");
            	Class[] argTypesP = {TokenStream.class};
            	Constructor<Parser> constructorParser = parserClass.getDeclaredConstructor(argTypesP);
            	Object[] arguments = {new CommonTokenStream(instanceLexer)};
            	Parser instanceParser = constructorParser.newInstance(arguments);
            	
            	
            	//parser.dump_read(modeller, lexer);
            	Class[] argTypesM = {Modeller.class, Lexer.class};
				Method printMethod = parserClass.getMethod("dump_read", argTypesM);
				Object[] argumentsM = {modeller, instanceLexer};
            	printMethod.invoke(instanceParser, argumentsM);

            } catch(ClassNotFoundException e) {
            	StringBuilder errorString = new StringBuilder("Parser or Lexer not found : ");
            	errorString.append(sgbdName);
            	errorString.append(". "); 
            	errorString.append(buildErrorString(f));
            	throw new ImportException(errorString.toString(), e);
            } catch(Exception e) {
            	String errorString = buildErrorString(f);
                //LOG.error(e.getClass().getName() + errorString, e);
                throw new ImportException(errorString, e);
            } finally {
            	 newElements.addAll(modeller.getNewElements());
                 in.close();
            }
            
        } catch (IOException e) {
            throw new ImportException(buildErrorString(f), e);
        }
    }

	private String buildErrorString(File f) {
		String path = "";
		try {
			path = f.getName();
			path = f.getCanonicalPath();
		} catch (IOException e) {
			// Just ignore - we'll use the simple file name
		}
		return "Exception in file: " + path;
	}
    
	/*
     * Get the Sql profile from project, if available.
     * 
     * @param p the project
     * @return the Java profile
     
    private Profile getSqlProfile(Project p) {
    	Profile ret = null;
        for (Profile profile : p.getProfileConfiguration().getProfiles()) {
            if ("Sql".equals(profile.getDisplayName())) {
            	ret = profile;
            }
        }
        return ret;
    }*/

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    @Override
    public String getName() {
    	return "Sql-import";
    }
    
    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     * TODO : like in java module.
     */
    @Override
    public String getInfo(final int type) {
    	switch (type) {
        case DESCRIPTION:
            return "SQL import from dump files.";
        case AUTHOR:
            return "Laurent BRAUD";
        case VERSION:
            return "0.35.1";
        case DOWNLOADSITE:
            return "http://argouml-sql.tigris.org/";
        default:
            return null;
        }
    }
    

    /*
     * @see org.argouml.uml.reveng.ImportInterface#getSuffixFilters()
     */
    @Override
    public SuffixFilter[] getSuffixFilters() {
		SuffixFilter[] result = { new SuffixFilter("sql",
				Translator.localize("argouml-sql.filefilter.sql")), };
        return result;
    }
    
    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    @Override
    public boolean disable() {
    	// We are permanently enabled
    	return false;
    }
    
    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    @Override
    public boolean enable() {
        ImporterManager.getInstance().addImporter(this);
        return true;
    }
    
    @Override
    public List<Setting> getImportSettings() {
    	return SqlImportSettings.getInstance().getImportSettings();
    }
    
    @Override
    public boolean isParseable(File file) {
    	return FileImportUtils.matchesSuffix(file, getSuffixFilters());
    }
    
    
}
