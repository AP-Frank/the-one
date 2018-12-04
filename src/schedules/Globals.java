package schedules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

public class Globals {
    static Random Rnd = new Random(0);
    static GlobalSchedule GlobSched = new GlobalSchedule("schedule.json");

    static Schedule GetStudentSchedule() {

        var daily_schedules = new ArrayList<>(Collections.nCopies(7, new LinkedList<Activity>()));

        for (int i = 0; i < 20; i++) {
            int day = Rnd.nextInt(7);
            int time = Rnd.nextInt(60 * 60 * 12) + 60 * 60 * 8; // Time in seconds from 8:00 to 20:00
            if (daily_schedules.get(day).stream().anyMatch(activity -> activity.overlapsTime(time))) {
                continue;
            }
            var a_opt = GlobSched.pickFreeActivity(day, time);
            a_opt.ifPresent(activity -> daily_schedules.get(day).add(activity));
        }

        return new MultiSchedule(daily_schedules.stream().
                map(SimpleSchedule::new).collect(Collectors.toList()), 24 * 60 * 60);
    }
}
