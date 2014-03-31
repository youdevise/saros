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

package de.fu_berlin.inf.dpp.core.versioning;

import java.util.StringTokenizer;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.25
 * Time: 13.48
 */

public class Version implements Comparable<Version>
{

    /**
     * Unique version instance representing an invalid version.
     */
    public static final Version INVALID = new Version(0, 0, 0, "invalid");

    private static final String SEPARATOR = ".";

    private final int major;

    private final int minor;

    private final int micro;

    private final String qualifier;

    private final String asString;

    private Version(final int major, final int minor, final int micro,
            final String qualifier)
    {

        if (major < 0 || minor < 0 || micro < 0)
        {
            throw new IllegalArgumentException(
                    "version contains negative numbers major: " + major
                            + " minor: " + minor + " micro: " + micro);
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = (qualifier == null) ? "" : qualifier;

        StringBuilder builder = new StringBuilder();
        builder.append(major);
        builder.append(SEPARATOR);
        builder.append(minor);
        builder.append(SEPARATOR);
        builder.append(micro);

        if (!this.qualifier.isEmpty())
        {
            builder.append(SEPARATOR);
            builder.append(qualifier);
        }

        this.asString = builder.toString();
    }

    /**
     * Parses a version identifier from the specified string. Identifier that
     * cannot be parsed will return the {@link #INVALID} version instance.
     *
     * @param version string representation of the version identifier
     * @return a Version object representing the version identifier
     */
    public static Version parseVersion(String version)
    {
        if (version == null)
        {
            throw new NullPointerException("version is null");
        }

        version = version.trim();

        int major = 0;
        int minor = 0;
        int micro = 0;
        String qualifier = "";

        if (version.isEmpty())
        {
            return INVALID;
        }

        StringTokenizer tokenizer = new StringTokenizer(version, SEPARATOR,
                true);

        parse:
        try
        {

            major = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreTokens())
            {
                break parse;
            }

            tokenizer.nextToken(); // delim
            minor = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreTokens())
            {
                break parse;
            }

            tokenizer.nextToken(); // delim
            micro = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreTokens())
            {
                break parse;
            }

            tokenizer.nextToken(); // delim
            qualifier = tokenizer.nextToken(""); // rest

        }
        catch (RuntimeException e)
        {
            return INVALID;
        }

        if (major < 0 || minor < 0 || micro < 0)
        {
            return INVALID;
        }

        return new Version(major, minor, micro, qualifier);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + micro;
        result = prime * result + minor;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Version other = (Version) obj;

        return this.compareTo(other) == 0;
    }

    @Override
    public int compareTo(Version other)
    {

        int result;

        result = major - other.major;

        if (result != 0)
        {
            return result;
        }

        result = minor - other.minor;

        if (result != 0)
        {
            return result;
        }

        result = micro - other.micro;

        return result;
    }

    @Override
    public String toString()
    {
        return asString;
    }
}
