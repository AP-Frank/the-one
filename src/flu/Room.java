package flu;

import core.Coord;
import core.SimClock;

public class Room {
    public String Tag;
    public double PosX;
    public double PosY;

    private double contamination = 0;
    private double timeContRetrieved = 0;
    private Aggregate agg = new Aggregate();

    public double getContamination() {
        double timeDiff = SimClock.getTime() - timeContRetrieved;
        timeContRetrieved = SimClock.getTime();
        contamination = Math.max(0, contamination - (timeDiff / (18 * 60 *60)));
        System.out.println("Room " + Tag + " has contamination level: " + contamination);
        return contamination;
    }

    public void setContamination(double contamination) {
        this.contamination = Math.min(contamination, 100);
    }

    public Coord getCoord(){
        return new Coord(PosX, PosY);
    }

    public void updateAggregate(){
        OnlineMean.updateMean(agg, contamination);
    }

    public Mean getCurrentMean(){
        return OnlineMean.getCurrentMean(agg);
    }
}
