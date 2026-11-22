package dev.playertotemskin.config;

/** Verhalten, wenn fuer einen Spieler (noch) keine Skin-Daten vorliegen. */
public enum MissingSkinBehavior {
    CLASSIC("Klassisches Totem"),
    DEFAULT_SKIN("Standard-Skin");

    public final String label;

    MissingSkinBehavior(String label) {
        this.label = label;
    }

    public MissingSkinBehavior next() {
        MissingSkinBehavior[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
