/* $Id: MemberModeler.java 505 2013-02-13 11:33:49Z euluis $
 *****************************************************************************
 * Copyright (c) 2009-2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Luis Sergio Oliveira (euluis)
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

package org.argouml.language.cpp.reveng;

import java.util.logging.Logger;

import org.argouml.language.cpp.profile.ProfileCpp;


/**
 * Modeler for C++ class members (without a specific kind specified).
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.28.0
 */
class MemberModeler {
    private static final Logger LOG = Logger.getLogger(
            MemberModeler.class.getName());
    
    private Object type;
    
    Object getType() {
        return type;
    }
    
    void setType(Object theType) {
        LOG.finer("Got the type: " + theType);
        type = theType;
    }
    
    private final Object owner;

    Object getOwner() {
        return owner;
    }

    private ProfileCpp profile;

    ProfileCpp getProfile() {
        return profile;
    }
    
    MemberModeler(Object theOwner, Object accessSpecifier,
            ProfileCpp theProfile) {
        owner = theOwner;
        profile = theProfile;
    }
    
    /**
     * Finish the modeling of the member.
     */
    void finish() {
    }
}