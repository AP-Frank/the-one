package flu;

import core.SimClock;
import schedules.Globals;

import static java.lang.Double.max;

public class HostContamination {

    private ContaminationState state = ContaminationState.Healthy;
    private double contamination;

    private double lastUpdated = 0;
    private double lastStateChange = 0;
    private static final int contRedFactor = 18;
    private final double transmissionProb = 0.00005 + 0.00005 * Globals.Rnd.nextDouble();
    private double timeTillInfected = 24 * 60 * 60 + Globals.Rnd.nextGaussian() * 10 * 60 * 60;
    private double timeTillContagious = 3 * 24 * 60 * 60 + Globals.Rnd.nextGaussian() * 48 * 60 * 60;

    public HostContamination(double initialContamination) {
        this.contamination = 0; // max(0, initialContamination * Globals.Rnd.nextGaussian());
        if (Globals.Rnd.nextDouble() < initialContamination / 100.0){
            state = ContaminationState.Contagious;
        }
    }

    public ContaminationState getState() {
        double timeDiff = SimClock.getTime() - lastStateChange;
        switch (state) {
            case Healthy:
                break;
            case Infected:
                if (timeDiff > timeTillInfected) {
                    state = ContaminationState.Contagious;
                    lastStateChange = SimClock.getTime();
                }
                break;
            case Contagious:
                if (timeDiff > timeTillContagious) {
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
            return contamination + 20;
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
                Globals.Rnd.nextDouble() >= Math.pow(1 - probPerSecond, time)) {
            state = ContaminationState.Infected;
            lastStateChange = SimClock.getTime();
        }
    }
}
