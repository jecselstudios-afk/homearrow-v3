package com.example.homearrow;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Stores the single home point in a small properties file inside the config folder. */
public final class HomeStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("homearrow.properties");

    public record Home(double x, double y, double z, String dimension) {}

    private static Home home;

    private HomeStore() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            p.load(reader);
            home = new Home(
                    Double.parseDouble(p.getProperty("x")),
                    Double.parseDouble(p.getProperty("y")),
                    Double.parseDouble(p.getProperty("z")),
                    p.getProperty("dimension", "minecraft:overworld"));
        } catch (IOException | RuntimeException e) {
            System.err.println("[HomeArrow] Could not read home file: " + e.getMessage());
            home = null;
        }
    }

    public static void set(Home newHome) {
        home = newHome;
        Properties p = new Properties();
        p.setProperty("x", Double.toString(newHome.x()));
        p.setProperty("y", Double.toString(newHome.y()));
        p.setProperty("z", Double.toString(newHome.z()));
        p.setProperty("dimension", newHome.dimension());
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                p.store(writer, "Home Arrow home point");
            }
        } catch (IOException e) {
            System.err.println("[HomeArrow] Could not save home file: " + e.getMessage());
        }
    }

    public static void clear() {
        home = null;
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException e) {
            System.err.println("[HomeArrow] Could not delete home file: " + e.getMessage());
        }
    }

    public static Home get() {
        return home;
    }
}
