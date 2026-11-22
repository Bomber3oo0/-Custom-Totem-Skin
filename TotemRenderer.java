package dev.playertotemskin.render;

import dev.playertotemskin.config.DisplayMode;
import dev.playertotemskin.config.TotemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;

/** Zeichnet die Totem-Darstellung eines Spielers in die GUI (HUD-Animation und Vorschau). */
public final class TotemRenderer {
    private TotemRenderer() {}

    /**
     * @param size   Kantenlaenge der Darstellung in GUI-Pixeln
     * @param ticks  Zeit in Ticks (fuer die dezente Schwenk-Animation)
     */
    public static void draw(GuiGraphics g, Minecraft mc, Player source, int cx, int cy, int size, float ticks) {
        TotemConfig cfg = TotemConfig.get();
        if (size < 4 || cfg.displayMode == DisplayMode.CLASSIC) {
            return;
        }
        RemotePlayer puppet = PlayerModelCache.get(mc, source);

        float mx = cx;
        boolean sway = cfg.animations && cfg.rotation && cfg.displayMode != DisplayMode.TWO_D;
        if (sway) {
            // Der GUI-Renderer dreht das Modell zur Maus: eine pendelnde "Maus" ergibt eine ruhige Drehbewegung.
            mx = cx + (float) Math.sin(ticks * 0.12f * cfg.animationSpeed) * size * 0.9f;
        }

        switch (cfg.displayMode) {
            case THREE_D, TWO_D -> {
                int half = size / 2;
                int scale = Math.max(1, (int) (size / 1.9f));
                float my = cy - size * 0.30f; // Blick geradeaus
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, cx - half, cy - half, cx + half, cy + half, scale, 0.0625f, mx, my, puppet);
            }
            case HEAD -> {
                // Grosses Modell, nur der Kopf-Bereich wird per Scissor ausgeschnitten -> Hut-Ebene und Helm
                // werden vom normalen Spieler-Renderer korrekt mitgezeichnet.
                int half = size / 2;
                int scale = size * 2;
                int centerY = cy + (int) (size * 1.30f);
                g.enableScissor(cx - half, cy - half, cx + half, cy + half);
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, cx - size, centerY - size, cx + size, centerY + size, scale, 0.0625f, mx, cy, puppet);
                g.disableScissor();
            }
            default -> { }
        }
    }
}
