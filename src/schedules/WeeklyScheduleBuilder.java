package schedules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class WeeklyScheduleBuilder {

    private final int numberWantedActivities;
    private final int tryLimit;

    public WeeklyScheduleBuilder(int numberWantedActivities, int tryLimit) {
        this.numberWantedActivities = numberWantedActivities;
        // TODO Settings for day / time selection preferences / probability distributions
        this.tryLimit = tryLimit;
    }

    public Schedule build() {

        List<List<Activity>> dailyActivities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dailyActivities.add(new LinkedList<>());
        }

        int numberPickedActivities = 0;
        for (int i = 0; i < tryLimit; i++) {

            int day = Globals.Rnd.nextInt(5); // MO - FR
            int hour = Globals.Rnd.nextInt(12); // 8:00 - 20:00

            int timeSec = hour * 3600;

            var pickedActivity = Globals.Schedule.pickFreeActivity(day, timeSec);
            if (pickedActivity.isPresent()) {
                dailyActivities.get(day).add(pickedActivity.get());
                numberPickedActivities++;

                if (numberPickedActivities >= numberWantedActivities) {
                    break;
                }
            }

        }

        var dailySchedules = dailyActivities.stream().map(day -> new SimpleSchedule(day)).collect(Collectors.toList());
        return new LoopingSchedule(new MultiSchedule(dailySchedules, 24 * 3600), 24 * 3600 * 7);
    }

}
