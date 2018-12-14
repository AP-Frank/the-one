package report;

import core.DTNHost;
import core.SimClock;
import flu.ContaminationState;

import java.util.List;

public class ContaminationReport extends SamplingReport {
    @Override protected void sample(List<DTNHost> hosts) {
        int healthy = 0;
        int infected = 0;
        int contagious = 0;
        int immune = 0;

        for (var host : hosts) {
            switch (host.hostContamination.getState()) {
                case Healthy:
                    healthy++;
                    break;
                case Infected:
                    infected++;
                    break;
                case Contagious:
                    contagious++;
                    break;
                case Immune:
                    immune++;
                    break;
            }
        }

        super.write(SimClock.getIntTime() + ", " + healthy + ", " + infected + ", " + contagious +
                ", " + immune);
    }
}
