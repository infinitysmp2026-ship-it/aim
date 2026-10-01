package com.example.autoaim;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Holds enabled state + current target and runs the per-tick logic. Does nothing while disabled. */
public final class AutoAimManager {
    private static final int PREEMPT_CHECK_INTERVAL_TICKS = 4;

    private static boolean enabled;
    private static LivingEntity target;
    private static ClientWorld lastWorld;
    private static int preemptCooldown;

    private AutoAimManager() {}

    /** True while the camera is hard-locked onto something (used by the mouse mixin). */
    public static boolean isLockActive() {
        return enabled && target != null;
    }

    public static void toggle(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        enabled = !enabled;
        target = null;
        preemptCooldown = 0;
        announce(client.player);
    }

    /** Clears everything tied to a world/connection. */
    public static void reset() {
        target = null;
        lastWorld = null;
        preemptCooldown = 0;
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            reset();
            return;
        }
        // New ClientWorld instance = world or dimension change.
        if (world != lastWorld) {
            target = null;
            lastWorld = world;
        }
        if (!enabled || client.currentScreen != null) {
            return;
        }

        AutoAimConfig cfg = AutoAimConfig.get();

        if (target != null && !TargetSelector.isValid(player, world, target, cfg)) {
            target = null;
        }

        if (target == null) {
            setTarget(player, TargetSelector.findBest(player, world, cfg, false));
        } else if (cfg.playerPriority && cfg.playerPreemptsMob
                && !(target instanceof PlayerEntity) && --preemptCooldown <= 0) {
            preemptCooldown = PREEMPT_CHECK_INTERVAL_TICKS;
            LivingEntity playerTarget = TargetSelector.findBest(player, world, cfg, true);
            if (playerTarget != null) {
                setTarget(player, playerTarget);
            }
        }

        if (target != null) {
            AimController.aimAt(player, target);
        }
    }

    private static void setTarget(ClientPlayerEntity player, LivingEntity newTarget) {
        if (newTarget == target) {
            return;
        }
        target = newTarget;
        if (newTarget != null && AutoAimConfig.get().showTargetName) {
            announce(player);
        }
    }

    private static void announce(ClientPlayerEntity player) {
        MutableText text = Text.literal("Auto Aim: " + (enabled ? "ON" : "OFF"))
                .formatted(enabled ? Formatting.GREEN : Formatting.RED);
        if (enabled && target != null && AutoAimConfig.get().showTargetName) {
            text.append(Text.literal("  Target: ").formatted(Formatting.GRAY))
                    .append(target.getName().copy().formatted(Formatting.WHITE));
        }
        player.sendMessage(text, true); // true = action bar, no chat spam
    }
}
