package schedules;

import java.util.*;

public class SimpleSchedule extends Schedule {

    private final List<Activity> activities;

    public SimpleSchedule(Collection<Activity> activities) {
        this.activities = new ArrayList<>(activities);
        Collections.sort(this.activities);
    }

    protected int findIndexByTime(int currentTime){
        // TODO This is a linear search, use binary search?
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).overlapsTime(currentTime)) {
                return i;
            }
        }
        return -1;
    }

    protected Optional<Activity> indexToActivity(int index){
        if (index < 0 || index >= activities.size()){
            return Optional.empty();
        }
        return Optional.of(activities.get(index));
    }

    @Override
    public Optional<Activity> getActivity(int currentTime, int offset) {
        return indexToActivity(findIndexByTime(currentTime) + offset);
    }

    @Override
    public int getNumberOfActivities() {
        return activities.size();
    }
}
