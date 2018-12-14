package movement;

import annotations.IFS;
import annotations.IFSProcessor;
import core.Coord;
import core.Settings;
import core.SimClock;
import flu.Room;
import flu.RoomMapper;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.NaSPF;
import movement.map.SimMap;
import schedules.*;

import java.util.List;

public class MapRouteTimeMovement extends MapBasedMovement implements SwitchableMovement {
    @IFS("scheduleLoop")
    private boolean scheduleDoLoop = false;
    @IFS("scheduleNumberWantedActivities")
    private int scheduleNumberWantedActivities = 15;
    @IFS("scheduleTryLimit")
    private int scheduleTryLimit = 30;
    @IFS("scheduleIncludeWeekend")
    private boolean scheduleIncludeWeekend = true;
    @IFS("type")
    private int type = 0;
    @IFS("debug")
    private boolean debug = false;
    private WeeklyScheduleBuilder scheduleBuilder;
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
        IFSProcessor.initialize(this, settings);
        pathFinder = new NaSPF(getOkMapNodeTypes());

        Globals.RoomMapping = new RoomMapper(settings);
        Globals.GlobSched = new GlobalSchedule(settings);

        switch (type) {
            case 0:
                scheduleBuilder = new WeeklyScheduleBuilder();
                break;
            case 1:
                scheduleBuilder = new StaffScheduleBuilder();
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }

        scheduleBuilder.setNumberWantedActivities(scheduleNumberWantedActivities)
                .setTryLimit(scheduleTryLimit).setDoLoop(scheduleDoLoop)
                .setIncludeWeekend(scheduleIncludeWeekend);
    }

    /**
     * Copyconstructor.
     *
     * @param proto The MapRouteMovement prototype
     */
    protected MapRouteTimeMovement(MapRouteTimeMovement proto) {
        super(proto);
        this.type = proto.type;
        this.pathFinder = proto.pathFinder;
        this.schedule = proto.scheduleBuilder.build();
    }

    private void log(String s) {
        if (debug) {
            System.out.println(s);
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
        if (goInactiveNextReached) {
            neverActive();
        }

        nextActive = nextActiveWhenReached;
        log(host + " reached: " + activity + " @ " + location);
        var roomList = Globals.RoomMapping.map.get(locationTag);

        boolean supportsContamination = true;
        // entries have automatic doors and will not add to contamination
        if (Tags.GO_HOME.toString().equals(locationTag)) {
            supportsContamination = false;
        }
        for (var room : roomList) {
            if (room.getCoord().equals(location)) {
                // found the correct room
                arrivalTime = SimClock.getTime();
                currentRoom = room;

                if (supportsContamination) {
                    var subject = host.hostContamination;
                    room.addContamination(subject.getContamination());
                }
                break;
            }
        }
    }

    private void leaveActivity() {
        if (currentRoom != null && !Tags.GO_HOME.toString().equals(currentRoom.Tag)) {
            var subject = host.hostContamination;

            if (Globals.RoomMapping.wcs.contains(currentRoom.Tag)) {
                subject.changeContamination(0.2);
            }

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
            } else if (lastLunch > time - 30 * 60) {
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
        nextActiveWhenReached =
                SimClock.getTime() + delta + activity.duration - Globals.Rnd.nextInt(10 * 60) -
                        5 * 60;
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
