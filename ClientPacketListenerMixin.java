package dev.playertotemskin.mixin;

import dev.playertotemskin.render.TotemAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    /** Entity-Event 35 = Totem der Unsterblichkeit wurde ausgeloest. */
    @Inject(
            method = "handleEntityEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundEntityEventPacket;getEntity(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private void playertotemskin$onTotem(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (packet.getEventId() != 35 || this.level == null) {
            return;
        }
        Entity entity = packet.getEntity(this.level);
        if (entity instanceof Player player && player != Minecraft.getInstance().player) {
            TotemAnimation.start(player);
        }
    }
}
