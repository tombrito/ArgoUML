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

public class ModellerP implements ModellerLevel {
	
	private Modeller modellerSource;
	
	public ModellerP(Modeller modeller) {
		this.modellerSource = modeller;
	}
	
	
	@Override
	public void genereInModele() {
		Map<String, Object> classes = new HashMap<String, Object>();
		
		List<ForeignKeyDefinition> foreign_keys = new ArrayList<ForeignKeyDefinition>();
		// TODO : in a PMD package : Stereotype <<Physical Data Model>>
		Object packageOfType = modellerSource.getModel();
		
		
		// build the classes.
		for(TableDefinition table : modellerSource.getTablesByName().values()) {
			//
			boolean writeClass = true;
			
			
			if (writeClass) {
				Object curClass = modellerSource.addClass(table);
				classes.put(table.getName(), curClass);
				Object stereoTable = Model.getExtensionMechanismsFactory().buildStereotype(curClass, "table", packageOfType);
				
				for(ColumnDefinition c : table.getColumnDefinitions()) {
					String attributeName = c.getName();
					String typeSpec = c.getDatatype();
		    		
					// TODO : Stereotype,...	
					// TODO : profile(SQL, or match with UML standard for conception) 
					
					// TODO : Allow to add a package (which can have a stereotype).
					Object mClassifierType = null;
					if (typeSpec != null) {
						mClassifierType = modellerSource.getOrAddDatatype(packageOfType, typeSpec);
					}
					
					
					Object mAttribute = modellerSource.buildAttribute(curClass, mClassifierType, attributeName);
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
					
					if(table.getPrimaryKeyFields().contains(attributeName)) {
						// TODO Only build if doesn't exists
						Object stereoPk = Model.getExtensionMechanismsFactory().buildStereotype(mAttribute, "PK", packageOfType);
					}
					
					
					for (ForeignKeyDefinition fk : table.getFkDefinitions()){
						if(fk.hasColumnInTable(attributeName)){
							// TODO : th "fk" stereotype isn't the same than in ForeignKey
							Object stereoFk = Model.getExtensionMechanismsFactory().buildStereotype(mAttribute, "FK", packageOfType);
							break;
						}
					}
					
		    	}
				foreign_keys.addAll(table.getFkDefinitions());
			} // end write class
			
		}
		
		for(ForeignKeyDefinition fk : foreign_keys) {
			String name = fk.getForeignKeyName();
			
			//int typeAsso = Modeller.ASSOCIATION;
			String multiplicity = Modeller.ASSOCIATION_1;
			
			// Build the good association type (association ?, composition ?, agregation ?, generalisation ?)
			Object mClassifier = classes.get(fk.getReferencesTable().getName());
			Object mClassifierEnd = classes.get(fk.getTable().getName());
			
			String nameAssociationEnd = name;
			
			
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

			//
			Object stereoFK = Model.getExtensionMechanismsFactory().buildStereotype(mAssociationEnd, "FK", packageOfType);
			
			
		}
	}
}
