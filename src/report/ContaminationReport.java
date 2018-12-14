package report;

import core.DTNHost;
import core.SimClock;
import flu.ContaminationState;

import java.util.List;

public class ContaminationReport extends SamplingReport {
    @Override protected void sample(List<DTNHost> hosts) {
        int infected = 0;
        for (var host : hosts) {
            if (host.hostContamination.getState() != ContaminationState.Healthy) {
                infected++;
            }
        }

        super.write(SimClock.getIntTime() + ", " + infected);
    }
}
