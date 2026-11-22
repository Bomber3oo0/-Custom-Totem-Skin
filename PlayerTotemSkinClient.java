package dev.playertotemskin;

import dev.playertotemskin.config.TotemConfig;
import dev.playertotemskin.render.PlayerModelCache;
import dev.playertotemskin.render.TotemAnimation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class PlayerTotemSkinClient implements ClientModInitializer {
    public static final String MOD_ID = "playertotemskin";

    @Override
    public void onInitializeClient() {
        TotemConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> TotemAnimation.tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PlayerModelCache.clear();
            TotemAnimation.clear();
        });
    }
}
