package schedules;

import util.Tuple;

import java.util.Optional;

public class LoopingSchedule extends InfiniteSchedule {

    private final Schedule internal;
    private final int loopLength;

    public LoopingSchedule(Schedule internal, int loopLength) {
        assert loopLength > 0;
        // TODO Assert that loop length covers schedule?
        this.internal = internal;
        this.loopLength = loopLength;
        if (internal.getNumberOfActivities() < 0) {
            throw new IllegalArgumentException("Can't wrap an infinite schedule.");
        }
    }

    @Override
    public Optional<Tuple<Activity, Integer>> getActivity(int currentTime, int offset) {
        int n = internal.getNumberOfActivities();

        int relTime = currentTime % loopLength;
        int relOffset = offset % n;

        var a = internal.getActivity(relTime, relOffset);

        if (!a.isPresent()) {
            if (relOffset < 0) {
                a = internal.getActivity(relTime, relOffset + n);
            } else if (relOffset > 0) {
                a = internal.getActivity(relTime, relOffset - n);
            }
        }

        if (a.isPresent()) {
            var ua = a.get().getKey();
            a = wrap(ua, currentTime, offset * loopLength); // TODO: startOffset calculated wrongly
        }

        return a;
    }

    @Override
    int getNumberOfActivitiesIn(int startTime, int endTime) {
        //TODO This does not work in all cases
        return (endTime - startTime) / loopLength * internal.getNumberOfActivities();
    }
}
