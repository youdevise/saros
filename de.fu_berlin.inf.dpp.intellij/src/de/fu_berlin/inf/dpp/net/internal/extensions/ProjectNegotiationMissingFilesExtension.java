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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.fu_berlin.inf.dpp.invitation.FileList;

@XStreamAlias(/* ProjectNegotiationMissingFiles */"PNMF")
public class ProjectNegotiationMissingFilesExtension extends
        ProjectNegotiationExtension
{

    public static final Provider PROVIDER = new Provider();

    private final List<FileList> fileLists;

    public ProjectNegotiationMissingFilesExtension(String sessionID,
            String negotiationID, List<FileList> fileLists)
    {
        super(sessionID, negotiationID);
        this.fileLists = fileLists;
    }

    public List<FileList> getFileLists()
    {
        return fileLists;
    }

    public static class Provider
            extends
            ProjectNegotiationExtension.Provider<ProjectNegotiationMissingFilesExtension>
    {

        private Provider()
        {
            super("pnmf", ProjectNegotiationMissingFilesExtension.class,
                    FileList.class);
        }
    }
}
