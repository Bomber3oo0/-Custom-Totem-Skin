package dev.playertotemskin.mixin;

import dev.playertotemskin.render.TotemAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void playertotemskin$replaceVanilla(ItemStack stack, RandomSource random, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (stack.is(Items.TOTEM_OF_UNDYING) && mc.player != null && TotemAnimation.start(mc.player)) {
            ci.cancel();
        }
    }
}
