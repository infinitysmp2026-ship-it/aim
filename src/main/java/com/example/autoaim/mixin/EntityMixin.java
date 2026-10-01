package com.example.autoaim.mixin;

import com.example.autoaim.AutoAimManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mouse movement reaches the player through Entity#changeLookDirection. Fabric API has no event for
 * this, so we cancel it for the local player only while a lock is active. That is what stops the
 * crosshair from being pulled off the target between ticks.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void autoaim$blockMouseWhileLocked(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity && AutoAimManager.isLockActive()) {
            ci.cancel();
        }
    }
}
