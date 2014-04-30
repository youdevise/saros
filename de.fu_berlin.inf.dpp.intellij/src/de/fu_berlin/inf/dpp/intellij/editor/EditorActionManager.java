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

package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.ITextOperation;
import de.fu_berlin.inf.dpp.core.editor.internal.ILineRange;
import de.fu_berlin.inf.dpp.core.editor.internal.ITextSelection;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableEditorFileListener;
import de.fu_berlin.inf.dpp.intellij.editor.events.StoppableSelectionListener;
import org.apache.log4j.Logger;


/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 2014-04-18
 * Time: 15:49
 */

public class EditorActionManager
{
    private Logger log = Logger.getLogger(EditorActionManager.class);
    private EditorPool editorPool;
    private EditorAPI editorAPI;

    private EditorManager manager;

    private StoppableDocumentListener documentListener;
    private StoppableEditorFileListener fileListener;
    private StoppableSelectionListener selectionListener;

    public EditorActionManager(EditorManager manager)
    {
        this.editorPool = new EditorPool();
        this.editorAPI = new EditorAPI();
        this.manager = manager;

        this.documentListener = new StoppableDocumentListener(manager);

        this.fileListener = new StoppableEditorFileListener(manager);
        this.selectionListener = new StoppableSelectionListener(manager);

        if (this.editorAPI.editorFileManager != null)
        {
            this.editorAPI.editorFileManager.addFileEditorManagerListener(this.fileListener);
        }
    }


    public Editor openFile(SPath file)
    {

        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        if (virtualFile.exists())
        {
            // this.fileListener.setEnabled(false);

            if (editorAPI.isOpen(virtualFile))
            {
                Editor editor = editorAPI.openEditor(virtualFile);   //todo: need to activate only, not open!
                return  editor;
              //  return editorPool.getEditor(file);
              //  editorFileManager.setSelectedEditor(path,FileEditorProvider.getEditorTypeId());
            }
            else
            {
                Editor editor = editorAPI.openEditor(virtualFile);

//            Document pooledDoc = editorPool.getDocument(file);
//            if (pooledDoc != null)
//            {
//            //    pooledDoc.removeDocumentListener(documentListener);
//                editorPool.remove(file);
//            }

                // editor.getDocument().addDocumentListener(documentListener);
                documentListener.setDocument(editor.getDocument());
                startEditor(editor);


                editorPool.add(file, editor);

                //this.fileListener.setEnabled(true);

                return editor;
            }
        }
        else
        {
            log.warn("File not exist " + file);
        }

        return null;
    }

    public void startEditor(Editor editor)
    {
        editor.getSelectionModel().addSelectionListener(selectionListener);
    }

    public void stopEditor(Editor editor)
    {
        editor.getSelectionModel().removeSelectionListener(selectionListener);
    }

    public void closeFile(SPath file)
    {
        VirtualFile virtualFile = editorAPI.toVirtualFile(file);
        if (virtualFile.exists())
        {
            //   this.fileListener.setEnabled(false);
            if (editorAPI.isOpen(virtualFile))
            {
                Document doc = editorPool.getDocument(file);
                if (doc != null)
                {
                    // doc.removeDocumentListener(documentListener);
                    documentListener.setDocument(null);
                }


                editorAPI.closeEditor(virtualFile);
            }
            //editorPool.remove(virtualFile);

            //      this.fileListener.setEnabled(true);
        }
        else
        {
            log.warn("File not exist " + file);
        }


    }

    public void saveFile(SPath file)
    {
        Document doc = editorPool.getDocument(file);
        if (doc != null)
        {
            // this.fileListener.setEnabled(false);
            editorAPI.saveDocument(doc);
            // this.fileListener.setEnabled(true);
        }
        else
        {
            log.warn("Document not exist " + file);
        }
    }

    public void editText(SPath file, Operation operations)
    {

        Document doc = editorPool.getDocument(file);

         /*
         * Disable documentListener temporarily to avoid being notified of the
         * change
         */
        documentListener.setEnabled(false);
        for (ITextOperation op : operations.getTextOperations())
        {
            if (op instanceof DeleteOperation)
            {
                editorAPI.deleteText(doc, op.getPosition(), op.getPosition() + op.getTextLength());
            }
            else
            {
                editorAPI.insertText(doc, op.getPosition(), op.getText());
            }
        }

        documentListener.setEnabled(true);
    }

    public void selectText(SPath file, int position, int length)
    {
        Editor editor = editorPool.getEditor(file);
        if (editor != null)
        {
            editorAPI.setSelection(editor, position, position + length); //todo: calculate new line char win and unix differences

        }
    }

