package report;

import core.DTNHost;

import java.util.List;

public class ContaminationReport extends SamplingReport {
    @Override protected void sample(List<DTNHost> hosts) {
        int infected = 0;
        for (var host : hosts) {
            if (host.isContaminated) {
                infected++;
            }
        }

        super.write(Integer.toString(infected));
    }
}
