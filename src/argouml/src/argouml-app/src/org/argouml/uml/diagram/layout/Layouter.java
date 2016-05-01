/* $Id: Layouter.java 19907 2012-12-30 13:06:01Z closettop_nightlybuild $
 *****************************************************************************
 * Copyright (c) 2009-2012 Contributors - see below
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

// Copyright (c) 1996-2006 The Regents of the University of California. All
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

package org.argouml.uml.diagram.layout;

import java.awt.Dimension;

/**
 * Any layouter for any diagram type should implement this interface.
 */
public interface Layouter {

	/**
	 * Add another object to the diagram.
	 *
	 * @param obj
	 *            represents the object to be part of the diagram.
	 */
	void add(LayoutedObject obj);

	/**
	 * Remove a object from the diagram.
	 *
	 * @param obj
	 *            represents the object to be removed.
	 */
	void remove(LayoutedObject obj);

	/**
	 * Operation getObjects returns all the layouted objects from this diagram.
	 *
	 * @return An array with the layouted objects of this diagram.
	 */
	LayoutedObject[] getObjects();

	/**
	 * Operation getObject returns one object from the diagram.
	 *
	 * @param index
	 *            represents the index of this object.
	 * @return the object
	 */
	LayoutedObject getObject(int index);

	/**
	 * This operation starts the actual layout process.
	 */
	void layout();

	/**
	 * Operation getMinimumDiagramSize returns the minimum diagram size after
	 * the layout, so the diagram could be resized to this size.
	 *
	 * @return the size/dimension
	 */
	Dimension getMinimumDiagramSize();
}
