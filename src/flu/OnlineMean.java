package flu;

public class OnlineMean {
    public static void updateMean(Aggregate agg, double val) {
        agg.count++;
        double delta = val - agg.mean;
        agg.mean += delta / agg.count;
        double delta2 = val - agg.mean;
        agg.m2 += delta * delta2;
    }

    public static Mean getCurrentMean(Aggregate agg) {
        if (agg.count < 2){
            return new Mean(-1, -1, -1);
        }
        return new Mean(agg.mean, agg.m2 / agg.count, agg.m2 / (agg.count - 1));
    }
}
