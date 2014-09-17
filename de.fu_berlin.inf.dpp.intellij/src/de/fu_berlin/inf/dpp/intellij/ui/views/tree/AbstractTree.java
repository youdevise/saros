/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.views.tree;

import org.apache.log4j.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Parent class for saros tree
 */
public abstract class AbstractTree extends DefaultMutableTreeNode {
    protected static final Logger LOG = Logger.getLogger(AbstractTree.class);

    protected final DefaultMutableTreeNode parent;

    protected AbstractTree() {
        parent = this;
    }

    protected AbstractTree(DefaultMutableTreeNode parent) {
        this();
        parent.add(this);
    }

}
