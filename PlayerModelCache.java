package dev.playertotemskin.render;

import dev.playertotemskin.config.TotemConfig;
import dev.playertotemskin.mixin.PlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UUID-basierter Cache fuer die Darstellungs-Puppen. Jede Puppe ist ein reiner Client-Spieler (nie in der Welt),
 * der dasselbe GameProfile nutzt. Skin, Classic/Slim-Modell und Cape loest Minecraft dadurch selbst ueber den
 * SkinManager (asynchron, gecacht) anhand der UUID auf. Es gibt keine eigenen Netzwerkanfragen.
 */
public final class PlayerModelCache {
    private record Entry(RemotePlayer puppet, long createdAt) {}

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    // accessOrder=true -> LRU
    private static final Map<UUID, Entry> CACHE = new LinkedHashMap<>(16, 0.75f, true);

    private PlayerModelCache() {}

    public static synchronized RemotePlayer get(Minecraft mc, Player source) {
        TotemConfig cfg = TotemConfig.get();
        ClientLevel level = mc.level;
        long now = System.currentTimeMillis();
        UUID id = source.getUUID();

        Entry e = CACHE.get(id);
        if (e != null) {
            boolean expired = !cfg.skinCaching || now - e.createdAt() > cfg.cacheMinutes * 60_000L;
            if (expired || e.puppet().level() != level) {
                CACHE.remove(id);
                e = null;
            }
        }
        if (e == null) {
            RemotePlayer puppet = new RemotePlayer(level, source.getGameProfile());
            puppet.setSilent(true);
            puppet.setPose(Pose.STANDING);
            e = new Entry(puppet, now);
            CACHE.put(id, e);
            trim(cfg.maxCachedSkins);
        }
        sync(e.puppet(), source, cfg);
        return e.puppet();
    }

    private static void trim(int max) {
        Iterator<Map.Entry<UUID, Entry>> it = CACHE.entrySet().iterator();
        while (CACHE.size() > max && it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    /** Uebernimmt Ruestung und Skin-Ebenen live vom echten Spieler (gemaess Einstellungen). */
    private static void sync(RemotePlayer puppet, Player source, TotemConfig cfg) {
        for (EquipmentSlot slot : ARMOR) {
            ItemStack wanted = ItemStack.EMPTY;
            if (cfg.showArmor) {
                wanted = source.getItemBySlot(slot).copy();
                if (!cfg.showEnchantments && !wanted.isEmpty()) {
                    wanted.remove(DataComponents.ENCHANTMENTS);
                    wanted.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                }
            }
            ItemStack current = puppet.getItemBySlot(slot);
            if (!ItemStack.isSameItemSameComponents(current, wanted)) {
                puppet.setItemSlot(slot, wanted);
            }
        }
        puppet.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        puppet.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        byte layers = cfg.showSecondLayer
                ? source.getEntityData().get(PlayerAccessor.playertotemskin$modelCustomisation())
                : 0;
        puppet.getEntityData().set(PlayerAccessor.playertotemskin$modelCustomisation(), layers);
    }

    /** Beim Verlassen einer Welt/eines Servers aufrufen, damit nichts auf alte Welten verweist. */
    public static synchronized void clear() {
        CACHE.clear();
    }
}
