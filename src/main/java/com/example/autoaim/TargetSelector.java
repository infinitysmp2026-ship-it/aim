package com.example.autoaim;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/** Finds and validates targets. Player > mob, closest wins within a category. */
public final class TargetSelector {
    private TargetSelector() {}

    /** Full validity check, also used to decide whether to keep an existing lock. */
    public static boolean isValid(ClientPlayerEntity self, ClientWorld world, LivingEntity e, AutoAimConfig cfg) {
        if (e == null || e == self || e.isRemoved() || !e.isAlive() || e.isSpectator()) {
            return false;
        }
        if (e instanceof ArmorStandEntity) {
            return false;
        }
        if (e instanceof PlayerEntity) {
            if (!cfg.targetPlayers) return false;
        } else if (!cfg.targetMobs) {
            return false;
        }
        // Must still be the loaded entity the world knows under that id.
        if (world.getEntityById(e.getId()) != e) {
            return false;
        }
        return self.squaredDistanceTo(e) <= cfg.aimRangeSquared();
    }

    /**
     * @param playersOnly only return a player (used to check if a player should replace a mob lock)
     * @return best target or null
     */
    public static LivingEntity findBest(ClientPlayerEntity self, ClientWorld world, AutoAimConfig cfg, boolean playersOnly) {
        Box searchBox = self.getBoundingBox().expand(cfg.aimRange);
        List<LivingEntity> candidates = world.getEntitiesByClass(
                LivingEntity.class, searchBox, e -> isValid(self, world, e, cfg));

        LivingEntity bestPlayer = null;
        LivingEntity bestMob = null;
        double bestPlayerDist = Double.MAX_VALUE;
        double bestMobDist = Double.MAX_VALUE;

        for (LivingEntity e : candidates) {
            double d = self.squaredDistanceTo(e);
            if (e instanceof PlayerEntity) {
                if (d < bestPlayerDist) {
                    bestPlayerDist = d;
                    bestPlayer = e;
                }
            } else if (d < bestMobDist) {
                bestMobDist = d;
                bestMob = e;
            }
        }

        if (playersOnly) {
            return bestPlayer;
        }
        if (cfg.playerPriority) {
            return bestPlayer != null ? bestPlayer : bestMob;
        }
        if (bestPlayer == null) return bestMob;
        if (bestMob == null) return bestPlayer;
        return bestPlayerDist <= bestMobDist ? bestPlayer : bestMob;
    }
}
