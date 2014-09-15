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

package de.fu_berlin.inf.dpp.intellij.ui.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.context.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.core.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.fs.FileImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.FolderImp;
import de.fu_berlin.inf.dpp.intellij.project.fs.ProjectImp;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Saros action group for the pop-up menu when right-clicking on a module.
 */
public class SarosFileShareGroup extends ActionGroup {
    private static final Logger LOG = Logger.getLogger(SarosFileShareGroup.class);

    @Inject
    private Saros saros;

    @Inject
    private ISarosSessionManager sessionManager;



    public void actionPerformed(AnActionEvent e) {
        //do nothing when menu pops-up
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        //the object has to be initialized here, because it is created before
        //{@link de.fu_berlin.inf.dpp.core.Saros}.
        SarosPluginContext.initComponent(this);

        if (e == null || !Saros.isInitialized()
                || sessionManager.getSarosSession() != null) {
            return new AnAction[0];
        }

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (virtualFile == null || project == null) {
            return new AnAction[0];
        }

        List<AnAction> list = new ArrayList<AnAction>();
        for (JID user : saros.getMainPanel().getSarosTree().getContactTree().getOnLineUsers()) {
            list.add(new ShareWithUserAction(user));
        }

        return list.toArray(new AnAction[]{});
    }


    public class ShareWithUserAction extends AnAction {

        private final JID userJID;
        private final String title;

        public ShareWithUserAction(JID user) {
            super(user.getName(), null, IconManager.CONTACT_ONLINE_ICON);
            userJID = user;
            title = user.getName();
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            VirtualFile virtFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtFile == null) {
                return;
            }

            File file = new File(virtFile.getPath());
            try {
                Module module = ProjectFileIndex.SERVICE.getInstance(e.getProject()).getModuleForFile(virtFile);
                String moduleName = null;
                if (module != null) {
                    moduleName = module.getName();
                } else {
                    //FIXME: Fix for non-module IDEAs
                }
                ProjectImp project = new ProjectImp(e.getProject(), moduleName, file);

                List<IResource> resources = new ArrayList<IResource>();

                if (virtFile.isDirectory()) {
                    //check if it is a completely shared project
                    FolderImp resFolder = new FolderImp(project, file);
                    if (resFolder.getFullPath().segmentCount() == project.getFullPath().segmentCount()) {
                        //fully shared
                        project.refreshLocal();
                        resources.add(project);
                    } else {
                        //partially shared
                        resources.addAll(getParentFolders(project, project.getFullPath(), resFolder.getFullPath()));
                        loadChildResources(project, resFolder.toFile(), resources);
                    }

                } else {
                    FileImp resFile = new FileImp(project, file);
                    resources.addAll(getParentFolders(project, project.getFullPath(), resFile.getFullPath()));
                    resources.add(resFile);
                }

                List<JID> contacts = Arrays.asList(userJID);

                CollaborationUtils.startSession(resources, contacts);
            } catch (IOException e1) {
                LOG.error("could not share project with user due to", e1);
            }
        }


        /**
         * Loads all resources under file into resources by traversing the
         * directory tree..
         *
         */
        private void loadChildResources(ProjectImp project, @NotNull File file, List<IResource> resources) {
            if (file.isDirectory()) {
                resources.add(new FolderImp(project, file));
                for (File f : getSafeFileList(file)) {
                    loadChildResources(project, f, resources);
                }
            } else {
                resources.add(new FileImp(project, file));
            }
        }

        private File[] getSafeFileList(File file) {
            File[] files = file.listFiles();
            return files != null ? files : new File[0];
        }

        /**
         * Load parent folders of file, for partially shared files.
         */
        private List<IResource> getParentFolders(ProjectImp project, IPath top, IPath bottom) {
            List<IResource> folders = new ArrayList<IResource>();
            File base = top.toFile();
            StringBuilder sbPath = new StringBuilder(base.getAbsolutePath());
            String[] segments = bottom.segments();
            for (int i = top.segmentCount(); i < segments.length - 1; i++) {
                sbPath.append(File.separator);
                sbPath.append(segments[i]);
                IFolder folder = new FolderImp(project, new File(sbPath.toString()));
                folders.add(folder);
            }

            return folders;
        }

        public String toString() {
            return super.toString() + " " + title;
        }
    }
}
