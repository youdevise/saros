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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import de.fu_berlin.inf.dpp.core.ui.IEnterProjectNamePage;
import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 14.22
 */

public class EnterProjectNamePage implements IEnterProjectNamePage
{
    @Override
    public void updateConnectionStatus()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startIBBLogoFlash()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void attachListeners(String projectID)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String setPageCompleteTargetProject(String projectName, String projectID)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updatePageState(String errorMessage)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updatePageComplete(String projectID)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateEnabled(String projectID)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public IProject getSourceProject(String name)
    {
        return null;
    }

    @Override
    public String getTargetProjectName(String name)
    {
        return null;
    }

    @Override
    public boolean overwriteResources(String key)
    {
        return false;
    }
}