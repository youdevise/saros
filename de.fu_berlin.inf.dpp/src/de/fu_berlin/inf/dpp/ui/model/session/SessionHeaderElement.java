package de.fu_berlin.inf.dpp.ui.model.session;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.model.HeaderElement;
import de.fu_berlin.inf.dpp.ui.model.TreeElement;

/**
 * Container {@link TreeElement} for a {@link SarosSession}
 * 
 * @author bkahlert
 */
public class SessionHeaderElement extends HeaderElement {
    private final SessionInput input;

    public SessionHeaderElement(Font font, SessionInput rosterSessionInput) {
        super(font);
        this.input = rosterSessionInput;
    }

    @Override
    public StyledString getStyledText() {
        StyledString styledString = new StyledString();
        if (input == null || input.getSession() == null) {
            styledString.append(
                Messages.SessionHeaderElement_no_session_running, boldStyler);
        } else {
            styledString.append(Messages.SessionHeaderElement_session,
                boldStyler);
        }
        return styledString;
    }

    @Override
    public Image getImage() {
        return ImageManager.ELCL_PROJECT_SHARE;
    }

    @Override
    public boolean hasChildren() {
        return input != null && input.getSession() != null;
    }

    @Override
    public Object[] getChildren() {

        if (input == null || input.getSession() == null)
            return new Object[0];

        final List<UserElement> userElements = new ArrayList<UserElement>();

        final List<User> users = input.getSession().getUsers();

        for (final User user : users)
            userElements.add(new UserElement(user));

        return userElements.toArray();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((input == null) ? 0 : input.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionHeaderElement other = (SessionHeaderElement) obj;
        if (input == null) {
            if (other.input != null)
                return false;
        } else if (!input.equals(other.input))
            return false;
        return true;
    }
}