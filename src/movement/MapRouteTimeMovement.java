package movement;

import annotations.IFS;
import core.Coord;
import flu.HostContamination;
import flu.Room;
import flu.RoomMapper;
import core.Settings;
import core.SimClock;
import jdk.jshell.spi.ExecutionControl;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.NaSPF;
import movement.map.SimMap;
import schedules.*;

import java.util.LinkedList;
import java.util.List;

public class MapRouteTimeMovement extends MapBasedMovement implements SwitchableMovement {
    @IFS("scheduleLoop")
    private static boolean scheduleDoLoop = false;
    @IFS("scheduleNumberWantedActivities")
    private static int scheduleNumberWantedActivities = 15;
    @IFS("scheduleTryLimit")
    private static int scheduleTryLimit = 30;
    @IFS("scheduleIncludeWeekend")
    private static boolean scheduleIncludeWeekend = true;
    @IFS("type")
    private static int type = 0;
    private static WeeklyScheduleBuilder scheduleBuilder = new WeeklyScheduleBuilder();
    private static StaffScheduleBuilder staffScheduleBuilder;
    /**
     * the Dijkstra shortest path finder
     */
    private DijkstraPathFinder pathFinder;
    /**
     * Persons schedule
     */
    private Schedule schedule;
    private boolean unreached = true;
    private boolean goInactiveNextReached = false;
    private double nextActive;
    private double nextActiveWhenReached;
    private String lastLocationTag = null;
    private Coord lastLocation = null;
    private boolean isActive = true;
    private int lastLunch = 0;


    /**
     * Current Position and Contamination status
     */
    private Room currentRoom;
    private double arrivalTime;

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

        staffScheduleBuilder = new StaffScheduleBuilder();

        scheduleBuilder.setNumberWantedActivities(scheduleNumberWantedActivities)
                .setTryLimit(scheduleTryLimit).setDoLoop(scheduleDoLoop)
                .setIncludeWeekend(scheduleIncludeWeekend);
        staffScheduleBuilder.setNumberWantedActivities(scheduleNumberWantedActivities)
                .setTryLimit(scheduleTryLimit).setDoLoop(scheduleDoLoop)
                .setIncludeWeekend(scheduleIncludeWeekend);
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

        if (type == 0) {
            this.schedule = scheduleBuilder.build();
        } else if (type == 1) {
            this.schedule = staffScheduleBuilder.build();
        } else {
            throw new RuntimeException("Not yet implemented");
        }
    }

    public Coord convertTag(String tag) {
        var rooms = Globals.RoomMapping.map.get(tag);
        int idx = Globals.Rnd.nextInt(rooms.size());
        var room = rooms.get(idx);
        return new Coord(room.PosX, room.PosY);
    }

    @Override
    public double nextPathAvailable() {
        return nextActive;
    }

    private void reachActivity(Coord location, Activity activity, String locationTag) {
        if(goInactiveNextReached){
            neverActive();
        }

        nextActive = nextActiveWhenReached;
        System.out.println(host + " reached: " + activity + " @ " + location);
        var roomList = Globals.RoomMapping.map.get(locationTag);

        // entries have automatic doors and will not add to contamination
        if (!Tags.GO_HOME.toString().equals(locationTag)) {
            for (var room : roomList) {
                if (room.getCoord().equals(location)) {
                    // found the correct room
                    arrivalTime = SimClock.getTime();
                    currentRoom = room;

                    var subject = host.hostContamination;
                    room.addContamination(subject.getContamination());
                    break;
                }
            }
        }
    }

    private void leaveActivity() {
        if(currentRoom != null) {
            var subject = host.hostContamination;
            subject.addContamination(currentRoom.getContamination(),
                    (int) (SimClock.getTime() - arrivalTime));
        }
    }

    public Coord getNextCoordinate() {

        int time = (int) SimClock.getTime();

        var currentActivity = schedule.getCurrentActivity(time);
        var nextActivity = schedule.getNextActivity(time);

        String locationTag;
        Activity pursuedActivity = null;
        if (currentActivity.isPresent() && nextActivity.isPresent()) {
            var ca = currentActivity.get().getKey();
            var ca_delta = currentActivity.get().getValue();
            var na = nextActivity.get().getKey();
            var na_delta = nextActivity.get().getValue();
            // Something to do right now and also after this

            if (na_delta < 15 * 60) {
                pursuedActivity = na;
                activeAfter(na, na_delta);
                locationTag = na.location;
            } else {
                pursuedActivity = ca;
                activeAfter(ca, ca_delta);
                locationTag = ca.location;
            }

        } else if (currentActivity.isPresent()) {
            var ca = currentActivity.get().getKey();
            var ca_delta = currentActivity.get().getValue();
            // Something to do right now but nothing after this
            pursuedActivity = ca;
            activeAfter(ca, ca_delta);
            locationTag = ca.location;

        } else if (nextActivity.isPresent()) {
            var na = nextActivity.get().getKey();
            var na_delta = nextActivity.get().getValue();
            // Nothing to do right now but something later

            // Go home if next activity is at the next day
            // Go home if gap is larger than 5 hours
            if (na_delta > 5 * 3600) {
                locationTag = Tags.GO_HOME.toString();
                activeBefore(na, na_delta);
            } else if (na_delta <= 15 * 60) { // Less than 15 minutes to next lecture -> go there
                locationTag = na.location;
                activeAfter(na, na_delta);
            } else if (lastLunch < time - 30 * 60) {
                locationTag = Tags.EATING.toString();
                activeBefore(na, na_delta);
            } else if (na_delta > 3600 && lastLunch < time - 13 * 3600) {
                lastLunch = time;
                locationTag = Tags.EAT.toString();
                activeBefore(na, 10 * 60);
            } else { // Study
                locationTag = Tags.SEATING.toString();
                activeBefore(na, na_delta);
            }
        } else {
            // Nothing to do right now and nothing later
            locationTag = Tags.GO_HOME.toString();
            goInactiveNextReached = true;
        }

        Coord location = convertTag(locationTag);
        if (locationTag.equals(lastLocationTag)) {
            if (unreached) {
                reachActivity(location, pursuedActivity, locationTag);
                unreached = false;
            }
            location = lastLocation;
        } else {
            leaveActivity();
            unreached = true;
            lastLocation = location;
        }
        lastLocationTag = locationTag;
        return location;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    private void neverActive() {
        isActive = false;
    }

    private void activeBefore(Activity activity, int delta) {
        nextActiveWhenReached = SimClock.getTime() + delta - Globals.Rnd.nextInt(10 * 60) - 5 * 60;
    }

    private void activeAfter(Activity activity, int delta) {
        nextActiveWhenReached = SimClock.getTime() + delta + activity.duration - Globals.Rnd.nextInt(10 * 60) - 5 * 60;
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());
        SimMap map = this.getMap();
        List<MapNode> allNodes = map.getNodes();
        Coord sketch = getNextCoordinate();
        MapNode nextNode = map.getNodeByCoord(sketch);

        System.out.println(lastMapNode + " - " + nextNode + " - " + sketch);
        List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, nextNode);
        // this assertion should never fire if the map is checked in read phase
        assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
                nextNode + ". The simulation map isn't fully connected";


        for (MapNode node : nodePath) { // create a Path from the shortest path
            p.addWaypoint(node.getLocation());
        }

        lastMapNode = nextNode;

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
