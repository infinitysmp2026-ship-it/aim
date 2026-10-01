package com.example.autoaim;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Aim math + applying rotation to the local player. Hard lock: no smoothing, no randomness. */
public final class AimController {
    private AimController() {}

    public static void aimAt(ClientPlayerEntity player, LivingEntity target) {
        Vec3d eye = player.getEyePos();

        // Eye/head height, clamped inside the hitbox so odd-sized mobs stay sensible.
        Box box = target.getBoundingBox();
        double tx = target.getX();
        double ty = MathHelper.clamp(target.getEyeY(), box.minY, box.maxY);
        double tz = target.getZ();

        double dx = tx - eye.x;
        double dy = ty - eye.y;
        double dz = tz - eye.z;
        double horizontalSq = dx * dx + dz * dz;

        // Minecraft: yaw 0 = +Z (south), -90 = +X (east); positive pitch = looking down.
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(horizontalSq)));
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);

        float currentYaw = player.getYaw();
        float newYaw = currentYaw;
        if (horizontalSq > 1.0E-7) {
            float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            // Shortest-arc delta keeps the stored yaw continuous (no 360-degree flips).
            newYaw = currentYaw + MathHelper.wrapDegrees(targetYaw - currentYaw);
        }

        player.setYaw(newYaw);
        player.setPitch(pitch);
    }
}
