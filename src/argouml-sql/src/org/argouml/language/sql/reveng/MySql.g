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
 *
 */
 /*
  * A grammar for reading mysqldump --no-data
  * 
  * Include Comment, drop table, create table
  * 	Link : http://dev.mysql.com/doc/refman/5.0/fr/create-table.html (Not complete)
  *
  */
 
grammar MySql; 

@header {
package org.argouml.language.sql.reveng;

import org.argouml.language.sql.TableDefinition;
import org.argouml.language.sql.ColumnDefinition;
import org.argouml.language.sql.ForeignKeyDefinition;

} 

@members {
	private Lexer _lexer = null;

	private Modeller _modeller;

	boolean readSpace = false;
	public Modeller getModeller() {
        return _modeller;
    }
    
    public void setModeller(Modeller modeller) {
        _modeller = modeller;
    }
}



@lexer::header {
package org.argouml.language.sql.reveng;

}

dump_read [Modeller modeller, Lexer lexer]
    @init{
        setModeller(modeller);
        _lexer = lexer;
        
    }
: 
 (  dumpComment
	| drop_table_statement
	| create_table_statement
	| set_statement
	)*  
	{
		getModeller().generateModele();
	}
;
     
	 
dumpComment: 
	SL_COMMENT
	| ML_COMMENT (';')?
;

set_statement:
	'SET' (VAR_MYSQL | ID) '=' (VAR_MYSQL | ID)
;

drop_table_statement :
	'DROP' 'TABLE' 'IF' 'EXISTS' (PROTECT_CHAR)? table_name (PROTECT_CHAR)? SEMICOLON ;
	
create_table_statement : 
	'CREATE' 'TABLE'
    		 (PROTECT_CHAR)? table_name (PROTECT_CHAR)? {
    		 	TableDefinition table = getModeller().getTableFromName($table_name.text);
			}
    		
             create_definition_list[table]
             table_options[table]
             
             
             SEMICOLON
;

table_name
     :   (schema DOT)? table
;
     
create_definition_list [TableDefinition table]
    : LEFT_PAREN create_definition[table] (COMMA create_definition[table])*   RIGHT_PAREN
; 

table_options [TableDefinition table]:
    ('ENGINE' '=' ID)?
    ('AUTO_INCREMENT' '=' NUMBER)?
    ('DEFAULT' 'CHARSET' '=' ID)?
    ('COMMENT' '=' text_quoted {$table.setComment($text_quoted.value);})?
;

create_definition [TableDefinition table]: 
        ('KEY'|'INDEX') index_name (index_type)? columns_list_name
        | constraint_def[table]
        |
         column_name {
         	ColumnDefinition col = ColumnDefinition.findOrCreateColumnDefinition(table, $column_name.cn);
         } data_type_def[col]
;

text_quoted returns [String value] :
	{
		int iStartToken = input.LT(1).getTokenIndex();
	}
	QUOTED_TEXT
	{
		value = $QUOTED_TEXT.text;
		value = value.substring(1);
		value = value.substring(0, value.length() -1 );
		value = value.replaceAll("''","'");
	}
;

text_quoted_keep returns [String value] :
	{
		int iStartToken = input.LT(1).getTokenIndex();
	}
	QUOTED_TEXT
	{
		value = $QUOTED_TEXT.text;
	}
;
	
constraint_def [TableDefinition table] returns [String constrName] :
	 ('CONSTRAINT' symbol
	 	{
	 		$constrName =  $symbol.name;
	 	} 
	 )? 
	 
	 (
	 constraint_unique 
	 | constraint_fk[table, constrName]
	 | constraint_pk[table, constrName]
	 )
	 
;
constraint_unique :
	'UNIQUE' ('INDEX' | 'KEY')? (index_name)? (index_type)?  columns_list_name
;
constraint_fk [TableDefinition table, String constrName] :
	'FOREIGN' 'KEY' (symbol)?
	{
		ForeignKeyDefinition fk = new ForeignKeyDefinition($table);
		fk.setForeignKeyName($constrName);
	}
	columns_list_name
	{
		for(String colName : $columns_list_name.listColumn) {
			fk.addColumnDefinition(colName);
		}
	}
	reference_definition[fk]
;

constraint_pk [TableDefinition table, String constrName] :
	'PRIMARY' 'KEY' (index_type)? columns_list_name
	{
		for(String colName : $columns_list_name.listColumn) {
			table.addPrimaryKeyField(colName);
		}
	}
