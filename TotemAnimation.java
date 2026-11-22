package dev.playertotemskin.render;

import dev.playertotemskin.config.DisplayMode;
import dev.playertotemskin.config.MissingSkinBehavior;
import dev.playertotemskin.config.TotemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Verwaltet laufende Totem-Animationen (eigene gross in der Mitte, fremde klein am rechten Rand). */
public final class TotemAnimation {
    private static final int BASE_DURATION = 40;
    private static final int MAX_ACTIVE = 4;

    private static final class Active {
        final UUID uuid;
        final boolean own;
        int age;

        Active(UUID uuid, boolean own) {
            this.uuid = uuid;
            this.own = own;
        }
    }

    private static final List<Active> ACTIVE = new ArrayList<>();

    private TotemAnimation() {}

    /** Wird fuer jeden Totem-Einsatz im Sichtbereich aufgerufen. */
    public static boolean start(Player player) {
        Minecraft mc = Minecraft.getInstance();
        TotemConfig cfg = TotemConfig.get();
        if (mc.player == null || cfg.displayMode == DisplayMode.CLASSIC || !cfg.showSkin) {
            return false;
        }
        // Fehlende Skin-Daten: je nach Einstellung auf das klassische Totem zurueckfallen.
        boolean hasInfo = mc.getConnection() != null && mc.getConnection().getPlayerInfo(player.getUUID()) != null;
        boolean own = player == mc.player;
        if (!own && !hasInfo && cfg.missingSkin == MissingSkinBehavior.CLASSIC) {
            return false;
        }
        ACTIVE.removeIf(a -> a.uuid.equals(player.getUUID()));
        if (ACTIVE.size() >= MAX_ACTIVE) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Active(player.getUUID(), own));
        return true;
    }

    private static int duration() {
        TotemConfig cfg = TotemConfig.get();
        float speed = cfg.animations ? cfg.animationSpeed : 1.0f;
        return Math.max(10, Math.round(BASE_DURATION / speed));
    }

    public static void tick() {
        int dur = duration();
        ACTIVE.removeIf(a -> ++a.age >= dur);
    }

    public static void clear() {
        ACTIVE.clear();
    }

    public static void render(GuiGraphics g, float partial) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        int w = g.guiWidth();
        int h = g.guiHeight();
        int dur = duration();
        boolean anim = TotemConfig.get().animations;
        int otherIndex = 0;

        for (Active a : ACTIVE) {
            Player p = mc.level.getPlayerByUUID(a.uuid);
            if (p == null) {
                continue;
            }
            float t = Math.min(1f, (a.age + partial) / dur);
            float s = anim ? curve(t) : 1f;
            if (a.own) {
                int size = (int) (Math.min(w, h) * 0.55f * s);
                TotemRenderer.draw(g, mc, p, w / 2, h / 2, size, a.age + partial);
            } else {
                int size = (int) (Math.min(w, h) * 0.22f * s);
                int cy = h / 4 + otherIndex * (int) (Math.min(w, h) * 0.26f);
                TotemRenderer.draw(g, mc, p, w - (int) (w * 0.12f), cy, size, a.age + partial);
                otherIndex++;
            }
        }
    }

    /** Einblenden mit leichtem Ueberschwingen, kurz halten, wieder ausblenden. */
    private static float curve(float t) {
        if (t < 0.2f) {
            float x = t / 0.2f;
            return x * x * (3f - 2f * x) * 1.08f;
        }
        if (t < 0.3f) {
            return 1.08f - 0.08f * ((t - 0.2f) / 0.1f);
        }
        if (t < 0.8f) {
            return 1f;
        }
        float x = (t - 0.8f) / 0.2f;
        return 1f - x * x;
    }
}
