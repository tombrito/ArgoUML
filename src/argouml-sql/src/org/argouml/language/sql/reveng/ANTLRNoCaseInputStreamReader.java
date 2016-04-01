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

import java.io.IOException;
import java.io.InputStreamReader;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CharStream;

/**
 * Usefull because syntaxe SQL is not case sentisive.
 * 
 * http://www.antlr.org/wiki/pages/viewpage.action?pageId=1782
 */
public class ANTLRNoCaseInputStreamReader extends ANTLRReaderStream {
	
	public ANTLRNoCaseInputStreamReader(InputStreamReader is) throws IOException {
		super(is);
	}

	@Override
	public int LA(int i) {
		if (i == 0) {
			return 0; // undefined
		}
		if (i < 0) {
			i++; // e.g., translate LA(-1) to use offset 0
		}

		if ((p + i - 1) >= n) {

			return CharStream.EOF;
		}
		return Character.toUpperCase(data[p + i - 1]);
	}
	
}
