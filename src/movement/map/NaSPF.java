package movement.map;

import schedules.Globals;

/**
 * NaSPF (Not always Shortest Path First) algorithm. Randomizes the taken path.
 */
public class NaSPF extends DijkstraPathFinder {

    /**
     * Constructor.
     *
     * @param okMapNodes The map node types that are OK for paths or null if
     *                   all nodes are OK
     */
    public NaSPF(int[] okMapNodes) {
        super(okMapNodes);
    }

    @Override
    protected void setDistance(MapNode n, double distance) {
        var incr = Globals.Rnd.nextInt((int) distance);
        unvisited.remove(n); // remove node from old place in the queue
        distances.put(n, distance + incr); // update distance
        unvisited.add(n); // insert node to the new place in the queue
    }
}
