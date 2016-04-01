/* $Id: ActionAddEventAsTrigger.java 19614 2011-07-20 12:10:13Z linus $
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mvw
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2009 The Regents of the University of California. All
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

package org.argouml.uml.ui.behavior.activity_graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.uml.ui.AbstractActionAddModelElement2;

/**
 * Provide a dialog which helps the user to select one event out of an existing
 * list, which will be used as the trigger of the transition.
 * 
 * @author MarkusK
 * 
 */
public class ActionAddEventAsTrigger extends AbstractActionAddModelElement2 {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6472724134137733235L;
	/**
     * The one and only instance of this class.
     */
    public static final ActionAddEventAsTrigger SINGLETON =
        new ActionAddEventAsTrigger();

    /**
     * Constructor for ActionAddClassifierRoleBase.
     */
    protected ActionAddEventAsTrigger() {
        super();
        setMultiSelect(false);
    }


    protected List getChoices() {
        List vec = new ArrayList();
        // TODO: the namespace of enlisted events is currently the model. 
        // I think this is wrong, they should be
        // in the namespace of the activitygraph!
//        vec.addAll(Model.getModelManagementHelper().getAllModelElementsOfKind(
//                Model.getFacade().getNamespace(getTarget()),
//                Model.getMetaTypes().getEvent()));
        vec.addAll(Model.getModelManagementHelper().getAllModelElementsOfKind(
                Model.getFacade().getRoot(getTarget()),
                Model.getMetaTypes().getEvent()));

        return vec;
    }


    protected List getSelected() {
        List vec = new ArrayList();
        Object trigger = Model.getFacade().getTrigger(getTarget());
        if (trigger != null) {
            vec.add(trigger);
        }
        return vec;
    }


    protected String getDialogTitle() {
        return Translator.localize("dialog.title.add-events");
    }


    @Override
    protected void doIt(Collection selected) {
        Object trans = getTarget();
        if (selected == null || selected.size() == 0) {
            Model.getStateMachinesHelper().setEventAsTrigger(trans, null);
        } else {
            Model.getStateMachinesHelper().setEventAsTrigger(trans,
                    selected.iterator().next());
        }
    }

}
