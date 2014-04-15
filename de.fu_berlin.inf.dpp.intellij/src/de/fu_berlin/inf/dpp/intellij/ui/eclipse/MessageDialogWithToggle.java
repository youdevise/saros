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

package de.fu_berlin.inf.dpp.intellij.ui.eclipse;

import de.fu_berlin.inf.dpp.core.preferences.IPreferenceStore;

import javax.swing.*;
import java.awt.*;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-11
 * Time: 16:35
 */

public class MessageDialogWithToggle extends JDialog
{
    public static final String PROMPT = "prompt";
    public static final String ALWAYS = "always";

    private int returnCode = -1;

    protected MessageDialogWithToggle(int returnCode)
    {
        this.returnCode = returnCode;
    }

    public MessageDialogWithToggle()
    {
    }

    public int getReturnCode()
    {
        return returnCode;
    }

    public static MessageDialogWithToggle openYesNoQuestion(Container container, String title, String msg, String s, boolean b, IPreferenceStore preferenceStore, String autoStopEmptySession)
    {
        int returnCode = JOptionPane.showConfirmDialog(container, msg, title, JOptionPane.YES_NO_OPTION);

        return new MessageDialogWithToggle(returnCode);

    }
}
