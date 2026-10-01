package com.example.autoaim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Simple JSON config stored at .minecraft/config/autoaim.json. */
public final class AutoAimConfig {
    public static final double DEFAULT_AIM_RANGE = 10.0;
    private static final double MIN_RANGE = 1.0;
    private static final double MAX_RANGE = 64.0;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static AutoAimConfig instance = new AutoAimConfig();

    /** Maximum targeting distance in blocks. */
    public double aimRange = DEFAULT_AIM_RANGE;
    /** If true, any valid player in range beats any mob. */
    public boolean playerPriority = true;
    /** If true, a player entering range replaces a current MOB lock (player locks are never replaced). */
    public boolean playerPreemptsMob = true;
    public boolean targetPlayers = true;
    public boolean targetMobs = true;
    /** Show "Target: name" in the action bar when the target changes. */
    public boolean showTargetName = true;

    private transient double rangeSquared = DEFAULT_AIM_RANGE * DEFAULT_AIM_RANGE;

    public static AutoAimConfig get() {
        return instance;
    }

    public double aimRangeSquared() {
        return rangeSquared;
    }

    private void sanitize() {
        if (!Double.isFinite(aimRange)) {
            aimRange = DEFAULT_AIM_RANGE;
        }
        aimRange = Math.max(MIN_RANGE, Math.min(MAX_RANGE, aimRange));
        rangeSquared = aimRange * aimRange;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("autoaim.json");
        AutoAimConfig loaded = null;

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                loaded = GSON.fromJson(reader, AutoAimConfig.class);
            } catch (IOException | RuntimeException e) {
                AutoAimClient.LOGGER.warn("Could not read autoaim.json, using defaults", e);
            }
        }
        if (loaded == null) {
            loaded = new AutoAimConfig();
        }
        loaded.sanitize();
        instance = loaded;

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(loaded, writer);
        } catch (IOException e) {
            AutoAimClient.LOGGER.warn("Could not write autoaim.json", e);
        }
    }
}
