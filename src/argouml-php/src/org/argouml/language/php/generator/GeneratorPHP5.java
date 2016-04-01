/* $Id: org.eclipse.jdt.ui.prefs 213 2010-02-11 21:47:01Z linus $
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

package org.argouml.language.php.generator;

import org.argouml.uml.generator.CodeGenerator;

public class GeneratorPHP5 extends GeneratorPHP4 implements CodeGenerator {
    
    
    public GeneratorPHP5() {
        super(ModulePHP5.LANGUAGE_MAJOR_VERSION_5);
    }

    /**
     * PHP4: A constructor have the same name than class
     * @return
     */
    @Override
    protected String getDestructorName(Object operation) {
        return "__destruct";
    }

    /**
     * PHP4: A constructor have the same name than class
     * @return
     */
    @Override
    protected String getConstructorName(Object operation) {
        return "__construct";
    }
    
    
}
