package dev.playertotemskin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class TotemConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("playertotemskin.json");
    private static TotemConfig instance = new TotemConfig();

    // Darstellung
    public DisplayMode displayMode = DisplayMode.THREE_D;
    public boolean showSkin = true;
    public boolean showArmor = true;
    public boolean showEnchantments = true;
    public boolean showSecondLayer = true;

    // Animationen
    public boolean animations = true;
    public boolean rotation = true;
    public float animationSpeed = 1.0f;

    // Performance
    public boolean skinCaching = true;
    public int maxCachedSkins = 32;
    public int cacheMinutes = 10;
    public MissingSkinBehavior missingSkin = MissingSkinBehavior.CLASSIC;

    public static TotemConfig get() {
        return instance;
    }

    public TotemConfig copy() {
        TotemConfig c = new TotemConfig();
        c.copyFrom(this);
        return c;
    }

    public void copyFrom(TotemConfig o) {
        displayMode = o.displayMode;
        showSkin = o.showSkin;
        showArmor = o.showArmor;
        showEnchantments = o.showEnchantments;
        showSecondLayer = o.showSecondLayer;
        animations = o.animations;
        rotation = o.rotation;
        animationSpeed = o.animationSpeed;
        skinCaching = o.skinCaching;
        maxCachedSkins = o.maxCachedSkins;
        cacheMinutes = o.cacheMinutes;
        missingSkin = o.missingSkin;
    }

    /** Ersetzt ungueltige Werte (z.B. aus handeditierter Datei) durch sichere Grenzen. */
    private void sanitize() {
        if (displayMode == null) displayMode = DisplayMode.THREE_D;
        if (missingSkin == null) missingSkin = MissingSkinBehavior.CLASSIC;
        animationSpeed = Math.max(0.25f, Math.min(3.0f, animationSpeed));
        maxCachedSkins = Math.max(1, Math.min(256, maxCachedSkins));
        cacheMinutes = Math.max(1, Math.min(120, cacheMinutes));
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader r = Files.newBufferedReader(FILE)) {
                TotemConfig loaded = GSON.fromJson(r, TotemConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (Exception e) {
                System.err.println("[PlayerTotemSkin] Konfiguration defekt, nutze Standardwerte: " + e);
                instance = new TotemConfig();
            }
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        instance.sanitize();
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(instance, w);
        } catch (IOException e) {
            System.err.println("[PlayerTotemSkin] Konfiguration konnte nicht gespeichert werden: " + e);
        }
    }
}
