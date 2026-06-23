package com.deejay.wishsmp;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClassManager {

    private static final File FOLDER =
            new File("config/wish_smp");

    private static final File FILE =
            new File(FOLDER, "PlayerClasses.yml");

    private static final Map<UUID, ClassType> CACHE = new HashMap<>();

    private static Yaml yaml;

    static {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        yaml = new Yaml(options);
    }

    /**
     * Called on server start
     */
    public static void init() {
        createFileIfMissing();
        load();
    }

    /**
     * Creates folder + file if they don't exist
     */
    private static void createFileIfMissing() {

        try {
            if (!FOLDER.exists()) {
                FOLDER.mkdirs();
            }

            if (!FILE.exists()) {
                FILE.createNewFile();

                Map<String, Object> root = new HashMap<>();
                root.put("Players", new HashMap<>());

                saveRaw(root);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all players into memory
     */
    public static void load() {

        try (InputStream input = new FileInputStream(FILE)) {

            Map<String, Object> data = yaml.load(input);

            if (data == null || !data.containsKey("Players")) {
                return;
            }

            Map<String, Object> players =
                    (Map<String, Object>) data.get("Players");

            for (String uuidStr : players.keySet()) {

                Map<String, Object> info =
                        (Map<String, Object>) players.get(uuidStr);

                String className = (String) info.get("Class");

                if (className != null) {
                    CACHE.put(
                            UUID.fromString(uuidStr),
                            ClassType.valueOf(className)
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save memory → file
     */
    public static void save() {

        Map<String, Object> root = new HashMap<>();
        Map<String, Object> players = new HashMap<>();

        for (UUID uuid : CACHE.keySet()) {

            Map<String, Object> info = new HashMap<>();
            info.put("Class", CACHE.get(uuid).name());

            players.put(uuid.toString(), info);
        }

        root.put("Players", players);

        saveRaw(root);
    }

    /**
     * Internal file write
     */
    private static void saveRaw(Map<String, Object> data) {

        try (FileWriter writer = new FileWriter(FILE)) {
            yaml.dump(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------------
    // PUBLIC API
    // ----------------------------

    public static boolean hasClass(UUID uuid) {
        return CACHE.containsKey(uuid);
    }

    public static ClassType getClass(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void setClass(UUID uuid, ClassType type) {
        CACHE.put(uuid, type);
        save();
    }

    public static void removeClass(UUID uuid) {
        CACHE.remove(uuid);
        save();
    }
}
