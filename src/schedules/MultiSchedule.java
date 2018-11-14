package schedules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MultiSchedule extends Schedule {

    private final List<Schedule> subschedules;
    private final int partialLength;

    public MultiSchedule(Collection<? extends Schedule> subschedules, int partialLength) {
        assert partialLength > 0;
        this.subschedules = new ArrayList<>(subschedules);
        this.partialLength = partialLength;
    }

    @Override
    public Optional<Activity> getActivity(int currentTime, int offset) {
        // Relative time in subschedule
        int relTime = currentTime % partialLength;

        // In which subschedule are we currently
        int relScheduleIndex = currentTime / partialLength;
        if (relScheduleIndex >= subschedules.size()) {
            relScheduleIndex = subschedules.size() - 1;
        }

        Schedule relSchedule;

        // Easy case: No offset
        if (offset == 0) {
            relSchedule = subschedules.get(relScheduleIndex);
            return relSchedule.getActivity(relTime, 0);
        }
        ////////////////////////////////////////////////////////////////////////
        // Positive offset: If offset is inside subschedule -> get
        // else decrease offset accordingly and move to next schedule
        // When we move outside of the last schedule, there is no activity
        ////////////////////////////////////////////////////////////////////////
        else if (offset > 0) {
            int relOffset = offset;
            while (relScheduleIndex < subschedules.size()) {
                relSchedule = subschedules.get(relScheduleIndex);

                int remainingActivities = relSchedule.getNumberOfActivitiesAfter(relTime);
                if (remainingActivities <= relOffset) {
                    // Inside current relSchedule
                    return relSchedule.getActivity(relTime, relOffset);
                } else {
                    // Outside current relSchedule
                    relTime = -1; // time always before the next part
                    relScheduleIndex++;
                    relOffset -= remainingActivities;
                }
            }

            return Optional.empty(); // After the last subschedule
        }
        ////////////////////////////////////////////////////////////////////////
        // Negative offset: Similar to the positive direction
        ////////////////////////////////////////////////////////////////////////
        else { // offset < 0
            while (relScheduleIndex >= 0) {
                int relOffset = offset;
                relSchedule = subschedules.get(relScheduleIndex);

                int remainingActivities = relSchedule.getNumberOfActivitiesBefore(relTime);
                if (remainingActivities <= -relOffset) { // relOffset is negative, so get count by switching sign
                    // Inside current relSchedule
                    return relSchedule.getActivity(relTime, relOffset);
                } else {
                    // Outside current relSchedule
                    relTime = this.partialLength + 1; // time always after the previous part
                    relScheduleIndex--;
                    //noinspection UnusedAssignment //TODO Check why this is reported as unused
                    relOffset += remainingActivities;
                }
            }

            return Optional.empty(); // Before the first subschedule
        }


    }

    @Override
    public int getNumberOfActivities() {
        int sum = 0;
        for (var s : subschedules) {
            if (s instanceof InfiniteSchedule) {
                sum += ((InfiniteSchedule) s).getNumberOfActivitiesIn(0, partialLength);
            } else {
                sum += s.getNumberOfActivities();
            }
        }
        return sum;
    }

    @Override
    int getNumberOfActivitiesAfter(int currentTime) {
        return 0; //TODO
    }

    @Override
    int getNumberOfActivitiesBefore(int currentTime) {
        return 0; //TODO
    }
}
