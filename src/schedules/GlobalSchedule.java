package schedules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GlobalSchedule {
    List<RoomAssignment> assignments = new LinkedList<>();

    public GlobalSchedule(String json) {
        try(InputStreamReader reader = new InputStreamReader(new FileInputStream(json))) {
            Gson gson = new GsonBuilder().create();
            ScheduleAssignment[] all_assignments = gson.fromJson(reader, ScheduleAssignment[].class);

            for(var a : all_assignments){
                var na = new RoomAssignment();
                na.day = Integer.parseInt(a.day);
                na.limit = a.seats > 0 ? a.seats : 0; // TODO
                na.activity = new Activity(a.room_local, tts(a.time_start), tts(a.time_end), a.ev);
                assignments.add(na);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int tts(String time){
        String[] units = time.split(":");
        int hours = Integer.parseInt(units[0]);
        int minutes = Integer.parseInt(units[1]);
        return 60 * minutes + 3600 * hours;
    }

    public Optional<Activity> pickFreeActivity(int day, int time) {
        var freeAssignments = assignments.stream().
                filter(a -> a.day == day && a.activity.overlapsTime(time) && a.limit > 0).
                collect(Collectors.toList());

        if (freeAssignments.size() == 0) {
            return Optional.empty();
        }

        int freeSlotsTotal = freeAssignments.stream().mapToInt(a -> a.limit).sum();

        int selectedSlot = Globals.Rnd.nextInt(freeSlotsTotal);

        for (var a : assignments) {
            if (selectedSlot < a.limit) {
                return Optional.of(freeAssignments.get(selectedSlot).activity);
            }
            selectedSlot -= a.limit;
        }

        // Should not be reached
        assert false;
        return Optional.empty();
    }


}
