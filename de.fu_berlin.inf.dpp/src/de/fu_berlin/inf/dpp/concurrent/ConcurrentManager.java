package de.fu_berlin.inf.dpp.concurrent;

import java.util.List;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Interface for management controller class of all jupiter document server.
 * @author orieger
 *
 */
public interface ConcurrentManager {

	public static enum Side{
		CLIENT_SIDE,
		HOST_SIDE
	}
	
	public void addDriver(User jid);
	
	public void removeDriver(User jid);
	
	public List<User> getDriver();
	
	public boolean isDriver(User jid);
	
	public boolean isHost();
	
	public void setHost(User jid);
	
	public IActivity activityCreated(IActivity activity);
	
	public IActivity exec(IActivity activity);
}
