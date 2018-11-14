package schedules;

import java.util.Objects;

public class Activity implements Comparable<Activity> {
    public final String location;
    public final int start;
    public final int duration;

    public Activity(String location, int start, int duration) {
        this.location = location;
        this.start = start;
        this.duration = duration;
    }

    public boolean overlapsTime(int time) {
        return time >= start && time < start + duration;
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
}
