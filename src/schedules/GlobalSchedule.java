package schedules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
            RoomAssignment[] ra = gson.fromJson(reader, RoomAssignment[].class);
            //TODO Fill assignments
        } catch (IOException e) {
            e.printStackTrace();
        }
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
