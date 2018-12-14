package schedules;

import java.time.LocalTime;
import java.util.Objects;

public class Activity implements Comparable<Activity> {
    public final String location;
    public final int start;
    public final int end;
    public final int duration;
    public final String name;

    public Activity(String location, int start, int end, String name) {
        this.location = location;
        this.start = start;
        this.end = end;
        this.duration = end - start;
        this.name = name;
    }

    private String toTime(int seconds) {
        var t = LocalTime.ofSecondOfDay(seconds);
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    public boolean overlapsTime(int time) {
        return time >= start && time < end;
    }

    @Override
    public int compareTo(Activity other) {
        return Integer.compare(this.start, other.start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return start == activity.start &&
                duration == activity.duration &&
                Objects.equals(location, activity.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, start, duration);
    }

    @Override
    public String toString() {
        return location + " - " + name + " [" + toTime(start) + "," + toTime(end) + "]";
    }
}
