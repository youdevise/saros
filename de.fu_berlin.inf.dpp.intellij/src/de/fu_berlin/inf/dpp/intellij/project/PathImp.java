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

import de.fu_berlin.inf.dpp.filesystem.IPath;

import java.io.File;
import java.util.Arrays;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.4.4
 * Time: 11.41
 */

public class PathImp implements IPath
{
    public static final String FILE_SEPARATOR = "/";

    private String _path;

    public PathImp(String path)
    {
       this._path = path;
       _path = toPortableString();
    }

    public PathImp(File file)
    {
        this._path = file.getPath();
        _path = toPortableString();
    }
    
    @Override
    public IPath append(IPath path)
    {
        return _path.endsWith(FILE_SEPARATOR) ? new PathImp(_path+path.toPortableString())
                : new PathImp(_path+FILE_SEPARATOR+ path.toPortableString());
    }

    @Override
    public String lastSegment()
    {
        String[] segments = _path.split(FILE_SEPARATOR);
        return segments[segments.length-1];
    }

    @Override
    public boolean hasTrailingSeparator()
    {
        return _path.endsWith(FILE_SEPARATOR);
    }

    @Override
    public boolean isPrefixOf(IPath path)
    {
        return path.toString().startsWith(_path);
    }

    @Override
    public int segmentCount()
    {
        return _path.split(FILE_SEPARATOR).length;
    }

    @Override
    public IPath removeLastSegments(int count)
    {
        String[] segments = _path.split(FILE_SEPARATOR);
        segments=Arrays.copyOf(segments,segments.length-count);

        return new PathImp(join(segments));
    }

    @Override
    public boolean isEmpty()
    {
        return new File(_path).exists();
    }

    @Override
    public String[] segments()
    {
        return _path.split(FILE_SEPARATOR);
    }

    @Override
    public IPath append(String path)
    {
        return new PathImp(_path.endsWith(FILE_SEPARATOR) ? _path+path:_path+FILE_SEPARATOR+path);
    }

    @Override
    public IPath addTrailingSeparator()
    {
        return _path.endsWith(FILE_SEPARATOR)?new PathImp(_path): new PathImp(_path+FILE_SEPARATOR);

    }

    @Override
    public IPath addFileExtension(String extension)
    {
        return new PathImp(_path+"."+extension);
    }

    @Override
    public IPath removeFileExtension()
    {
        String path=_path;
        if(path.contains("."))
        {
           path = path.substring(0,path.lastIndexOf("."));
        }
        return new PathImp(path);
    }

    @Override
    public IPath makeAbsolute()
    {
        return new PathImp(new File(_path).getAbsolutePath());
    }

    @Override
    public boolean isAbsolute()
    {
        return new File(_path).isAbsolute();
    }

    @Override
    public String toPortableString()
    {
        final String ts = "\\\\";

       String path = _path;

        if(path.contains(ts))
        {
           path = path.replaceAll(ts, FILE_SEPARATOR);
        }
        return path;
    }

    @Override
    public String toOSString()
    {
        return new File(_path).getPath();
    }

    @Override
    public File toFile()
    {
        return new File(_path);
    }

    private String join (String ... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            if (i >= data.length-1) {break;}
            sb.append(FILE_SEPARATOR);
        }
        return sb.toString();
    }
}