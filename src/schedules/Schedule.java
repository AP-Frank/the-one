package schedules;

import util.Tuple;

import java.util.Optional;

public abstract class Schedule {

    public Optional<Tuple<Activity, Integer>> getCurrentActivity(int currentTime) {
        return getActivity(currentTime, 0);
    }

    public Optional<Tuple<Activity, Integer>> getNextActivity(int currentTime) {
        return getActivity(currentTime, 1);
    }

    public abstract Optional<Tuple<Activity, Integer>> getActivity(int currentTime, int offset);

    abstract int getNumberOfActivities();

    abstract int getNumberOfActivitiesAfter(int currentTime);

    abstract int getNumberOfActivitiesBefore(int currentTime);

    protected Optional<Tuple<Activity, Integer>> wrap(Activity activity, int currentTime, int startOffset) {
        if (activity != null) {
            var t = new Tuple<>(activity, activity.start + startOffset - currentTime);
            return Optional.of(t);
        }
        return Optional.empty();
    }

    protected Optional<Tuple<Activity, Integer>> wrap(Activity activity, int currentTime) {
        return wrap(activity, currentTime, 0);
    }

}
