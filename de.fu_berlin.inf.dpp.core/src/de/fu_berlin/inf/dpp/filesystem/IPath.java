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

package de.fu_berlin.inf.dpp.filesystem;

import java.io.File;

/**
 * This interface is under development. It currently equals its Eclipse
 * counterpart. If not mentioned otherwise all offered methods are equivalent to
 * their Eclipse counterpart.
 */
public interface IPath {

    public IPath append(IPath path);

    public String lastSegment();

    public boolean hasTrailingSeparator();

    public boolean isPrefixOf(IPath path);

    public int segmentCount();

    public IPath removeFirstSegments(int count);

    public IPath removeLastSegments(int count);

    public boolean isEmpty();

    public String[] segments();

    public IPath append(String path);

    public IPath addTrailingSeparator();

    public IPath addFileExtension(String extension);

    public IPath removeFileExtension();

    public IPath makeAbsolute();

    public boolean isAbsolute();

    public String toPortableString();

    public String toOSString();

    public File toFile();

    public String getFileExtension();

}
