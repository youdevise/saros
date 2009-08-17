/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager.Side;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO Review if SharedProject, ConcurrentDocumentManager, ActivitySequencer
 * all honor start() and stop() semantics.
 */
public class SharedProject implements ISharedProject, Disposable {

    public static final Logger log = Logger.getLogger(SharedProject.class);

    public static final int MAX_USERCOLORS = 5;

    /* Dependencies */
    protected Saros saros;

    protected ITransmitter transmitter;

    protected ActivitySequencer activitySequencer;

    protected ConcurrentDocumentManager concurrentDocumentManager;

    protected final List<IActivityProvider> activityProviders = new LinkedList<IActivityProvider>();

    protected DataTransferManager transferManager;

    protected IProject project;

    protected StopManager stopManager;

    /* Instance fields */
    protected User localUser;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    protected List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

    protected User host;

    protected FreeColors freeColors = null;

    protected Blockable stopManagerListener = new Blockable() {

        public void unblock() {
            // TODO see #block()
        }

        public void block() {
            // TODO find a way to effectively block the user from doing anything
        }
    };

    protected SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project,
        StopManager stopManager, JID myJID, int myColorID) {

        assert transmitter != null;

        assert myJID != null;

        this.saros = saros;
        this.transmitter = transmitter;
        this.project = project;
        this.transferManager = transferManager;
        this.stopManager = stopManager;

        this.localUser = new User(this, myJID, myColorID);
        this.activitySequencer = new ActivitySequencer(this, transmitter,
            transferManager);

        stopManager.addBlockable(stopManagerListener);
    }

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project, JID myID,
        StopManager stopManager) {

        this(saros, transmitter, transferManager, project, stopManager, myID, 0);

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);
        this.localUser.setUserRole(UserRole.DRIVER);
        this.host = localUser;

        this.participants.put(this.host.getJID(), this.host);

        /* add host to driver list. */
        this.concurrentDocumentManager = new ConcurrentDocumentManager(
            Side.HOST_SIDE, this.host, myID, this, activitySequencer);

    }

    /**
     * Constructor of client
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project, JID myID,
        JID hostID, int myColorID, StopManager stopManager) {

        this(saros, transmitter, transferManager, project, stopManager, myID,
            myColorID);

        this.host = new User(this, hostID, 0);
        this.host.setUserRole(UserRole.DRIVER);

        this.participants.put(hostID, host);
        this.participants.put(myID, localUser);

        this.concurrentDocumentManager = new ConcurrentDocumentManager(
            Side.CLIENT_SIDE, this.host, myID, this, activitySequencer);
    }

    public Collection<User> getParticipants() {
        return this.participants.values();
    }

    public List<User> getRemoteUsers() {
        Collection<User> users = getParticipants();
        List<User> result = new ArrayList<User>(users.size() - 1);
        for (User user : users) {
            if (user.isRemote()) {
                result.add(user);
            }
        }
        return result;
    }

    public ActivitySequencer getSequencer() {
        return this.activitySequencer;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * @throws CancellationException
     */
    public void initiateRoleChange(final User user, final UserRole newRole,
        SubMonitor progress) throws CancellationException, InterruptedException {
        assert localUser.isHost() : "Only the host can initiate role changes.";

        if (user.isHost()) {
            activityCreated(new RoleActivity(
                getLocalUser().getJID().toString(), user.getJID().toString(),
                newRole));

            setUserRole(user, newRole);
        } else {
            StartHandle startHandle = stopManager.stop(user,
                "Performing role change", progress);

            activityCreated(new RoleActivity(
                getLocalUser().getJID().toString(), user.getJID().toString(),
                newRole));

            setUserRole(user, newRole);

            if (!startHandle.start())
                log
                    .error("Didn't unblock. There still exist unstarted StartHandles.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setUserRole(final User user, final UserRole role) {

        assert user != null;
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                user.setUserRole(role);

                log.info("User " + user + " is now a " + role);

                for (ISharedProjectListener listener : SharedProject.this.listeners) {
                    listener.roleChanged(user);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getHost() {
        return this.host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public boolean isHost() {
        return this.host.equals(localUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        return localUser.isDriver();
    }

    public boolean isExclusiveDriver() {
        if (!isDriver()) {
            return false;
        }
        for (User user : getParticipants()) {
            if (user.isRemote() && user.isDriver()) {
                return false;
            }
        }
        return true;
    }

    public void addUser(User user) {

        assert user.getSharedProject().equals(this);

        JID jid = user.getJID();
        if (this.participants.containsKey(jid)) {
            log.warn("User " + jid + " added twice to SharedProject");
        }
        participants.putIfAbsent(jid, user);
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }
        SharedProject.log.info("User " + jid + " joined session");
    }

    public void removeUser(User user) {
        JID jid = user.getJID();
        if (this.participants.remove(jid) == null) {
            log
                .warn("Tried to remove user who was not in participants: "
                    + jid);
            return;
        }
        if (isHost()) {
            returnColor(user.getColorID());
        }

        this.activitySequencer.userLeft(jid);

        // TODO what is to do here if no driver exists anymore?

        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user);
        }

        SharedProject.log.info("User " + jid + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        this.listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IProject getProject() {
        return this.project;
    }

    public Thread requestTransmitter = null;

    public void start() {
        if (!stopped) {
            throw new IllegalStateException();
        }

        activitySequencer.start();

        stopped = false;

    }

    // TODO Review sendRequest for InterruptedException and remove this flag.
    boolean stopped = true;

    /**
     * Stops the associated activity sequencer.
     * 
     * @throws IllegalStateException
     *             if the shared project is already stopped.
     */
    public void stop() {
        if (stopped) {
            throw new IllegalStateException();
        }

        activitySequencer.stop();

        stopped = true;
    }

    public void dispose() {
        stopManager.removeBlockable(stopManagerListener);
        concurrentDocumentManager.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        if (jid.isBareJID()) {
            throw new IllegalArgumentException(
                "JIDs used for the SharedProject should always be resource qualified: "
                    + jid);
        }

        User user = this.participants.get(jid);

        if (user == null || !user.getJID().strictlyEquals(jid))
            return null;

        return user;
    }

    /**
     * Given a JID (with resource or not), will return the resource qualified
     * JID associated with this user or null if no user for the given JID exists
     * in this SharedProject.
     */
    public JID getResourceQualifiedJID(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        User user = this.participants.get(jid);

        if (user == null)
            return null;

        return user.getJID();
    }

    public User getLocalUser() {
        return localUser;
    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return concurrentDocumentManager;
    }

    public ITransmitter getTransmitter() {
        return transmitter;
    }

    public Saros getSaros() {
        return saros;
    }

    public int getFreeColor() {
        return freeColors.get();
    }

    public void returnColor(int colorID) {
        freeColors.add(colorID);
    }

    public void execTransformedActivity(TextEditActivity activity) {

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        assert Util.isSWT();

        try {
            log.debug("Executing   transformed activity: " + activity);

            for (IActivityProvider exec : this.activityProviders) {
                exec.exec(activity);
            }

            /*
             * FIXME The following will send the activities to everybody, so all
             * drivers will receive the message twice (once through Jupiter once
             * as a Activity)
             */

            // Send activity to every remote user.
            if (this.concurrentDocumentManager.isHostSide()) {
                activitySequencer.sendActivity(getRemoteUsers(), activity);
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }
    }

    public void exec(List<IActivity> activities) {
        // TODO Check all this for problems with concurrency.

        final List<IActivity> stillToExecute = concurrentDocumentManager
            .exec(activities);
        if (stillToExecute.isEmpty()) {
            return;
        }

        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                for (IActivity activity : stillToExecute) {
                    for (IActivityProvider executor : activityProviders) {
                        executor.exec(activity);
                    }
                }
            }
        });
    }

    public void activityCreated(IActivity activity) {

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        /* Let ConcurrentDocumentManager have a look at the activities first */
        boolean consumed = this.concurrentDocumentManager
            .activityCreated(activity);

        if (!consumed) {
            activitySequencer.sendActivity(getRemoteUsers(), activity);
        }
    }

    public void addActivityProvider(IActivityProvider provider) {
        if (!activityProviders.contains(provider)) {
            this.activityProviders.add(provider);
            provider.addActivityListener(this);
        }
    }

    public void removeActivityProvider(IActivityProvider provider) {
        this.activityProviders.remove(provider);
        provider.removeActivityListener(this);
    }
}