    public void setViewPort(final SPath file, final int lineStart, final int lineEnd)
    {
        Editor editor = editorPool.getEditor(file);
        if (editor != null)
        {
            editorAPI.setViewPort(editor, lineStart, lineEnd);
        }
    }

    /**
     * Adjusts viewport. Focus is set on the center of the range, but priority
     * is given to selected lines. Either range or selection can be null, but
     * not both.
     *
     * @param editor    Editor of the open Editor
     * @param range     viewport of the followed user. Can be <code>null</code>.
     * @param selection text selection of the followed user. Can be <code>null</code>.
     */
    public void adjustViewport(Editor editor, ILineRange range, ITextSelection selection)
    {
        if (editor == null)
        {
            return;
        }

        //todo
        editorAPI.setSelection(editor, selection.getOffset(), selection.getOffset() + selection.getLength());
        editorAPI.setViewPort(editor, range.getStartLine(), range.getStartLine() + range.getNumberOfLines());

       /* int lines = editor.getSelectionModel().getSelectionEndPosition().getLine()
                - editor.getSelectionModel().getSelectionStartPosition().getLine();
        int rangeTop = 0;
        int rangeBottom = 0;
        int selectionTop = 0;
        int selectionBottom = 0;

        if (selection != null)
        {
            try
            {
                selectionTop = editor.getSelectionModel().getSelectionStart();
                selectionBottom = editor.getSelectionModel().getSelectionEnd();
            }
            catch (Exception e)
            {
                // should never be reached
                log.error("Invalid line selection: offset: "
                        + selection.getOffset() + ", length: "
                        + selection.getLength());

                selection = null;
            }
        }

        if (range != null)
        {
            if (range.getStartLine() == -1)
            {
                range = null;
            }
            else
            {
                rangeTop = Math.min(lines - 1, range.getStartLine());
                rangeBottom = Math.min(lines - 1,
                        rangeTop + range.getNumberOfLines());
            }
        }

        if (range == null && selection == null)
        {
            return;
        }

        // top line of the new viewport
        int topPosition;
        int localLines = lines;
        int remoteLines = rangeBottom - rangeTop;
        int sizeDiff = remoteLines - localLines;

        // initializations finished

        if (range == null || selection == null)
        {
            topPosition = (rangeTop + rangeBottom + selectionTop + selectionBottom) / 2;
            editor.getScrollingModel() .setTopIndex(topPosition);

            return;
        }

        *//*
         * usually the viewport of the follower and the viewport of the followed
         * user will have the same center (this calculation). Exceptions may be
         * made below.
         *//*
        int center = (rangeTop + rangeBottom) / 2;
        topPosition = center - localLines / 2;

        if (sizeDiff <= 0)
        {
            // no further examination necessary when the local viewport is the
            // larger one
            viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
            return;
        }

        boolean selectionTopInvisible = (selectionTop < rangeTop + sizeDiff / 2);
        boolean selectionBottomInvisible = (selectionBottom > rangeBottom
                - sizeDiff / 2 - 1);

        if (rangeTop == 0
                && !(selectionTop <= rangeBottom && selectionTop > rangeBottom
                - sizeDiff))
        {
            // scrolled to the top and no selection at the bottom of range
            topPosition = 0;

        }
        else if (rangeBottom == lines - 1
                && !(selectionBottom >= rangeTop && selectionBottom < rangeTop
                + sizeDiff))
        {
            // scrolled to the bottom and no selection at the top of range
            topPosition = lines - localLines;

        }
        else if (selectionTopInvisible && selectionBottom >= rangeTop)
        {
            // making selection at top of range visible
            topPosition = Math.max(rangeTop, selectionTop);

        }
        else if (selectionBottomInvisible && selectionTop <= rangeBottom)
        {
            // making selection at bottom of range visible
            topPosition = Math.min(rangeBottom, selectionBottom) - localLines
                    + 1;
        }

        viewer.setTopIndex(Math.max(0, Math.min(topPosition, lines)));
   */
    }


    public SPath toPath(VirtualFile virtualFile)
    {
        if (virtualFile == null || !virtualFile.exists() || manager.sarosSession == null)
        {
            return null;
        }

        IResource resource = null;
        String path = virtualFile.getPath();

        for (IProject project : manager.sarosSession.getProjects())
        {
            resource = project.getFile(path);
            if (resource != null)
            {
                break;
            }

        }
        return resource == null ? null : new SPath(resource);
    }

    public EditorPool getEditorPool()
    {
        return editorPool;
    }


    public EditorAPI getEditorAPI()
    {
        return editorAPI;
    }
}