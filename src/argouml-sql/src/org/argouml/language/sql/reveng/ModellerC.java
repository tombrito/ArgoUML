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

import org.argouml.language.sql.ColumnDefinition;
import org.argouml.language.sql.ForeignKeyDefinition;
import org.argouml.language.sql.TableDefinition;
import org.argouml.model.Model;

public class ModellerC implements ModellerLevel{
	
	private Modeller modellerSource;
	
	public ModellerC(Modeller modeller) {
		this.modellerSource = modeller;
	}
	
	
	public void genereInModele() {
		Map<String, Object> classes = new HashMap<String, Object>();
		
		List<ForeignKeyDefinition> foreign_keys = new ArrayList<ForeignKeyDefinition>();
		// build the classes.
		for(TableDefinition table : modellerSource.getTablesByName().values()) {
			//
			boolean writeClass = true;

			// Set to false if all attribute in PK are keys.
			//

			if (writeClass) {
				Object curClass = modellerSource.addClass(table);
				classes.put(table.getName(), curClass);
				for(ColumnDefinition c : table.getColumnDefinitions()) {
					String attributeName = c.getName();
					// TODO Set a UML type instead of a type SQL (or with a tagValue)
					// How to have the YML Datatype.
					String typeSpec = modellerSource.getMappingDataTypeSQLToUML(c.getDatatype());
					
					
					// Don't create attribute if a FK exists.
					// => reset attributeName to null
					for (ForeignKeyDefinition fk : table.getFkDefinitions()){
						if(fk.hasColumnInTable(attributeName)){
							attributeName = null;
							break;
						}
					}
	    		
					if(attributeName!=null) {
						
						// TODO : profile(SQL, or match with UML standard for conception) 
						Object packageOfType = modellerSource.getModel();
						Object mClassifierType = null;
						if (typeSpec != null) {
							mClassifierType = modellerSource.getOrAddDatatype(packageOfType, typeSpec);
						}
			    		//Object mClassifier = null;
						Object mAttribute =  modellerSource.buildAttribute(curClass, mClassifierType, attributeName);
						String multiplicity = Modeller.ASSOCIATION_1;
						if (c.getNullable() == null || c.getNullable()) {
							multiplicity = Modeller.ASSOCIATION_01;
						}
						Model.getCoreHelper().setMultiplicity(mAttribute,
								multiplicity);
						
						if (c.getDefaultValue() != null) {
							Object newInitialValue = Model.getDataTypesFactory()
									.createExpression("Sql", c.getDefaultValue());
							Model.getCoreHelper().setInitialValue(mAttribute,
									newInitialValue);	
						}
						
					
					}
		    	}
				foreign_keys.addAll(table.getFkDefinitions());
			} // end write class
			
		}
		
		for(ForeignKeyDefinition fk : foreign_keys) {
			String name = fk.getForeignKeyName();
			
			int typeAsso = Modeller.ASSOCIATION;
			String multiplicity = Modeller.ASSOCIATION_1;
			
			if (fk.getReferencesTable() != fk.getTable()) {
				List<String> fkTable = fk.getColumnNames();
				
				List<String> pkTable = fk.getTable().getPrimaryKeyFields();
				List<String> pkRef = fk.getReferencesTable().getPrimaryKeyFields();
				
				if (fkTable.size()>0 && pkTable.size()>0 && fkTable.containsAll(pkTable) && pkTable.containsAll(fkTable)) {
					
					if (pkRef.size()>0 && fkTable.containsAll(pkTable) && pkTable.containsAll(fkTable)) {
						typeAsso = Modeller.GENERALIZATION;	
					}
				}
				
			}
			
			// if at least one is column of the FK, in the Table is nullable: "0..1", otherwise "1".
			for (ColumnDefinition columnDefinition : fk.getColumns()) {
				if (columnDefinition.getNullable() == null
						|| columnDefinition.getNullable()) {
					multiplicity = Modeller.ASSOCIATION_01;
					break;
				}
			}
			
			
			
			// Build the good association type (association ?, composition ?, agregation ?, generalisation ?)
			Object mClassifier = classes.get(fk.getReferencesTable().getName());
			Object mClassifierEnd = classes.get(fk.getTable().getName());
			
			String nameAssociationEnd = name;
			
			if (typeAsso == Modeller.GENERALIZATION) {
				Object mGeneralization = modellerSource.getGeneralization(modellerSource.getModel(), mClassifier, mClassifierEnd);
				Model.getCoreHelper().setName(mGeneralization, nameAssociationEnd);
				
			} else if (typeAsso == Modeller.ASSOCIATION) {
				Object mAssociationEnd = modellerSource.getAssociationEnd(name, mClassifier, mClassifierEnd);
				//setVisibility(mAssociationEnd, modifiers);
				Model.getCoreHelper().setMultiplicity(
	                  mAssociationEnd,
	                  multiplicity);
				Model.getCoreHelper().setType(mAssociationEnd, mClassifier);
				
				// String nameAssociationEnd = name;
				if (fk.getColumns().size() == 1) {
					nameAssociationEnd = fk.getColumns().get(0).getName();
				}
				
				Model.getCoreHelper().setName(mAssociationEnd, nameAssociationEnd);
				if (!mClassifier.equals(mClassifierEnd)) {
					// Because if they are equal,
					// then getAssociationEnd(name, mClassifier) could return
					// the wrong assoc end, on the other hand the navigability
					// is already set correctly (at least in this case), so the
					// next line is not necessary. (maybe never necessary?) - thn
					Model.getCoreHelper().setNavigable(mAssociationEnd, true);
				}
				//addDocumentationTag(mAssociationEnd, javadoc);*
				// else if (typeAsso == GENERALIZATION) {
				//}
			}
				
			
			
		}
		
	}
	

}
