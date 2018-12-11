package schedules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import core.Settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GlobalSchedule {
    private static String PATH_SCHEDULE = "pathToSchedule";
    List<RoomAssignment> assignments = new LinkedList<>();
    SimpleDateFormat dateParser = new SimpleDateFormat("ddMMyyyy");

    public GlobalSchedule(Settings settings) {
        String currentNs = settings.getNameSpace();
        settings.setNameSpace("MapBasedMovement");

        String json = settings.getSetting(PATH_SCHEDULE);
        settings.setNameSpace(currentNs);

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(json))) {
            Gson gson = new GsonBuilder().create();
            ScheduleAssignment[] all_assignments = gson.fromJson(reader, ScheduleAssignment[].class);

            var min_date = Arrays.stream(all_assignments).map(a -> parseDate(a.day)).min(Date::compareTo).get();

            for (var a : all_assignments) {
                var na = new RoomAssignment();
                na.day = (int) ((parseDate(a.day).getTime() - min_date.getTime()) / (1000 * 60 * 60 * 24));
                na.limit = a.seats > 0 ? a.seats : 0; // TODO
                na.activity = new Activity(a.room_local, tts(a.time_start), tts(a.time_end), a.ev);
                assignments.add(na);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Date parseDate(String day) {
        try {
            return dateParser.parse(day);
        } catch (ParseException e) {
            throw new RuntimeException("Crash", e);
        }
    }

    private int tts(String time) {
        String[] units = time.split(":");
        int hours = Integer.parseInt(units[0]);
        int minutes = Integer.parseInt(units[1]);
        return 60 * minutes + 3600 * hours;
    }

    public Optional<Activity> pickFreeActivity(int day, int time, int freeUntil, int blockedUntil) {
        var freeAssignments = assignments.stream().
                filter(a -> a.day == day &&
                        a.activity.overlapsTime(time) &&
                        a.activity.end <= freeUntil &&
                        a.activity.start >= blockedUntil &&
                        a.limit > 0).
                collect(Collectors.toList());

        if (freeAssignments.size() == 0) {
            return Optional.empty();
        }

        /*
        int freeSlotsTotal = freeAssignments.stream().mapToInt(a -> a.limit).sum();

        int selectedSlot = Globals.Rnd.nextInt(freeSlotsTotal);

        int idx = 0;
        for (var a : freeAssignments) {
            if (selectedSlot < a.limit) {
                return Optional.of(freeAssignments.get(idx).activity);
            }
            selectedSlot -= a.limit;
            idx++;
        }*/

        int selectedActivity = Globals.Rnd.nextInt(freeAssignments.size());
        return Optional.of(freeAssignments.get(selectedActivity).activity);

        // Should not be reached
        //assert false;
        //return Optional.empty();
    }


}
