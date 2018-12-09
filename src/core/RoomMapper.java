package core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    public HashMap<String, LinkedList<Room>> map = new HashMap<>();

    public RoomMapper(Settings settings){
        String currentNs = settings.getNameSpace();
        settings.setNameSpace("MapBasedMovement");

        String pathToJson = settings.getSetting(PATH_ROOM_MAPPING);
        settings.setNameSpace(currentNs);

        Type targetClassType = new TypeToken<ArrayList<Room>>() { }.getType();
        ArrayList<Room> rooms = new Gson().fromJson(readFile(pathToJson), targetClassType);

        for (Room room: rooms) {
            if(map.containsKey(room.Tag)){
                map.get(room.Tag).add(room);
            } else {
                var list = new LinkedList<Room>();
                list.add(room);
                map.put(room.Tag, list);
            }
        }
    }

    private static String readFile(String path)
    {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw  new IllegalArgumentException();
        }
        return new String(encoded, Charset.forName("UTF-8"));
    }
}
