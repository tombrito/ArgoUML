/* $Id$
 *******************************************************************************
 * Copyright (c) 2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent BRAUD (From Java module)
 *******************************************************************************
 */

package org.argouml.language.sql.reveng;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.argouml.application.api.Argo;

import org.argouml.kernel.ProjectManager;
import org.argouml.language.sql.TableDefinition;
import org.argouml.model.Model;


import org.argouml.profile.Profile;

public class Modeller {
	public static final int ASSOCIATION = 1;
	public static final int GENERALIZATION = 2;
	
	// these next const need to be in a generic class
	public static final String ASSOCIATION_1 = "1";
	public static final String ASSOCIATION_01 = "0..1";
	
	/**
     * Current working model.
     */
    private Object model;

    /*
     * Sql profile model.
     *
    private Profile sqlProfile;
    */
    /**
     * The name of the file being parsed.
     */
    private String fileName;
    
    /**
     * New model elements that were created during this reverse engineering
     * session. TODO: We want a stronger type here, but ArgoUML treats all
     * elements as just simple Objects.
     */
    private Collection<Object> newElements;
    
    private String settingLevel;
    
    
    private Map<String, TableDefinition> tablesByName;
    
    /////////
    public Modeller(Object theModel, Profile theSqlProfile,
            String theFileName) {
        model = theModel;
        //sqlProfile = theSqlProfile;
        
        newElements = new HashSet<Object>();
        fileName = theFileName;
        
        tablesByName = new HashMap<String, TableDefinition>();
        
        
    }
    
    public String getMappingDataTypeSQLToUML(final String typeSQL) {
    	String typeUML = "String";
		if (typeSQL.toLowerCase().indexOf("char") > 0) {
			// char, varchar, nvarchar [oracle], ...
			typeUML = "String";
		} else if (typeSQL.toLowerCase().indexOf("int") > 0) {
			// integer , *int,
			typeUML = "Integer";
		} else if (typeSQL.toLowerCase().indexOf("Boolean") > 0) {
			typeUML = "Boolean";
		} else if (typeSQL.toLowerCase().indexOf("text") > 0) {
			typeUML = "String";
		}
    	
    	return typeUML;
	}

	/**
     * Get the elements which were created while reverse engineering this file.
     * 
     * @return the collection of elements
     */
    public Collection<Object> getNewElements() {
        return newElements;
    }
    
	//////////////////////////
	/**
	* Call by the SqlParser
	* Build all elements
	*/
	public void generateModele() {
		
		ModellerLevel generation = null;
		if (this.settingLevel.equals(SqlImportSettings.LEVEL_MCD)) {
			generation =new ModellerC(this);
		} else {
			// default : this.settingLevel.equals(SqlImportSettings.LEVEL_MPD)
			generation =new ModellerP(this);
		}
		
		generation.genereInModele();
		
		
	}
	
	/**
	 * 
	 * @param name : TODO can be null, not done.
	 * @param mClassifier
	 * @param mClassifierEnd
	 * @return
	 */
	public Object getAssociationEnd(String name, Object mClassifier, Object mClassifierEnd) {
        Object mAssociationEnd = null;
        for (Iterator<Object> i = Model.getFacade().getAssociationEnds(mClassifier)
                .iterator(); i.hasNext();) {
			Object ae = i.next();
			Object assoc = Model.getFacade().getAssociation(ae);
			if (name.equals(Model.getFacade().getName(ae))
					&& Model.getFacade().getConnections(assoc).size() == 2
					&& Model.getFacade().getType(Model.getFacade().getNextEnd(ae)) == mClassifierEnd) {
				mAssociationEnd = ae;
			}
        }
        if (mAssociationEnd == null) {
            Object mAssociation = buildDirectedAssociation(name, mClassifierEnd, mClassifier);
            // this causes a problem when mClassifier is not only 
            // at one assoc end: (which one is the right one?)
            mAssociationEnd =
                Model.getFacade().getAssociationEnd(
                        mClassifier,
                        mAssociation);
        }
        return mAssociationEnd;
    }
	
