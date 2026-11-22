package dev.playertotemskin.gui;

import dev.playertotemskin.config.TotemConfig;
import dev.playertotemskin.render.TotemRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class TotemSettingsScreen extends Screen {
    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int ROW = 23;

    private final Screen parent;
    private final TotemConfig cfg = TotemConfig.get();
    private final TotemConfig backup = cfg.copy();

    public TotemSettingsScreen(Screen parent) {
        super(Component.literal("Player Totem Skin"));
        this.parent = parent;
    }

    private static Component onOff(String name, boolean v) {
        return Component.literal(name + ": " + (v ? "Ein" : "Aus"));
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(onOff(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(onOff(name, get.getAsBoolean()));
        }).bounds(x, y, BTN_W, BTN_H).build());
    }

    @Override
    protected void init() {
        int c1 = 10;
        int c2 = c1 + BTN_W + 6;
        int y0 = 34;

        // --- Spalte 1: Darstellung + Animationen ---
        addRenderableWidget(Button.builder(Component.literal("Modus: " + cfg.displayMode.label), b -> {
            cfg.displayMode = cfg.displayMode.next();
            b.setMessage(Component.literal("Modus: " + cfg.displayMode.label));
        }).bounds(c1, y0, BTN_W, BTN_H).build());
        toggle(c1, y0 + ROW, "Skin anzeigen", () -> cfg.showSkin, v -> cfg.showSkin = v);
        toggle(c1, y0 + ROW * 2, "Rüstung", () -> cfg.showArmor, v -> cfg.showArmor = v);
        toggle(c1, y0 + ROW * 3, "Verzauberungen", () -> cfg.showEnchantments, v -> cfg.showEnchantments = v);
        toggle(c1, y0 + ROW * 4, "Zweite Skin-Ebene", () -> cfg.showSecondLayer, v -> cfg.showSecondLayer = v);
        toggle(c1, y0 + ROW * 5, "Animationen", () -> cfg.animations, v -> cfg.animations = v);
        toggle(c1, y0 + ROW * 6, "Drehung", () -> cfg.rotation, v -> cfg.rotation = v);

        // --- Spalte 2: Animationstempo + Performance ---
        addRenderableWidget(new Slider(c2, y0, "Tempo", 0.5, 2.0, cfg.animationSpeed, false,
                v -> cfg.animationSpeed = (float) v));
        toggle(c2, y0 + ROW, "Skin-Caching", () -> cfg.skinCaching, v -> cfg.skinCaching = v);
        addRenderableWidget(new Slider(c2, y0 + ROW * 2, "Max. Skins", 4, 128, cfg.maxCachedSkins, true,
                v -> cfg.maxCachedSkins = (int) Math.round(v)));
        addRenderableWidget(new Slider(c2, y0 + ROW * 3, "Aktualisierung (Min)", 1, 60, cfg.cacheMinutes, true,
                v -> cfg.cacheMinutes = (int) Math.round(v)));
        addRenderableWidget(Button.builder(Component.literal("Fehlender Skin: " + cfg.missingSkin.label), b -> {
            cfg.missingSkin = cfg.missingSkin.next();
            b.setMessage(Component.literal("Fehlender Skin: " + cfg.missingSkin.label));
        }).bounds(c2, y0 + ROW * 4, BTN_W, BTN_H).build());

        // --- Unten: Standardwerte / Zurücksetzen / Fertig ---
        int by = this.height - 28;
        addRenderableWidget(Button.builder(Component.literal("Standardwerte"), b -> {
            cfg.copyFrom(new TotemConfig());
            rebuildWidgets();
        }).bounds(c1, by, BTN_W, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("Änderungen verwerfen"), b -> {
            cfg.copyFrom(backup);
            rebuildWidgets();
        }).bounds(c2, by, BTN_W, BTN_H).build());
        addRenderableWidget(Button.builder(Component.literal("Speichern & Schließen"), b -> onClose())
                .bounds(c2 + BTN_W + 6, by, BTN_W, BTN_H).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        // Live-Vorschau rechts: liest die Einstellungen jeden Frame -> aktualisiert sich sofort.
        int left = 10 + (BTN_W + 6) * 2 + 4;
        int right = this.width - 10;
        int top = 34;
        int bottom = this.height - 36;
        if (right - left < 60) {
            return;
        }
        g.fill(left, top, right, bottom, 0x90000000);
        g.renderOutline(left, top, right - left, bottom - top, 0xFF55AA55);
        g.drawCenteredString(this.font, Component.literal("Vorschau"), (left + right) / 2, top + 4, 0xFFAAAAAA);

        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) {
            g.drawCenteredString(this.font, Component.literal("Vorschau nur in einer Welt"),
                    (left + right) / 2, (top + bottom) / 2, 0xFFFFFFFF);
            return;
        }
        int size = Math.min(right - left - 16, bottom - top - 32);
        float ticks = (System.currentTimeMillis() % 3_600_000L) / 50f;
        TotemRenderer.draw(g, this.minecraft, this.minecraft.player,
                (left + right) / 2, (top + bottom) / 2 + 6, size, ticks);
        g.drawCenteredString(this.font, Component.literal(cfg.displayMode.label),
                (left + right) / 2, bottom - 12, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        TotemConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private static final class Slider extends AbstractSliderButton {
        private final String name;
        private final double min;
        private final double max;
        private final boolean integer;
        private final DoubleConsumer onChange;

        Slider(int x, int y, String name, double min, double max, double current, boolean integer,
               DoubleConsumer onChange) {
            super(x, y, BTN_W, BTN_H, Component.empty(), (current - min) / (max - min));
            this.name = name;
            this.min = min;
            this.max = max;
            this.integer = integer;
            this.onChange = onChange;
            updateMessage();
        }

        private double real() {
            return min + (max - min) * this.value;
        }

        @Override
        protected void updateMessage() {
            String v = integer ? String.valueOf(Math.round(real())) : String.format("%.2f", real());
            setMessage(Component.literal(name + ": " + v));
        }

        @Override
        protected void applyValue() {
            onChange.accept(real());
        }
    }
}
