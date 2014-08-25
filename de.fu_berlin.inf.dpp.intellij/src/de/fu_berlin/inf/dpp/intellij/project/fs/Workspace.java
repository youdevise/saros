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

package de.fu_berlin.inf.dpp.intellij.project.fs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.IWorkspaceRunnable;
import de.fu_berlin.inf.dpp.intellij.project.FileSystemChangeListener;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Workspace implements IWorkspace {
    public static final Logger LOG = Logger.getLogger(Workspace.class);
    private WorkspaceRoot root;

    private LocalFileSystem fileSystem;

    private File path;
    private Project project;

    /**
     * @deprecated use for testing only
     */
    public Workspace() {
    }

    public Workspace(File basePath) {
        initPath(basePath);
    }

    public Workspace(String basePath) {
        this(new File(basePath));
    }

    public Workspace(Project project) {
        this.project = project;
        this.fileSystem = LocalFileSystem.getInstance();
        this.fileSystem.addRootToWatch(project.getBasePath(), true);
        //initPath(new File(project.getBasePath()));
        createWorkSpace(new File(project.getBasePath()));
    }

    @Override public void run(IWorkspaceRunnable procedure) throws IOException {
        procedure.run(new NullProgressMonitor());
    }

    @Override public IProject getProject(String project) {
        //TODO implement this
        return null;
    }

    @Override
    public IPath getLocation() {
        //TODO implement this
        return null;
    }

    /**
     * @param path
     * @deprecated use for tests only
     */
    public void createWorkSpace(File path) {
        this.root = new WorkspaceRoot(project, path);
        this.path = path;

    }

    protected void initPath(File path) {
        this.root = new WorkspaceRoot(project, path);
        this.path = path;
    }

    public void addResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.addVirtualFileListener(listener);
    }

    public void removeResourceListener(FileSystemChangeListener listener) {
        listener.setWorkspace(this);
        fileSystem.removeVirtualFileListener(listener);
    }

    public ProjectImp getProjectForPath(String path) {
        //FIXME: implement this
        return null;
    }
}
