package schedules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class StaffScheduleBuilder extends WeeklyScheduleBuilder {

    private ArrayList<String> offices;
    private HashMap<String, Integer> seats = new HashMap<>();

    public StaffScheduleBuilder() {
        offices = Globals.RoomMapping.offices;
        for (String o : offices) {
            var n = Globals.Rnd.nextInt(10); // 1 out of 10 offices has only a single desk
            seats.put(o, n == 0 ? 1 : 2);
        }
    }

    @Override
    protected Schedule toSchedule(ArrayList<LinkedList<Activity>> dailyActivities) {

        var extendedDaily = new ArrayList<LinkedList<Activity>>();
        var freeOffices = offices.stream().filter(s -> seats.get(s) > 0).collect(Collectors.toList());
        if(freeOffices.size() == 0){
            throw new IllegalArgumentException("Not enough rooms for the staff.");
        }
        var idx = Globals.Rnd.nextInt(freeOffices.size());
        var office = freeOffices.get(idx);
        seats.put(office, seats.get(office) - 1);

        for (int day = 0; day < numDays; day++) {
            var extendedDay = new LinkedList<Activity>();
            extendedDaily.add(extendedDay);
            if (day >= 5) continue;

            var dayAct = dailyActivities.get(day);
            dayAct.sort(Comparator.comparingInt(o -> o.start));

            var lastEnd = 8 * 3600; // 8:00
            boolean hadLunch = false;

            for (var a : dayAct) {
                int gap = a.start - lastEnd;
                if (gap > 0) {
                    if (!hadLunch && lastEnd > 11 * 3600 && gap >= 3600) {
                        haveLunch(office, extendedDay, lastEnd);
                        extendedDay.add(new Activity(office, lastEnd + 3600, a.start, "Work"));
                        hadLunch = true;
                    } else {
                        extendedDay.add(new Activity(office, lastEnd, a.start, "Work"));
                    }
                }

                lastEnd = a.end;
                extendedDay.add(a);
            }

            if (lastEnd < 18 * 3600) {
                if (!hadLunch && lastEnd > 11 * 3600 && lastEnd < 16 * 3600) {
                    haveLunch(office, extendedDay, lastEnd);
                    extendedDay.add(new Activity(office, lastEnd + 3600, 18 * 3600, "Work"));
                } else if (!hadLunch && lastEnd < 11 * 3600) {
                    extendedDay.add(new Activity(office, lastEnd, 12 * 3600, "Work"));
                    haveLunch(office, extendedDay, 12 * 3600);
                    extendedDay.add(new Activity(office, 12 * 3600 + 3600, 18 * 3600, "Work"));
                } else {
                    extendedDay.add(new Activity(office, lastEnd, 18 * 3600, "Work"));
                }
            }
        }

        return super.toSchedule(extendedDaily);
    }

    private void haveLunch(String office, LinkedList<Activity> extendedDay, int lastEnd) {
        extendedDay.add(new Activity(Tags.EAT.toString(), lastEnd, lastEnd + 15 * 60, "Lunch"));
        if (Globals.Rnd.nextInt(2) == 0) {
            extendedDay.add(new Activity(Tags.SEATING.toString(), lastEnd + 15 * 60, lastEnd + 3600, "Lunch (Seating)"));
        } else {
            extendedDay.add(new Activity(office, lastEnd + 15 * 60, lastEnd + 3600, "Lunch (Seating)"));
        }
    }
}
