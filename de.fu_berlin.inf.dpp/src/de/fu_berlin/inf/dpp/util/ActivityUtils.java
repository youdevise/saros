package de.fu_berlin.inf.dpp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.activities.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;

/**
 * Class contains static helper methods for {@link IActivity activities}.
 */
public class ActivityUtils {

    /**
     * Checks if the give collections contains only
     * {@linkplain ChecksumActivity checksum activities}.
     * 
     * @param activities
     *            collection containing {@linkplain IActivity activities}
     * @return <code>true</code> if the collection contains only checksum
     *         activities, <code>false</code> otherwise
     */
    public static boolean containsChecksumsOnly(Collection<IActivity> activities) {

        if (activities.isEmpty())
            return false;

        for (IActivity a : activities)
            if (!(a instanceof ChecksumActivity))
                return false;

        return true;
    }

    /**
     * Tries to reduce the number of {@link IActivity activities} so that:
     * <p>
     * 
     * <pre>
     * for (activity : optimize(activities))
     *         exec(activity)
     * 
     * will produce the same result as
     * 
     * for (activity : activities)
     *         exec(activity)
     * </pre>
     * 
     * @param activities
     *            a collection containing the activities to optimize
     * @return a list which may contains a reduced amount of activities
     */

    public static List<IActivity> optimize(Collection<IActivity> activities) {

        List<IActivity> result = new ArrayList<IActivity>(activities.size());

        boolean[] dropActivityIdx = new boolean[activities.size()];

        Map<SPath, Integer> selections = new HashMap<SPath, Integer>();
        Map<SPath, Integer> viewports = new HashMap<SPath, Integer>();

        /*
         * keep only the latest selection/viewport activities per project and
         * path
         */

        int activityIdx = 0;

        for (IActivity activity : activities) {

            if (activity instanceof TextSelectionActivity) {
                SPath path = ((TextSelectionActivity) activity).getPath();

                Integer idx = selections.get(path);

                if (idx != null)
                    dropActivityIdx[idx] = true;

                selections.put(path, activityIdx);
            } else if (activity instanceof ViewportActivity) {
                SPath path = ((ViewportActivity) activity).getPath();

                Integer idx = viewports.get(path);

                if (idx != null)
                    dropActivityIdx[idx] = true;

                viewports.put(path, activityIdx);
            }

            activityIdx++;
        }

        activityIdx = 0;

        for (IActivity activity : activities)
            if (!dropActivityIdx[activityIdx++])
                result.add(activity);

        return result;
    }
}
