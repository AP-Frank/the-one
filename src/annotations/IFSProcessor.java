package annotations;

import core.Settings;
import util.Range;

import java.lang.reflect.Field;

/**
 * Processes the IFS annotations of the fields of the given objects and initializes them based on the given settings.
 */
public final class IFSProcessor {

    private IFSProcessor() {
    }

    /**
     * Processes the IFS annotations of the fields of the given objects and initializes them based on the given settings.
     * @param obj Object to initialize
     * @param settings Settings to read the initialization from
     */
    public static void initialize(Object obj, Settings settings) {

        // Reflection magic below

        var fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(IFS.class)) {
                    continue;
                }
                var settingName = field.getAnnotation(IFS.class).value();

                var type = field.getType();
                var def = field.get(obj);

                Object val = null;
                if (type == boolean.class) {
                    val = settings.getBoolean(settingName, (boolean) def);
                } else if (type == int.class) {
                    val = settings.getInt(settingName, (int) def);
                } else if (type == double.class) {
                    val = settings.getDouble(settingName, (double) def);
                } else if (type == float.class) {
                    throw new IllegalArgumentException("Float settings not available, use double instead");
                } else if (type == String.class) {
                    val = settings.getSetting(settingName, (String) def);
                } else if (type == long.class) {
                    if ((long) def == 0) {
                        throw new IllegalArgumentException("Long settings can't have a default value");
                    }
                    val = settings.getLong(settingName);
                } else if (type == short.class) {
                    throw new IllegalArgumentException("Short settings not available, use int instead");
                } else if (type == boolean[].class) {
                    throw new IllegalArgumentException("Boolean arrays are not available");
                } else if (type == int[].class) {
                    val = settings.getCsvInts(settingName);
                } else if (type == double[].class) {
                    val = settings.getCsvDoubles(settingName);
                } else if (type == float[].class) {
                    throw new IllegalArgumentException("Float array settings not available, use double arrays instead");
                } else if (type == String[].class) {
                    val = settings.getCsvSetting(settingName);
                } else if (type == long[].class) {
                    throw new IllegalArgumentException("Long array settings not available, use int arrays instead");
                } else if (type == short[].class) {
                    throw new IllegalArgumentException("Short arrays settings not available, use int arrays instead");
                } else if (type == Range[].class) {
                    val = settings.getCsvRanges(settingName);
                }
                // Add more data types if necessary
                else {
                    throw new IllegalArgumentException("Data type " + type.getSimpleName() +
                            " not supported for the initialization via annotations");
                }

                field.set(obj, val);
            } catch (IllegalAccessException e) {
                // Can't happen; was set accessible
            }
        }
    }

}
