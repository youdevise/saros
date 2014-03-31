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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public abstract class InvitationExtension extends SarosPacketExtension
{

    @XStreamAlias("nid")
    @XStreamAsAttribute
    final protected String negotiationID;

    public InvitationExtension(String negotiationID)
    {
        this.negotiationID = negotiationID;
    }

    public String getNegotiationID()
    {
        return negotiationID;
    }

    public abstract static class Provider<T extends InvitationExtension>
            extends SarosPacketExtension.Provider<T>
    {

        public Provider(String elementName, Class<?>... classes)
        {
            super(elementName, classes);
        }


        public PacketFilter getPacketFilter(final String invitationID)
        {

            return new AndFilter(super.getPacketFilter(), new PacketFilter()
            {
                @Override
                public boolean accept(Packet packet)
                {
                    InvitationExtension extension = getPayload(packet);

                    if (extension == null)
                    {
                        return false;
                    }

                    return invitationID.equals(extension.getNegotiationID());
                }
            });
        }
    }
}
