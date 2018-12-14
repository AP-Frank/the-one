package flu;

import core.SimClock;
import schedules.Globals;

public class HostContamination {

    private ContaminationState state = ContaminationState.Healthy;
    private double contamination;

    private double lastUpdated = 0;
    private double lastStateChange = 0;
    private static final int contRedFactor = 18;
    private static final double transmissionProb = 0.00005;

    public HostContamination(double initialContamination) {
        this.contamination = initialContamination;
    }

    public ContaminationState getState() {
        double timeDiff = SimClock.getTime() - lastStateChange;
        switch (state) {
            case Healthy:
                break;
            case Infected:
                if (timeDiff > 24 * 60 * 60 + Globals.Rnd.nextGaussian() * 10 * 60 * 60) {
                    state = ContaminationState.Contagious;
                    lastStateChange = SimClock.getTime();
                }
                break;
            case Contagious:
                if (timeDiff > 3 * 24 * 60 * 60 + Globals.Rnd.nextGaussian() * 48 * 60 * 60) {
                    state = ContaminationState.Immune;
                    lastStateChange = SimClock.getTime();
                }
                break;
            case Immune:
                break;
        }
        return state;
    }

    public double getContamination() {
        updateContamination();

        if (getState() == ContaminationState.Contagious) {
            return contamination + 5;
        }
        return contamination;
    }

    public void changeContamination(double factor){
        updateContamination();
        contamination = contamination * factor;
    }

    private void updateContamination(){
        double timeDiff = SimClock.getTime() - lastUpdated;
        lastUpdated = SimClock.getTime();
        contamination = Math.max(0, contamination - (timeDiff / (contRedFactor * 60 * 60)));
    }

    public void addContamination(double contamination, int time) {
        updateContamination();
        this.contamination = Math.min(this.contamination + 0.1 * contamination, 100);

        double probPerSecond = transmissionProb * (this.contamination / 100);
        if (getState() == ContaminationState.Healthy &&
                Globals.Rnd.nextDouble() >=
                        Math.pow(1 - probPerSecond, time)) {
            state = ContaminationState.Infected;
            lastStateChange = SimClock.getTime();
        }
    }
}
