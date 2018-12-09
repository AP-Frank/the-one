package movement;

import annotations.IFS;
import core.Coord;
import core.RoomMapper;
import core.Settings;
import core.SimClock;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.NaSPF;
import movement.map.SimMap;
import schedules.*;


import java.util.List;

public class MapRouteTimeMovement extends MapBasedMovement implements SwitchableMovement {
    /**
     * the Dijkstra shortest path finder
     */
    private DijkstraPathFinder pathFinder;

    /**
     * Persons schedule
     */
    private Schedule schedule;
    private WeeklyScheduleBuilder scheduleBuilder = new WeeklyScheduleBuilder();

    @IFS("scheduleLoop")
    private static boolean scheduleDoLoop = false;
    @IFS("scheduleNumberWantedActivities")
    private static int scheduleNumberWantedActivities = 15;
    @IFS("scheduleTryLimit")
    private static int scheduleTryLimit = 30;
    @IFS("scheduleIncludeWeekend")
    private static boolean scheduleIncludeWeekend = true;

    /**
     * Creates a new movement model based on a Settings object's settings.
     *
     * @param settings The Settings object where the settings are read from
     */
    public MapRouteTimeMovement(Settings settings) {
        super(settings);
        pathFinder = new NaSPF(getOkMapNodeTypes());

        Globals.RoomMapping = new RoomMapper(settings);
        Globals.GlobSched = new GlobalSchedule(settings);
        scheduleBuilder.setNumberWantedActivities(scheduleNumberWantedActivities)
                .setTryLimit(scheduleTryLimit).setDoLoop(scheduleDoLoop).setIncludeWeekend(scheduleIncludeWeekend);
    }

    /**
     * Copyconstructor. Gives a route to the new movement model from the
     * list of routes and randomizes the starting position.
     *
     * @param proto The MapRouteMovement prototype
     */
    protected MapRouteTimeMovement(MapRouteTimeMovement proto) {
        super(proto);
        this.pathFinder = proto.pathFinder;
        this.schedule = scheduleBuilder.build();
    }

    public Coord convertTag(String tag){
        System.out.println(tag);
        var room = Globals.RoomMapping.map.get(tag);
        return new Coord(room.PosX, room.PosY);
    }

    public Coord getNextCoordinate(){
        int time = (int)SimClock.getTime();

        // TODO Go home, go study, etc.
        var currentActivity = schedule.getCurrentActivity(time);
        var nextActivity = schedule.getNextActivity(time);

        String locationTag = "";
        if(currentActivity.isPresent() && nextActivity.isPresent()){
            var ca = currentActivity.get().getKey();
            var ca_delta = currentActivity.get().getValue();
            var na = nextActivity.get().getKey();
            var na_delta = nextActivity.get().getValue();
            // Something to do right now and also after this

            if(na_delta < 15 * 60) {
                locationTag = na.location;
            } else{
                locationTag = ca.location;
            }

        } else if(currentActivity.isPresent()){
            var ca = currentActivity.get().getKey();
            var ca_delta = currentActivity.get().getValue();
            // Something to do right now but nothing after this
            locationTag = ca.location;

        } else if(nextActivity.isPresent()){
            var na = nextActivity.get().getKey();
            var na_delta = nextActivity.get().getValue();
            // Nothing to do right now but something later

            // Go home if next activity is at the next day
            // Go home if gap is larger than 8 hours
            if(na_delta > 8 * 3600){
                locationTag = Tags.GO_HOME.toString();
            } else {
                // TODO Go study, lunch, etc.
                locationTag = Tags.EAT.toString();
            }
        } else {
            // Nothing to do right now and nothing later
            locationTag = Tags.GO_HOME.toString();
        }

        return convertTag(locationTag);
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());
        SimMap map = this.getMap();
        List<MapNode> allNodes = map.getNodes();
        Coord sketch = getNextCoordinate();
        MapNode nextNode = map.getNodeByCoord(sketch);

        List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, nextNode);
        // this assertion should never fire if the map is checked in read phase
        assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
                nextNode + ". The simulation map isn't fully connected";

        double time = SimClock.getTime();
        //in this example, x0 has a break during timing interval [1000ms,4000ms] and moves around
        // the three nodes:
        double beginBreak = 1000;
        double endBreak = 4000;
        if (time >= beginBreak && time <= endBreak) {

            for (MapNode node : nodePath) { // create a Path from the shortest path
                p.addWaypoint(node.getLocation());
            }

            lastMapNode = nextNode;

        }

        return p;
    }


    /**
     * Returns the first stop on the route
     */
    @Override
    public Coord getInitialLocation() {
        if (lastMapNode == null) {
            lastMapNode = this.getMap().getNodeByCoord(new Coord(0, 0));
        }

        return lastMapNode.getLocation().clone();
    }

    @Override
    public Coord getLastLocation() {
        if (lastMapNode != null) {
            return lastMapNode.getLocation().clone();
        } else {
            return null;
        }
    }


    @Override
    public MapRouteTimeMovement replicate() {
        return new MapRouteTimeMovement(this);
    }

}
