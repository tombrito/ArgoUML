/* $Id: ColumnDefinition.java 309 2013-06-03 17:51:23Z elbedd $
 *****************************************************************************
 * Copyright (c) 2009-2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    drahmann
 *    Laurent BRAUD
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.language.sql;

/**
 * Represents a column definition.
 * 
 * @author drahmann
 */
public class ColumnDefinition {
    private String datatype;

    private String defaultValue;

    private String name;

    private Boolean nullable;

    private Integer length;
    
    private Integer nbDecimal;
    
    /**
     * Comment, label,...
     */
    private String comment;
    
    /**
     * Table where is the column.
     */
    private TableDefinition table;
    
    /**
     * Creates a new column definition.
     * 
     */
    public ColumnDefinition() {
        super();
    }

    public ColumnDefinition(TableDefinition table, String name) {
        this();
        this.name = name;
        this.table = table;
        table.addColumnDefinition(this);
    }
    
    /**
     * Creates a new column definition with the given attributes.
     * 
     * @param datatype
     * @param name
     * @param nullable
     */
    public ColumnDefinition(String datatype, String name, Boolean nullable) {
        this();
        this.datatype = datatype;
        this.name = name;
        this.nullable = nullable;
    }

    /**
     * return the column in the table.
     * If doesn't exist, create it.
     * 
     * Use this because if 2 (or a third) classes references each other, we need to be able to import a FK 
     * with a REFERENCES table which doesn't exist.(mysql: FOREIGN_KEY_CHECKS=0 )
     *  
     * @param table
     * @param columnName
     * @return
     */
	public static ColumnDefinition findOrCreateColumnDefinition(TableDefinition table,
			String columnName) {
    	ColumnDefinition ret = table.getColumnDefinition(columnName);
    	if(ret == null) {
    		ret = new ColumnDefinition(table, columnName);
    	}
    	
    	return ret;
    }
    
    /**
     * 
     * @return The datatype of the column definition.
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * 
     * @return The default Value of the column definition.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * 
     * @return The name of the column.
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return Whether this column is NULLable
     */
    public Boolean getNullable() {
        return nullable;
    }

    /**
     * Set the datatype of the column.
     * 
     * @param datatype
     */
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * Set the default value of the column.
     * 
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set the name of the column.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set whether this column is NULLable.
     * 
     * @param nullable
     */
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }
    
    public Integer getLength() {
		return length;
	}
    
    public void setLength(Integer length) {
		this.length = length;
	}
    
    public Integer getNbDecimal() {
		return nbDecimal;
	}
    
    public void setNbDecimal(Integer nbDecimal) {
		this.nbDecimal = nbDecimal;
	}
    
    public TableDefinition getTable() {
		return table;
	}
    
    public void setTable(TableDefinition table) {
		this.table = table;
	}

    public String getComment() {
		return comment;
	}
    
    public void setComment(String comment) {
		this.comment = comment;
	}
}
