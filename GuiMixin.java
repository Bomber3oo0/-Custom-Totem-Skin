package dev.playertotemskin.mixin;

import dev.playertotemskin.render.TotemAnimation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void playertotemskin$renderTotems(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        TotemAnimation.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
