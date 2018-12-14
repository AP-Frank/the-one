package schedules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class WeeklyScheduleBuilder {

    protected int numberWantedActivities = 15;
    protected int tryLimit = 30;
    protected boolean includeWeekend = false;
    protected boolean doLoop = false;

    protected int numDays;
    // TODO Settings for day and time selection preferences / probability distributions

    public WeeklyScheduleBuilder setNumberWantedActivities(int val) {
        this.numberWantedActivities = val;
        return this;
    }

    public WeeklyScheduleBuilder setTryLimit(int val) {
        this.tryLimit = val;
        return this;
    }

    public WeeklyScheduleBuilder setIncludeWeekend(boolean val) {
        this.includeWeekend = val;
        return this;
    }

    public WeeklyScheduleBuilder setDoLoop(boolean val) {
        this.doLoop = val;
        return this;
    }

    public Schedule build() {
        numDays = includeWeekend ? 7 : 5;

        var dailyActivities = new ArrayList<LinkedList<Activity>>();

        for (int i = 0; i < numDays; i++) {
            dailyActivities.add(new LinkedList<>());
        }

        int numberPickedActivities = 0;
        for (int i = 0; i < tryLimit; i++) {

            int day = Globals.Rnd.nextInt(numDays);
            int hour = Globals.Rnd.nextInt(12) + 8; // 8:00 - 20:00

            int timeSec = hour * 3600;

            if (dailyActivities.get(day).stream().anyMatch(activity -> activity.overlapsTime(timeSec))) {
                continue;
            }

            var nextActivityStart = dailyActivities.get(day).stream()
                    .filter(activity -> activity.end >= timeSec)
                    .map(activity -> activity.start).min(Integer::compareTo);

            var prevActivityEnd = dailyActivities.get(day).stream()
                    .filter(activity -> activity.start <= timeSec)
                    .map(activity -> activity.end).max(Integer::compareTo);

            var pickedActivity = Globals.GlobSched.pickFreeActivity(day, timeSec,
                    nextActivityStart.orElse(Integer.MAX_VALUE), prevActivityEnd.orElse(0));

            if (pickedActivity.isPresent()) {
                dailyActivities.get(day).add(pickedActivity.get());
                numberPickedActivities++;

                if (numberPickedActivities >= numberWantedActivities) {
                    break;
                }
            }

        }

        return toSchedule(dailyActivities);
    }

    protected Schedule toSchedule(ArrayList<LinkedList<Activity>> dailyActivities) {
        var dailySchedules = dailyActivities.stream().map(SimpleSchedule::new).collect(Collectors.toList());
        var oneWeek = new MultiSchedule(dailySchedules, 24 * 3600);
        if (doLoop) {
            return new LoopingSchedule(oneWeek, 24 * 3600 * numDays);
        } else {
            return oneWeek;
        }
    }

}
