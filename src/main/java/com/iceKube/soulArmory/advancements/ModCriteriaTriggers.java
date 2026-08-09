package com.iceKube.soulArmory.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ModCriteriaTriggers {

    public static final SoulActionTrigger SOUL_ACTION = new SoulActionTrigger();


    public static void register() {
        CriteriaTriggers.register(SOUL_ACTION);
    }

    /** No-ops off the logical server, so call sites never need their own side check. */
    public static void grant(Player player, SoulAction action) {
        if (player instanceof ServerPlayer serverPlayer) {
            SOUL_ACTION.trigger(serverPlayer, action);
        }
    }

    /**
     * Shared by the Sonic Boom skill, the Sonic Overload skill and the incomplete bow's one-shot
     * blast, all three of which damage their targets through the same kind of loop.
     *
     * @param hurt what the target's {@code hurt} call returned — false while it is still in its
     *             invulnerability window
     */
    public static void onSonicSkillHit(Player player, LivingEntity target, boolean hurt) {
        if (!hurt || target.getType() != EntityType.WARDEN) return;

        grant(player, SoulAction.WARDEN_SONIC_HIT);

        // hurt() calls die() synchronously, so this is already accurate for the killing blow.
        if (target.isDeadOrDying()) {
            grant(player, SoulAction.WARDEN_SONIC_KILL);
        }
    }

    private ModCriteriaTriggers() {}
}