	/**
     * Build a unidirectional association between two Classifiers.(From SQL/JAVA)
     * 
     * @param name name of the association
     * @param sourceClassifier source classifier (end which is non-navigable)
     * @param destClassifier destination classifier (end which is navigable)
     * @return newly created Association
     */
    public static Object buildDirectedAssociation(String name,
            Object sourceClassifier, Object destClassifier) {
        return Model.getCoreFactory().buildAssociation(destClassifier, true,
                sourceClassifier, false, name);
    }
	
	/**
	 * Call by the SqlParser
	 * Must be call before the end of pasring a table (so, only when then name is known), because a FK can reference himself.
	 */
	public void addTable(final TableDefinition table) {
		tablesByName.put(table.getName(), table);
	}
   
	public TableDefinition getTableFromName(final String nameTable) {
		TableDefinition ret = tablesByName.get(nameTable);
		if (ret == null) {
			ret = new TableDefinition();
			ret.setName(nameTable);
			addTable(ret);
		}
		return ret;
	}

    
	public Map<String, TableDefinition> getTablesByName() {
		return tablesByName;
	}
	
    public Object addClass(final TableDefinition table) {
        
        Object mClass = addClassifier(Model.getCoreFactory().createClass(),
        		table.getName(), table.getComment(), null);

        /*Model.getCoreHelper().setAbstract(mClass,
                (modifiers & JavaParser.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mClass,
                (modifiers & JavaParser.ACC_FINAL) > 0);
        */
        if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
            Model.getCoreHelper().setRoot(mClass, false);
        }
        newElements.add(mClass);
        return mClass;
    }
    
    
    public Object getOrAddDatatype(Object packageOfType, String typeSpec) {
    	Object mClassifierType = Model.getFacade().lookupIn(packageOfType, typeSpec);
		if (mClassifierType == null) {
			mClassifierType = Model.getCoreFactory().buildDataType(typeSpec, packageOfType);
			newElements.add(mClassifierType);
		}
		return mClassifierType;
    }
    
    private Object addClassifier(Object newClassifier, String name,
            String documentation, List<String> typeParameters) {
        Object mClassifier;
        Object mNamespace;

        Object currentPackage = this.model;
        
        
        mClassifier = Model.getFacade().lookupIn(currentPackage, name);
        mNamespace = currentPackage;

        if (mClassifier == null) {
            // if the classifier could not be found in the model
            //if (LOG.isInfoEnabled()) {
            //    LOG.info("Created new classifier for " + name);
            //}
            mClassifier = newClassifier;
            Model.getCoreHelper().setName(mClassifier, name);
            Model.getCoreHelper().setNamespace(mClassifier, mNamespace);
            newElements.add(mClassifier);
        }/* else {
            // it was found and we delete any existing tagged values.
            if (LOG.isInfoEnabled()) {
                LOG.info("Found existing classifier for " + name);
            }
            // TODO: Rewrite existing elements instead? - tfm
            cleanModelElement(mClassifier);
        } */

        /*
        // set up the artifact manifestation (only for top level classes)
        if (parseState.getClassifier() == null) {
            if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
                // set the classifier to be a resident in its component:
                // (before we push a new parse state on the stack)
    
                // This test is carried over from a previous implementation,
                // but I'm not sure why it would already be set - tfm
                if (Model.getFacade()
                        .getElementResidences(mClassifier).isEmpty()) {
                    Object resident = Model.getCoreFactory()
                            .createElementResidence();
                    Model.getCoreHelper().setResident(resident, mClassifier);
                    Model.getCoreHelper().setContainer(resident,
                            parseState.getArtifact());
                }
            } else {
                Object artifact = parseState.getArtifact();
                Collection c =
                    Model.getCoreHelper().getUtilizedElements(artifact);
                if (!c.contains(mClassifier)) {
                    Object manifestation = Model.getCoreFactory()
                            .buildManifestation(mClassifier);
                    Model.getCoreHelper()
                            .addManifestation(artifact, manifestation);
                }
            }
        }*/

        /*
        // change the parse state to a classifier parse state
        parseStateStack.push(parseState);
        parseState = new ParseState(parseState, mClassifier, currentPackage);
		*/
        

        // Add classifier documentation tags during first (or only) pass only
        //if (getLevel() <= 0) {
            addDocumentationTag(mClassifier, documentation);
        //}
        // addTypeParameters(mClassifier, typeParameters);
        return mClassifier;
    }
    
    
    private void addDocumentationTag(Object modelElement, String sDocumentation) {
        if ((sDocumentation != null) && (sDocumentation.trim().length() >= 1)) {
        	//Now store documentation text in a tagged value
            String[] docs = {
            		sDocumentation
            };
            buildTaggedValue(modelElement, Argo.DOCUMENTATION_TAG, docs);
            addStereotypes(modelElement);
        }
    }
    
    private void buildTaggedValue(Object me, 
            String sTagName, 
            String[] sTagData) {
        Object tv = Model.getFacade().getTaggedValue(me, sTagName);
        if (tv == null) {
            // using deprecated buildTaggedValue here, because getting the tag
            // definition from a tag name is the critical step, and this is
            // implemented in ExtensionMechanismsFactory in a central place,
            // but not as a public method:
            Model.getExtensionMechanismsHelper().addTaggedValue(
                    me,
                    Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(sTagName, sTagData[0]));
        } else {
            Model.getExtensionMechanismsHelper().setDataValues(tv, sTagData);
        }
    }
    
    private void addStereotypes(Object modelElement) {
        // TODO: What we do here is allowed for UML 1.x only!
        if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
            Object tv = Model.getFacade()
                    .getTaggedValue(modelElement, "stereotype");
            if (tv != null) {
                String stereo = Model.getFacade().getValueOfTag(tv);
                if (stereo != null && stereo.length() > 0) {
                    StringTokenizer st = new StringTokenizer(stereo, ", ");
                    while (st.hasMoreTokens()) {
                        Model.getCoreHelper().addStereotype(modelElement,
                                getUML1Stereotype(st.nextToken().trim()));
                    }
                    ProjectManager.getManager().updateRoots();
                }
                Model.getUmlFactory().delete(tv);
            }
        }
    }
    
    /**
     * Get the stereotype with a specific name. UML 1.x only.
     * 
     * @param name The name of the stereotype.
     * @return The stereotype.
     */
    private Object getUML1Stereotype(String name) {
        //LOG.debug("Trying to find a stereotype of name <<" + name + ">>");
        // Is this line really safe wouldn't it just return the first
        // model element of the same name whether or not it is a stereotype
        Object stereotype = Model.getFacade().lookupIn(model, name);

        if (stereotype == null) {
            //LOG.debug("Couldn't find so creating it");
            return Model.getExtensionMechanismsFactory().buildStereotype(name,
                    model);
        }

        if (!Model.getFacade().isAStereotype(stereotype)) {
            // and so this piece of code may create an existing stereotype
            // in error.
            //LOG.debug("Found something that isn't a stereotype so creating it");
            return Model.getExtensionMechanismsFactory().buildStereotype(name,
                    model);
        }

        //LOG.debug("Found it");
        return stereotype;
    }
    
    public Object buildAttribute(Object classifier, Object type, String name) {
        Object mAttribute = Model.getCoreFactory().buildAttribute2(classifier,
                type);
        
        newElements.add(mAttribute);
        
        Model.getCoreHelper().setName(mAttribute, name);
        return mAttribute;
    }

	public void setLevel(final String level) {
		this.settingLevel = level;
	}
    
	public Object getGeneralization(Object mPackage,
            Object parent,
            Object child) {
		Object mGeneralization = Model.getFacade().getGeneralization(child,
				parent);
		if (mGeneralization == null) {
			mGeneralization = Model.getCoreFactory().buildGeneralization(child,
					parent);
			newElements.add(mGeneralization);
		}
		if (mGeneralization != null) {
			Model.getCoreHelper().setNamespace(mGeneralization, mPackage);
		}
		return mGeneralization;
	}
	
	
	public Object getModel() {
		return this.model;
	}
}
