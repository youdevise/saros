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

package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.vfs.*;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.core.exception.OperationCanceledException;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.*;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.events.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.PathImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.Workspace;
import de.fu_berlin.inf.dpp.session.User;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-05-19
 * Time: 16:45
 */

public class FileSystemChangeListener extends AbstractStoppableListener implements VirtualFileListener {

    private static Logger log = Logger.getLogger(FileSystemChangeListener.class);
    private SharedResourcesManager resourceManager;
    private Workspace workspace;
    private List<File> incomingList = new ArrayList<File>();

    private EditorManager editorManager;

    public FileSystemChangeListener(SharedResourcesManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    private void generateFolderMove(SPath oldSPath, SPath newSPath, boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        ProjectImp project = (ProjectImp) oldSPath.getProject();
        IActivity createActivity = new FolderActivity(user, FolderActivity.Type.CREATED, newSPath);
        resourceManager.fireActivityInternal(createActivity);

        IFolder folder = before ? oldSPath.getFolder() : newSPath.getFolder();

        IResource[] members = new IResource[0];
        try {
            members = folder.members();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new OperationCanceledException("Internal I/O error");
        }

        for (IResource resource : members) {
            SPath oldChildSPath = new FileImp((ProjectImp) oldSPath.getProject(), new File(oldSPath.getFullPath().toOSString() + File.separator + resource.getName())).getSPath();
            SPath newChildSPath = new FileImp((ProjectImp) newSPath.getProject(), new File(newSPath.getFullPath().toOSString() + File.separator + resource.getName())).getSPath();
            if (resource.getType() == IResource.FOLDER) {
                generateFolderMove(oldChildSPath, newChildSPath, before);
            } else {
                generateFileMove(oldChildSPath, newChildSPath, before);
            }
        }

        IActivity removeActivity = new FolderActivity(user, FolderActivity.Type.REMOVED, oldSPath);
        resourceManager.fireActivityInternal(removeActivity);

        project.addFile(newSPath.getFile().toFile());
        project.removeFile(oldSPath.getFile().toFile());
    }


    private void generateFileMove(SPath oldSPath, SPath newSPath, boolean before) {
        User user = resourceManager.getSession().getLocalUser();
        ProjectImp project = (ProjectImp) oldSPath.getProject();

        IFile file=null;

        try {
            if (before) {

                file = project.getFile(oldSPath.getFullPath());
                editorManager.saveLazy(oldSPath);
            } else {

                file = project.getFile(newSPath.getFullPath());
                editorManager.saveLazy(newSPath);
            }
        } catch (FileNotFoundException e) {
           log.error("Could not save file ",e);
        }

        if (file == null)
            return;


        byte[] bytes = FileUtils.getLocalFileContent(file);

        IActivity activity = new FileActivity(user, FileActivity.Type.MOVED, newSPath, oldSPath, bytes, FileActivity.Purpose.ACTIVITY);
        editorManager.getActionManager().getEditorPool().replaceAll(oldSPath, newSPath);
        project.addFile(newSPath.getFile().toFile());
        project.removeFile(oldSPath.getFile().toFile());
        resourceManager.fireActivityInternal(activity);
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {


    }


    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = new File(virtualFileEvent.getFile().getPath());
        IPath path = new PathImp(file);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (incomingList.contains(file)) {
            incomingList.remove(file);

            ((ProjectImp) project).addFile(file);

            return;
        }


        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getFullPath().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;
        if (file.isFile()) {
            byte[] bytes = new byte[0];
            try {
                bytes = virtualFileEvent.getFile().contentsToByteArray();
            } catch (IOException e) {
                workspace.log.error(e.getMessage(), e);
            }

            activity = FileActivity.created(user, spath, bytes, FileActivity.Purpose.ACTIVITY);
            editorManager.getActionManager().registerNewFile(virtualFileEvent.getFile(), bytes);

        } else {
            activity = new FolderActivity(user, FolderActivity.Type.CREATED, spath);

        }

        ((ProjectImp) project).addFile(file);


        resourceManager.fireActivityInternal(activity);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        if (!enabled) {
            return;
        }

        File file = new File(virtualFileEvent.getFile().getPath());
        if (incomingList.contains(file)) {
            incomingList.remove(file);
            return;
        }


        IPath path = new PathImp(file);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getFullPath().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);
        User user = resourceManager.getSession().getLocalUser();

        IActivity activity;
        if (virtualFileEvent.getFile().isDirectory()) {
            activity = new FolderActivity(user, FolderActivity.Type.REMOVED, spath);
        } else {
            activity = FileActivity.removed(user, spath, FileActivity.Purpose.ACTIVITY);
        }

        ((ProjectImp) project).removeFile(file);
        editorManager.getActionManager().getEditorPool().removeAll(spath);

        resourceManager.fireActivityInternal(activity);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        if (!enabled) {
            return;
        }

        File newFile = new File(virtualFileMoveEvent.getFile().getPath());
        if (incomingList.contains(newFile)) {
            incomingList.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getFullPath().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath newSPath = new SPath(project, path);

        IPath oldPath = new PathImp(new File(virtualFileMoveEvent.getOldParent() + File.separator + virtualFileMoveEvent.getFileName()));
        oldPath = oldPath.removeFirstSegments(projSegmentCount);

        SPath oldSPath = new SPath(project, oldPath);

        //  User user = resourceManager.getSession().getLocalUser();

        //move activity
        if (newFile.isFile()) {
           /* byte[] bytes = new byte[0];
            try {
                bytes = virtualFileMoveEvent.getFile().contentsToByteArray();
            } catch (IOException e) {
                workspace.log.error(e.getMessage(), e);
            }

            IActivity activity = new FileActivity(user, FileActivity.Type.MOVED, newSPath, oldSPath, bytes, FileActivity.Purpose.ACTIVITY);
            editorManager.getActionManager().getEditorPool().removeAll(oldSPath);
            resourceManager.fireActivity(activity);*/
            generateFileMove(oldSPath, newSPath, false);

        } else {
            generateFolderMove(oldSPath, newSPath, false);
        }

//        ((ProjectImp) project).removeFile(oldPath.toFile());
//        ((ProjectImp) project).addFile(newFile);
//        editorManager.getActionManager().getEditorPool().removeAll(newSPath);

    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent filePropertyEvent) {
        if (!enabled) {
            return;
        }

        File oldFile = new File(filePropertyEvent.getFile().getParent().getPath() + File.separator + filePropertyEvent.getOldValue());
        File newFile = new File(filePropertyEvent.getFile().getPath());

        if (incomingList.contains(newFile)) {
            incomingList.remove(newFile);
            return;
        }

        IPath oldPath = new PathImp(oldFile);
        IProject project = workspace.getRoot().locateProject(oldPath);

        if (project == null || !project.exists()) {
            return;
        }

        int projSegmentCount = project.getFullPath().segments().length;

        oldPath = oldPath.removeFirstSegments(projSegmentCount);
        SPath oldSPath = new SPath(project, oldPath);

        IPath newPath = new PathImp(newFile);
        newPath = newPath.removeFirstSegments(projSegmentCount);

        SPath newSPath = new SPath(project, newPath);
        //move activity
        if (newFile.isFile()) {
            generateFileMove(oldSPath, newSPath, false);
        } else {
            generateFolderMove(oldSPath, newSPath, false);
        }
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
        if (!enabled) {
            return;
        }

        File newFile = new File(virtualFileCopyEvent.getFile().getPath());
        if (incomingList.contains(newFile)) {
            incomingList.remove(newFile);
            return;
        }

        IPath path = new PathImp(newFile);
        IProject project = workspace.getRoot().locateProject(path);

        if (project == null || !project.exists()) {
            return;
        }

        if (!resourceManager.getSession().isCompletelyShared(project)) {
            return;
        }

        int projSegmentCount = project.getFullPath().segments().length;
        path = path.removeFirstSegments(projSegmentCount);

        SPath spath = new SPath(project, path);


        User user = resourceManager.getSession().getLocalUser();
        IActivity activity;

        byte[] bytes = new byte[0];
        try {
            bytes = virtualFileCopyEvent.getOriginalFile().contentsToByteArray();
        } catch (IOException e) {
            workspace.log.error(e.getMessage(), e);
        }

        activity = FileActivity.created(user, spath, bytes, FileActivity.Purpose.ACTIVITY);

        ((ProjectImp) project).addFile(newFile);

        resourceManager.fireActivityInternal(activity);
    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent filePropertyEvent) {

    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public void addIncoming(File file) {
        incomingList.add(file);
    }

    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
    }
}