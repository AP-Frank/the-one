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

public class RoomMapper {
    public static HashMap<String, Room> map = new HashMap<>();

    public static void initialize(String pathToJson){
        Type targetClassType = new TypeToken<ArrayList<Room>>() { }.getType();
        ArrayList<Room> rooms = new Gson().fromJson(readFile(pathToJson), targetClassType);

        for (Room room: rooms) {
            map.put(room.Tag, room);
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
