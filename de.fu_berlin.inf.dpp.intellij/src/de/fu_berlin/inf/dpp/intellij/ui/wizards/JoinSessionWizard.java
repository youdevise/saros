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

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

/**
 * Created by:  r.kvietkauskas@uniplicity.com
 * <p/>
 * Date: 14.3.27
 * Time: 08.51
 */

import de.fu_berlin.inf.dpp.core.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.core.invitation.ProcessTools;
import de.fu_berlin.inf.dpp.core.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.core.monitor.IProgressMonitor;
import de.fu_berlin.inf.dpp.core.ui.IJoinSession;
import de.fu_berlin.inf.dpp.core.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.core.Saros;
import de.fu_berlin.inf.dpp.intellij.ui.eclipse.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.core.Wizard;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.InfoPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.ProgressPage;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.text.MessageFormat;


/**
 * A wizard that guides the user through an incoming invitation process.
 * <p/>
 * TODO Automatically switch to follow mode
 * <p/>
 * more nicely: Long-Running Operation after each step, cancellation by a remote
 * party, auto-advance.
 */
public class JoinSessionWizard implements IJoinSession
{
    public static final String PAGE_INFO_ID = "JoinSessionInfo";
    public static final String PAGE_PROGRESS_ID = "JoinSessionProgress";

    private static Container parent = Saros.instance().getMainPanel();

    private static final Logger log = Logger.getLogger(JoinSessionWizard.class);

    private boolean accepted = false;

    private IncomingSessionNegotiation process;


    private SessionNegotiation.Status invitationStatus;

    private ProgressPage progressPage;
    private Wizard wizard;

    private PageActionListener actionListener = new PageActionListener()
    {
        @Override
        public void back()
        {

        }

        @Override
        public void next()
        {
            performFinish();
        }

        @Override
        public void cancel()
        {
            performCancel();
        }
    };

    /**
     * Creates wizard UI
     *
     * @param process
     */
    public JoinSessionWizard(IncomingSessionNegotiation process)
    {
        this.process = process;

        wizard = new Wizard(Messages.JoinSessionWizard_title);
        wizard.getNavigationPanel().setBackButton(null);

        wizard.setHeadPanel(new HeaderPanel(Messages.ShowDescriptionPage_title2, Messages.ShowDescriptionPage_description));

        InfoPage infoPage = new InfoPage(PAGE_INFO_ID);
        infoPage.addText(process.getPeer().getName() + " " + Messages.JoinSessionWizard_info);
        infoPage.addText(process.getDescription());
        infoPage.addPageListener(actionListener);
        infoPage.setNextButtonTitle(Messages.JoinSessionWizard_accept);

        wizard.registerPage(infoPage);

        this.progressPage = new ProgressPage(PAGE_PROGRESS_ID);
        wizard.registerPage(progressPage);

        wizard.create();

    }


    @Override
    public boolean performFinish()
    {

        accepted = true;

        try
        {

            ThreadUtils.runSafeAsync(log, new Runnable()
            {
                @Override
                public void run()
                {
                    IProgressMonitor progress = progressPage.getProgressMonitor(true, true);
                    invitationStatus = process.accept(progress);
                    switch (invitationStatus)
                    {
                        case OK:
                            break;
                        case CANCEL:
                        case ERROR:
                            asyncShowCancelMessage(process.getPeer(),
                                    process.getErrorMessage(), ProcessTools.CancelLocation.LOCAL);
                            break;
                        case REMOTE_CANCEL:
                        case REMOTE_ERROR:
                            asyncShowCancelMessage(process.getPeer(),
                                    process.getErrorMessage(), ProcessTools.CancelLocation.REMOTE);
                            break;

                    }
                }
            });

        }
        catch (Exception e)
        {
            Throwable cause = e.getCause();

            if (cause == null)
            {
                cause = e;
            }

            asyncShowCancelMessage(process.getPeer(), cause.getMessage(),
                    ProcessTools.CancelLocation.LOCAL);

            // give up, close the wizard as we cannot do anything here !
            return accepted;
        }

        return accepted;
    }


    @Override
    public boolean performCancel()
    {
        ThreadUtils.runSafeAsync("CancelJoinSessionWizard", log,
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        process.localCancel(null, ProcessTools.CancelOption.NOTIFY_PEER);
                    }
                }
        );
        return true;
    }

    /**
     * Get rid of this method, use a listener !
     */
    @Override
    public void cancelWizard(final JID jid, final String errorMsg, final ProcessTools.CancelLocation cancelLocation)
    {

        ThreadUtils.runSafeSync(log, new Runnable()
        {
            @Override
            public void run()
            {

                /*
                 * do NOT CLOSE the wizard if it performs async operations
                 *
                 * see performFinish() -> getContainer().run(boolean, boolean,
                 * IRunnableWithProgress)
                 */
                if (accepted)
                {
                    return;
                }

                //todo
                /* Shell shell = JoinSessionWizard.this.getShell();
               if (shell == null || shell.isDisposed())
                   return;

               ((WizardDialog) JoinSessionWizard.this.getContainer()).close();*/

                asyncShowCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void asyncShowCancelMessage(final JID jid, final String errorMsg,
            final ProcessTools.CancelLocation cancelLocation)
    {
        ThreadUtils.runSafeAsync(log, new Runnable()
        {
            @Override
            public void run()
            {
                showCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void showCancelMessage(JID jid, String errorMsg,
            ProcessTools.CancelLocation cancelLocation)
    {

        String peer = jid.getBase();

        Container shell = parent;

        if (errorMsg != null)
        {
            switch (cancelLocation)
            {
                case LOCAL:
                    DialogUtils.openErrorMessageDialog(shell,
                            Messages.JoinSessionWizard_inv_cancelled,
                            Messages.JoinSessionWizard_inv_cancelled_text
                                    + Messages.JoinSessionWizard_8 + errorMsg
                    );
                    break;
                case REMOTE:
                    DialogUtils.openErrorMessageDialog(shell,

                            Messages.JoinSessionWizard_inv_cancelled, MessageFormat.format(
                                    Messages.JoinSessionWizard_inv_cancelled_text2, peer,
                                    errorMsg)
                    );
            }
        }
        else
        {
            switch (cancelLocation)
            {
                case LOCAL:
                    break;
                case REMOTE:
                    DialogUtils.openInformationMessageDialog(shell,
                            Messages.JoinSessionWizard_inv_cancelled, MessageFormat
                                    .format(Messages.JoinSessionWizard_inv_cancelled_text3,
                                            peer)
                    );
            }
        }
    }
}