package schedules;

public abstract class InfiniteSchedule extends Schedule {

    abstract int getNumberOfActivitiesIn(int startTime, int endTime);

    @Override
    int getNumberOfActivities() {
        return -1;
    }

    @Override
    int getNumberOfActivitiesAfter(int currentTime) {
        return -1;
    }

    @Override
    int getNumberOfActivitiesBefore(int currentTime) {
        return -1;
    }
}
