package flu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import core.Settings;
import flu.Room;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class RoomMapper {
    private static String PATH_ROOM_MAPPING = "pathToRoomMapping";
    private static String PATH_OFFICES = "pathToOffices";
    private static String PATH_WCS = "pathToWCs";
    public HashMap<String, LinkedList<Room>> map = new HashMap<>();
    public ArrayList<String> offices;
    public ArrayList<String> wcs;

    public RoomMapper(Settings settings) {
        String pathToJson = getStringFromSettings(settings, PATH_ROOM_MAPPING);

        Type targetClassType = new TypeToken<ArrayList<Room>>() {
        }.getType();
        ArrayList<Room> rooms = new Gson().fromJson(readFile(pathToJson), targetClassType);

        for (Room room : rooms) {
            final String[] tags = room.Tag.split(";");
            for (var tag : tags) {
                if (map.containsKey(tag)) {
                    map.get(tag).add(room);
                } else {
                    var list = new LinkedList<Room>();
                    list.add(room);
                    map.put(tag, list);
                }
            }
        }

        String pathToOffices = getStringFromSettings(settings, PATH_OFFICES);

        Type roomListT = new TypeToken<ArrayList<String>>() {
        }.getType();
        offices = new Gson().fromJson(readFile(pathToOffices), roomListT);

        String pathToWCs = getStringFromSettings(settings, PATH_WCS);
        wcs = new Gson().fromJson(readFile(pathToWCs), roomListT);
    }

    private String getStringFromSettings(Settings settings, String key) {
        String currentNs;
        currentNs = settings.getNameSpace();
        settings.setNameSpace("MapBasedMovement");

        String pathToOffices = settings.getSetting(key);
        settings.setNameSpace(currentNs);
        return pathToOffices;
    }

    private static String readFile(String path) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return new String(encoded, Charset.forName("UTF-8"));
    }
}
