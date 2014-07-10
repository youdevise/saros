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

package de.fu_berlin.inf.dpp.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.views.SarosMainPanelView;
import org.apache.log4j.PropertyConfigurator;

/**
 * Saros core panel tool window factory. Here is a starting point of IntelliJ plugin
 * <p/>
 */

public class SarosToolWindowFactory implements ToolWindowFactory {
    /**
     * Plugin starting point via IntelliJ
     *
     * @param project
     * @param toolWindow
     */
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {

        PropertyConfigurator.configure("/home/holger/code/saros-raimondas/de.fu_berlin.inf.dpp.intellij/log4j.properties");  //todo

        Saros saros = Saros.instance();
        saros.setToolWindow(toolWindow);

        SarosMainPanelView mainPanel = new SarosMainPanelView(saros);
        mainPanel.create();
    }
}
