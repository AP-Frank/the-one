package report;

import core.DTNHost;
import schedules.Globals;

import java.util.List;

public class RoomReport extends SamplingReport {
    @Override protected void sample(List<DTNHost> hosts) {
        for (var roomList : Globals.RoomMapping.map.values()) {
            for (var room : roomList) {
                room.updateAggregate();
            }
        }
    }

    @Override public void done() {
        for (var roomList : Globals.RoomMapping.map.values()) {
            for (var room : roomList) {
                super.write(room.PosX + ", " + room.PosY + ", " + room.getCurrentMean().mean);
            }
        }

        super.done();
    }
}
