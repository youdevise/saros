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

package de.fu_berlin.inf.dpp.net.internal.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("SSRs")
public class SessionStatusResponseExtension extends SarosPacketExtension
{

    public static final Provider PROVIDER = new Provider();

    private boolean isInSession;
    private int participants;
    private String sessionDescription;

    public SessionStatusResponseExtension()
    {
        isInSession = false;
        participants = -1;
    }

    /**
     * Constructor for SessionStatusResponseExtension.
     *
     * @param participants       number of participants, server not counting
     * @param sessionDescription a string that describes the session, is shown to the receiver
     */
    public SessionStatusResponseExtension(int participants,
            String sessionDescription)
    {

        if (participants < 0)
        {
            throw new IllegalArgumentException(
                    "Invalid number of participants: " + participants);
        }
        if (sessionDescription == null)
        {
            throw new IllegalArgumentException("Session description is null");
        }

        isInSession = true;
        this.participants = participants;
        this.sessionDescription = sessionDescription;
    }

    public boolean isInSession()
    {
        return isInSession;
    }

    /**
     * @return a session description or <code>null</code> if no session is
     *         running
     */
    public String getSessionDescription()
    {
        return sessionDescription;
    }

    /**
     * @return number of participants, server not counting or -1 if no session
     *         is running
     */
    public int getNumberOfParticipants()
    {
        return participants;
    }

    public static class Provider extends
            SarosPacketExtension.Provider<SessionStatusResponseExtension>
    {
        private Provider()
        {
            super("sessionStatusResponse", SessionStatusResponseExtension.class);
        }
    }
}