;

reference_definition [ForeignKeyDefinition fk] :
	'REFERENCES' tbl_name 
	{
		TableDefinition tableRef = getModeller().getTableFromName($tbl_name.name);
		fk.setReferencesTable(tableRef);
		
	}
	columns_list_name
	{
		for(String colName : $columns_list_name.listColumn) {
			fk.addReferencesColumn(colName);
		}
	}
	
	('MATCH' ('FULL' | 'PARTIAL') )?
	('ON' ('DELETE' | 'UPDATE') reference_option )*
	
;

reference_option :
	'RESTRICT' | 'CASCADE' | 'SET' 'NULL' | 'NO ACTION' | 'SET DEFAULT'
;

columns_list_name returns [List<String> listColumn]:
	
	LEFT_PAREN c1=column_name 
	{
		$listColumn = new ArrayList<String>();
		$listColumn.add($c1.cn);
	}
	( 
		COMMA c2=column_name {
			$listColumn.add($c2.cn);
		}
	)* RIGHT_PAREN 
;

column_name returns [String cn] :
	name_with_esc {$cn = $name_with_esc.name;}
;
    
tbl_name returns [String name] :
    name_with_esc {$name = $name_with_esc.name;}
;

data_type_def [ColumnDefinition col] :
    data_type {$col.setDatatype($data_type.text);} 
    	(data_length)? {
    		$col.setLength($data_length.len);
    		$col.setNbDecimal($data_length.decimal);//can be null
    	}
   	 ( 'UNSIGNED' {})? 
   	 
   	 ( 'ZEROFILL' {} )? 
   	 ( 'NOT' 'NULL' {
   	 	$col.setNullable(false);
   	 	} 
   	 	| 'NULL' {
   	 	$col.setNullable(true);
   	 	}
   	 )? 
   	 ( 'DEFAULT' default_statement[col] )?
   	  
   	 ( 'AUTO_INCREMENT' )?  
   	 ( 'PRIMARY' 'KEY'  | 'KEY'	)? 
   	 ( 'COMMENT' text_quoted {$col.setComment($text_quoted.value);} )?
;
	
	
default_statement[ColumnDefinition col] :
	'NULL' {
		$col.setDefaultValue("NULL");
	}
	| 'CURRENT_TIMESTAMP' ('ON' 'UPDATE' 'CURRENT_TIMESTAMP')? {
		$col.setDefaultValue("CURRENT_TIMESTAMP");
	}
	| text_quoted_keep {
		$col.setDefaultValue($text_quoted_keep.value);
	}
	
;
data_length returns [Integer len, Integer decimal] :
     LEFT_PAREN i1=NUMBER (COMMA i2=NUMBER {$decimal = Integer.valueOf($i2.text);} )? RIGHT_PAREN {$len = Integer.valueOf($i1.text);}
;
     
schema : ID;
table : ID; 
data_type : ID;
index_name returns [String name] :
	(LEFT_PAREN)? name_with_esc {$name = $name_with_esc.name; } (RIGHT_PAREN)? ;

index_type :  'BTREE' | 'HASH';

symbol returns [String name] :
	name_with_esc  {$name = $name_with_esc.name; }
;
	
name_with_esc returns [String name] :
	(PROTECT_CHAR)? ID {$name = String.valueOf($ID.text); } (PROTECT_CHAR)? 
;

	
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/
 
LEFT_PAREN : '(';
RIGHT_PAREN : ')';
COMMA : ',';
SEMICOLON : ';';
DOT :    '.';


NUMBER  :   (DIGIT)+;
ID  : (( LETTER | '_') ((DIGIT)*))+ ;

VAR_MYSQL : '@' ('@')? ID;

PROTECT_CHAR : '`';

WS : ( '\t' | ' ' | '\r' | '\n' | '\u000C' )+   {$channel = HIDDEN;} ;
  
SL_COMMENT :
	( ('--'|'#') ~('\n'|'\r')* '\r'? '\n' )
;


ML_COMMENT : 
	'/*' .* '*/'
;

QUOTED_TEXT : 
	'\'' 
	( ~
		(
			'\''
		)
	| 
		'\''
		'\''
	 )* 
	'\''
;

fragment LETTER
    : 'a'..'z'
    | 'A'..'Z'
;
  
fragment DIGIT :   '0'..'9' ;

