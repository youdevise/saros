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

package de.fu_berlin.inf.dpp.intellij.project.intl;

import de.fu_berlin.inf.dpp.core.exceptions.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.project.ISchedulingRoot;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspace;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceDescription;
import de.fu_berlin.inf.dpp.core.workspace.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.filesystem.IPathFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 11.03
 * //todo
 */

public class WorkspaceIntl implements IWorkspace
{
    public static final Logger log = Logger.getLogger(WorkspaceIntl.class);
    private static WorkspaceIntl _instance;

    private SchedulingRootIntl root;
    private File path;

    private WorkspaceIntl()
    {

    }

    public static WorkspaceIntl instance()
    {
        if (_instance == null)
        {
            _instance = new WorkspaceIntl();
        }

        return _instance;
    }

    private IWorkspaceDescription description = new WorkspaceDescription();

    @Override
    public void run(final IWorkspaceRunnable procedure, IProgressMonitor monitor) throws OperationCanceledException, IOException
    {
        System.out.println("WorkspaceIntl.run 1 //todo");

        //  new WorkspaceThread(procedure,monitor).start();
        try
        {
            procedure.run(monitor);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run(IWorkspaceRunnable procedure, ISchedulingRoot root, int mode, IProgressMonitor monitor)
    {
        System.out.println("WorkspaceIntl.run 2 //todo");

        // new WorkspaceThread(procedure,monitor).start(); //todo
        try
        {
            procedure.run(monitor);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ISchedulingRoot getRoot()
    {
        if (root == null)
        {
            throw new RuntimeException("WorkspaceIntl not initialized!");
        }

        return root;
    }

    public IWorkspaceDescription getDescription()
    {
        return description;
    }

    public void setDescription(IWorkspaceDescription description)
    {
        this.description = description;
    }

    public void createWorkSpace(File path) throws Exception
    {
        this.root = new SchedulingRootIntl(path);
        this.path = path;

        log.info("Add workspace " + path.getAbsolutePath());
        for (File prj : path.listFiles())
        {
            if (prj.isDirectory())
            {

                root.addProject(prj.getName(), prj);
            }
        }
    }

    public File getPath()
    {
        return path;
    }

    @Override
    public IPathFactory getPathFactory()
    {
        return new PathFactoryIntl();
    }
}