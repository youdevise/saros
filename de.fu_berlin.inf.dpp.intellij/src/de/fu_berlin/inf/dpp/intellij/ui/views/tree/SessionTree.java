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

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.2
 * Time: 08.53
 */

public class SessionTree extends AbstractTree implements ISarosSessionListener
{
    public static final String TREE_TITLE = "Sessions";
    public static final String TREE_TITLE_NO_SESSIONS = "No Sessions Running";

    private RootTree rootTree;
    private List<DefaultMutableTreeNode> sessionNodeList = new ArrayList<DefaultMutableTreeNode>();

    /**
     * @param parent
     */
    public SessionTree(RootTree parent)
    {
        super(parent);
        this.rootTree = parent;

        setUserObject(new CategoryInfo(TREE_TITLE_NO_SESSIONS));

        create();

        //register listener
        saros.getSessionManager().addSarosSessionListener(this);
    }

    protected void create()
    {

    }

    public CategoryInfo getUserObject()
    {
        return (CategoryInfo) super.getUserObject();
    }

    public void setTitle(String title)
    {
        getUserObject().title = title;
    }

    @Override
    public void preIncomingInvitationCompleted(IProgressMonitor monitor)
    {

    }

    @Override
    public void postOutgoingInvitationCompleted(IProgressMonitor monitor, User user)
    {

    }

    @Override
    public void sessionStarting(ISarosSession newSarosSession)
    {

    }

    @Override
    public void sessionStarted(ISarosSession newSarosSession)
    {

        DefaultMutableTreeNode nSession = new DefaultMutableTreeNode(new SessionInfo(newSarosSession));

        this.sessionNodeList.add(nSession);

        add(nSession);

        setTitle(TREE_TITLE);

        UIUtil.invokeAndWaitIfNeeded(new Runnable()
        {
            @Override
            public void run()
            {
                rootTree.getJtree().expandRow(1);
            }
        });


    }

    @Override
    public void sessionEnding(ISarosSession oldSarosSession)
    {

    }

    @Override
    public void sessionEnded(ISarosSession oldSarosSession)
    {

        DefaultMutableTreeNode node;

        boolean empty = true;
        for (int i = 0; i < getChildCount(); i++)
        {
            node = (DefaultMutableTreeNode) getChildAt(i);
            SessionInfo sessionInfo = ((SessionInfo) node.getUserObject());
            if (sessionInfo.getSession().getID().equalsIgnoreCase(oldSarosSession.getID()))
            {
                remove(node);
                sessionNodeList.remove(node);
            }
            else
            {
                empty = false;
            }
        }

        if (empty)
        {
            setTitle(TREE_TITLE_NO_SESSIONS);
        }

        SwingUtilities.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                rootTree.getJtree().expandRow(1);
                rootTree.getJtree().collapseRow(1);
            }
        });

    }

    @Override
    public void projectAdded(String projectID)
    {

        //iterate projects in sessions
        for (DefaultMutableTreeNode nSession : sessionNodeList)
        {
            IProject p = ((SessionInfo) nSession.getUserObject()).getSession().getProject(projectID);
            DefaultMutableTreeNode nProject = new DefaultMutableTreeNode(new ProjectInfo(p));
            nSession.add(nProject);

            //todo: expand node
//            for (TreeNode path : nSession.getPath())
//            {
//
//                rootTree.getJtree().collapsePath(new TreePath(path));
//                rootTree.getJtree().expandPath(new TreePath(path));
//            }
        }


    }

    /**
     * Class to keep session information
     */
    protected class SessionInfo extends LeafInfo
    {
        private ISarosSession session;

        private SessionInfo(ISarosSession session)
        {
            super(session.getID(), session.getHost().getShortHumanReadableName());
            this.session = session;
        }

        public ImageIcon getIcon()
        {
            return IconManager.contactOnlineIcon;
        }

        public ISarosSession getSession()
        {
            return session;
        }

    }

    /**
     *
     */
    protected class ProjectInfo extends LeafInfo
    {
        private IProject project;

        public ProjectInfo(IProject project)
        {
            super(project.getFullPath().toString(), project.getName());
            this.project = project;
        }

        public IProject getProject()
        {
            return project;
        }
    }
}
