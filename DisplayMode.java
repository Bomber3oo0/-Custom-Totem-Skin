package dev.playertotemskin.config;

public enum DisplayMode {
    THREE_D("3D-Modell"),
    TWO_D("2D-Modell"),
    HEAD("Nur Kopf"),
    CLASSIC("Klassisch");

    public final String label;

    DisplayMode(String label) {
        this.label = label;
    }

    public DisplayMode next() {
        DisplayMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
