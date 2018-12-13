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
        if (freeOffices.size() == 0) {
            throw new IllegalArgumentException("Not enough rooms for the staff.");
        }
        var idx = Globals.Rnd.nextInt(freeOffices.size());
        var office = freeOffices.get(idx);
        seats.put(office, seats.get(office) - 1);

        String someToilet;
        var officeToilet = Globals.RoomMapping.wcs.stream().filter(s -> s.startsWith(office.substring(0, 5))).findFirst();
        if (officeToilet.isPresent()) {
            someToilet = officeToilet.get();
        } else {
            int idxToilet = Globals.Rnd.nextInt(Globals.RoomMapping.wcs.size());
            someToilet = Globals.RoomMapping.wcs.get(idxToilet);
        }

        for (int day = 0; day < numDays; day++) {
            var extendedDay = new LinkedList<Activity>();
            extendedDaily.add(extendedDay);
            if (day >= 5) continue;

            var dayAct = dailyActivities.get(day);
            dayAct.sort(Comparator.comparingInt(o -> o.start));

            var lastEnd = 8 * 3600; // 8:00
            boolean hadLunch = false;
            int remToilet = Globals.Rnd.nextInt(1) + 1;
            int lastToilet = 8 * 3600;

            for (var a : dayAct) {
                int gap = a.start - lastEnd;
                if (gap > 0) {
                    if (!hadLunch && lastEnd > 11 * 3600 && gap >= 3600) {
                        lastEnd = haveLunch(office, extendedDay, lastEnd);
                        hadLunch = true;
                    } else if (remToilet > 0 && lastEnd - lastToilet > 3 * 3600) {
                        lastEnd = lastToilet = haveToilet(someToilet, extendedDay, lastEnd);
                        remToilet--;
                    } else {
                        lastEnd = a.end;
                    }
                } else {
                    lastEnd = a.end;
                }

                extendedDay.add(a);
            }

            if (lastEnd < 16 * 3600) {
                if (!hadLunch && lastEnd > 11 * 3600) {
                    lastEnd = haveLunch(office, extendedDay, lastEnd);
                } else if (!hadLunch && lastEnd < 11 * 3600) {
                    lastEnd = haveLunch(office, extendedDay, 12 * 3600);
                }
                if (remToilet > 0) {
                    if (lastEnd > 10 * 3600) {
                        haveToilet(someToilet, extendedDay, lastEnd);
                    } else {
                        haveToilet(someToilet, extendedDay, Globals.Rnd.nextInt(6 * 3600) + 10 * 3600);
                    }
                }
            }

            extendedDay.sort(Comparator.comparingInt(o -> o.start));

            lastEnd = 8 * 3600 - Globals.Rnd.nextInt(30 * 60);
            var stuffToAdd = new LinkedList<Activity>();
            for (var a : extendedDay) {
                int gap = a.start - lastEnd;
                if (gap > 0) {
                    stuffToAdd.add(new Activity(office, lastEnd, a.start, "Work"));
                }

                lastEnd = a.end;
                stuffToAdd.add(a);
            }

            extendedDay.clear();
            extendedDay.addAll(stuffToAdd);
            extendedDay.sort(Comparator.comparingInt(o -> o.start));

            if (lastEnd < 18 * 3600) {
                int end = 18 * 3600;
                if (lastEnd < 18 * 3600 - 30 * 60) {
                    end += Globals.Rnd.nextInt(60 * 15 * 4) - 2 * 60 * 15;
                } else {
                    end += Globals.Rnd.nextInt(60 * 15 * 3);
                }
                extendedDay.add(new Activity(office, lastEnd, end, "Work"));
            }
        }

        return super.toSchedule(extendedDaily);
    }

    private int haveToilet(String toilet, LinkedList<Activity> extendedDay, int lastEnd) {
        extendedDay.add(new Activity(toilet, lastEnd, lastEnd + 5 * 60, "Toilet"));
        return lastEnd + 5 * 60;
    }

    private int haveLunch(String office, LinkedList<Activity> extendedDay, int lastEnd) {
        extendedDay.add(new Activity(Tags.EAT.toString(), lastEnd, lastEnd + 15 * 60, "Lunch"));
        if (Globals.Rnd.nextInt(2) == 0) {
            extendedDay.add(new Activity(Tags.EATING.toString(), lastEnd + 15 * 60, lastEnd + 3600, "Lunch (Seating)"));
        } else {
            extendedDay.add(new Activity(office, lastEnd + 15 * 60, lastEnd + 3600, "Lunch (Seating)"));
        }
        return lastEnd + 3600;
    }
}
