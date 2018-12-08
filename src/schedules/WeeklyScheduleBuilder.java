package schedules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class WeeklyScheduleBuilder {

    private int numberWantedActivities = 15;
    private int tryLimit = 30;
    private boolean includeWeekend = false;
    private boolean doLoop = false;
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

        var numDays = includeWeekend ? 7 : 5;

        var dailyActivities = new ArrayList<>(Collections.nCopies(numDays, new LinkedList<Activity>()));

        int numberPickedActivities = 0;
        for (int i = 0; i < tryLimit; i++) {

            int day = Globals.Rnd.nextInt(numDays); // MO - FR
            int hour = Globals.Rnd.nextInt(12) + 8; // 8:00 - 20:00

            int timeSec = hour * 3600;

            var pickedActivity = Globals.GlobSched.pickFreeActivity(day, timeSec);
            if (pickedActivity.isPresent()) {
                dailyActivities.get(day).add(pickedActivity.get());
                numberPickedActivities++;

                if (numberPickedActivities >= numberWantedActivities) {
                    break;
                }
            }

        }

        var dailySchedules = dailyActivities.stream().map(SimpleSchedule::new).collect(Collectors.toList());
        var oneWeek = new MultiSchedule(dailySchedules, 24 * 3600);
        if (doLoop) {
            return new LoopingSchedule(oneWeek, 24 * 3600 * numDays);
        } else {
            return oneWeek;
        }
    }

}
