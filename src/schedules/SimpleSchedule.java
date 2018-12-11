package schedules;

import util.Tuple;

import java.util.*;

public class SimpleSchedule extends Schedule {

    private final List<Activity> activities;

    public SimpleSchedule(Collection<Activity> activities) {
        this.activities = new ArrayList<>(activities);
        Collections.sort(this.activities);
    }

    protected int findIndexByTime(int currentTime) {
        // TODO This is a linear search, use binary search?
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).overlapsTime(currentTime)) {
                return i;
            }
        }
        return -1;
    }

    protected Activity indexToActivity(int index) {
        if (index < 0 || index >= activities.size()) {
            return null;
        }
        return activities.get(index);
    }

    @Override
    public Optional<Tuple<Activity, Integer>> getActivity(int currentTime, int offset) {
        int idx = findIndexByTime(currentTime);
        Activity a = null;
        if(idx < 0){
            if(offset != 0) {
                a = indexToActivity(offset + getNumberOfActivitiesBefore(currentTime) - 1);
            }
        } else {
            a = indexToActivity(idx + offset);
        }
        return wrap(a, currentTime);
    }

    @Override
    int getNumberOfActivities() {
        return activities.size();
    }

    @Override
    int getNumberOfActivitiesAfter(int currentTime) {
        return (int) activities.stream().filter(activity -> activity.start > currentTime).count();
    }

    @Override
    int getNumberOfActivitiesBefore(int currentTime) {
        return (int) activities.stream().filter(activity -> activity.start < currentTime &&
                !activity.overlapsTime(currentTime)).count();
    }
}
