package schedules;

import java.util.Optional;

public abstract class Schedule {

    public Optional<Activity> getCurrentActivity(int currentTime) {
        return getActivity(currentTime, 0);
    }

    public Optional<Activity> getNextActivity(int currentTime) {
        return getActivity(currentTime, 1);
    }

    public abstract Optional<Activity> getActivity(int currentTime, int offset);

    public abstract int getNumberOfActivities();

}
