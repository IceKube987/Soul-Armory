package com.iceKube.soulArmory.entities;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.utils.ModDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;

public class BladeWaveEntity extends Projectile {

    private static final float SPEED = (float) Config.bladeWaveSpeed;
    private static final int LIFETIME = Config.bladeWaveLifetime;
    private static final double BLADE_LENGTH = Config.bladeWaveLength;
    private static final double HALF_LENGTH = BLADE_LENGTH / 2.0; // 1.5 by default
    private static final double HIT_AABB_GAP = 0.15;
    private static final double PARTICLE_GAP = 0.1;
    private static final double AABB_HALF_SIZE = 0.05; // 0.1 x 0.1 → half = 0.05

    /**
     * Normalized travel direction, set once at spawn.
     */
    private static final EntityDataAccessor<Vector3f> SYNC_MOVE_DIR = SynchedEntityData.defineId(BladeWaveEntity.class, EntityDataSerializers.VECTOR3);

    /**
     * "Right" vector relative to travel direction and world-up axis.
     * Computed once at spawn: moveDirection × (0,1,0)
     */
    private static final EntityDataAccessor<Vector3f> SYNC_RIGHT_VEC = SynchedEntityData.defineId(BladeWaveEntity.class, EntityDataSerializers.VECTOR3);

    /**
     * Entities collected for damage this tick.
     */
    private final HashSet<LivingEntity> target = new HashSet<>();

    /**
     * Entities already dealt damage to (not hurt again).
     */
    private final HashSet<LivingEntity> hitTarget = new HashSet<>();

    /**
     * Position at the start of the previous tick (used for AABB sweep).
     */
    private Vec3 prevTickPos = null;

    private float damage = 0;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BladeWaveEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    /**
     * Factory helper — spawn a BladeWaveEntity with an owner and a travel direction.
     *
     * @param owner     The player (or entity) that fired this wave.
     * @param direction Normalized travel direction vector.
     * @param level     The level to spawn into.
     * @return A fully initialised BladeWaveEntity ready to be added to the world.
     */
    public static BladeWaveEntity create(LivingEntity owner, Vec3 direction, Level level, float damage) {
        BladeWaveEntity entity = new BladeWaveEntity(EntityRegistry.BLADE_WAVE.get(), level);
        entity.setPos(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
        entity.setOwner(owner);
        entity.damage = damage;

        Vec3 normalized = direction.normalize();
        // Precompute right vector once — direction is constant
        Vec3 rightVec = normalized.cross(new Vec3(0, 1, 0)).normalize();

        entity.entityData.set(SYNC_MOVE_DIR, new Vector3f((float) normalized.x, (float) normalized.y, (float) normalized.z));
        entity.entityData.set(SYNC_RIGHT_VEC, new Vector3f((float) rightVec.x, (float) rightVec.y, (float) rightVec.z));

        return entity;
    }

    // -------------------------------------------------------------------------
    // Required override
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SYNC_MOVE_DIR, new Vector3f(0, 0, 0));
        this.entityData.define(SYNC_RIGHT_VEC, new Vector3f(0, 0, 0));
    }

    // -------------------------------------------------------------------------
    // Behaviour overrides
    // -------------------------------------------------------------------------

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    /**
     * Do not stop on block contact — passes through walls.
     */
    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        // intentionally empty
    }

    /**
     * Do not react to entity contact via the projectile hit path.
     */
    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        // intentionally empty — damage is handled in tick()
    }

    // -------------------------------------------------------------------------
    // Main tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        // 1. Record position at start of this tick (before moving)
        Vec3 currentPos = position();
        Vec3 sweepFrom = (prevTickPos != null) ? prevTickPos : currentPos;

        // 2. Move forward
        Vec3 newPos = currentPos.add(getMoveDirection().scale(SPEED));
        setPos(newPos.x, newPos.y, newPos.z);

        // 3. Generate hit AABBs, collect enemies
        collectEnemiesAlongRightVec(newPos, sweepFrom);

        // 4. Spawn firework particles
        // TODO: visual effect
//        spawnParticles(newPos);

        // 5. Damage entities in target but not yet in hitTarget
        applyDamage();

        // 6. Update tracking sets
        hitTarget.addAll(target);
        target.clear();

        // 7. Update prevTickPos for next tick
        prevTickPos = newPos;

        // 8. Lifetime check
        if (tickCount >= LIFETIME) {
            discard();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Walk along rightVec in both directions from the entity center,
     * generate 0.1×0.1 AABBs inflated toward where the entity was last tick,
     * and collect all Enemy LivingEntities inside them into Target.
     */
    private void collectEnemiesAlongRightVec(Vec3 pos, Vec3 prevPos) {
        Vec3 sweepDelta = prevPos.subtract(pos); // vector from current pos back to last tick pos

        // d = 0 is center; iterate outward until the offset reaches HALF_LENGTH
        for (double d = 0; d < HALF_LENGTH; d += HIT_AABB_GAP) {
            if (d == 0) {
                collectFromAABB(buildSweptAABB(pos, sweepDelta));
            } else {
                // right side
                Vec3 rightPoint = pos.add(getRightVector().scale(d));
                collectFromAABB(buildSweptAABB(rightPoint, sweepDelta));
                // left side
                Vec3 leftPoint = pos.subtract(getRightVector().scale(d));
                collectFromAABB(buildSweptAABB(leftPoint, sweepDelta));
            }
        }
    }

    public Vec3 getMoveDirection() {
        Vector3f v = this.entityData.get(SYNC_MOVE_DIR);
        return new Vec3(v.x(), v.y(), v.z());
    }

    public Vec3 getRightVector() {
        Vector3f v = this.entityData.get(SYNC_RIGHT_VEC);
        return new Vec3(v.x(), v.y(), v.z());
    }

    /**
     * Build a 0.1×0.1 AABB centerd on {@code center}, then inflate it
     * toward {@code sweepDelta} to form the swept volume for this tick.
     */
    private AABB buildSweptAABB(Vec3 center, Vec3 sweepDelta) {
        AABB box = new AABB(
                center.x - AABB_HALF_SIZE, center.y - AABB_HALF_SIZE, center.z - AABB_HALF_SIZE,
                center.x + AABB_HALF_SIZE, center.y + AABB_HALF_SIZE, center.z + AABB_HALF_SIZE
        );
        // Inflate toward the previous position
        return box.inflate(
                Math.abs(sweepDelta.x),
                Math.abs(sweepDelta.y),
                Math.abs(sweepDelta.z)
        );
    }

    private void collectFromAABB(AABB aabb) {
        List<LivingEntity> found = level().getEntitiesOfClass(LivingEntity.class, aabb);
        for (LivingEntity entity : found) {
            if (entity instanceof Enemy) {
                target.add(entity);
            }
        }
    }

    /**
     * Spawn firework particles at 0.1-block intervals along rightVec,
     * both left and right of the entity center.
     */
    private void spawnParticles(Vec3 pos) {
        for (double d = 0; d < HALF_LENGTH; d += PARTICLE_GAP) {
            if (d == 0) {
                spawnFireworkParticle(pos);
            } else {
                spawnFireworkParticle(pos.add(getRightVector().scale(d)));
                spawnFireworkParticle(pos.subtract(getRightVector().scale(d)));
            }
        }
    }

    private void spawnFireworkParticle(Vec3 point) {
        level().addParticle(
                ParticleTypes.FIREWORK,
                point.x, point.y, point.z,
                0.0, 0.0, 0.0   // no velocity; purely visual marker
        );
    }

    /**
     * Hurt every entity in Target that has not yet been hit (not in hitTarget).
     * Uses vanilla playerAttack damage with the owner as attacker if available.
     */
    private void applyDamage() {
        Entity ownerEntity = getOwner();
        DamageSource source = ModDamageTypes.skillDamage(level(), ownerEntity);

        for (LivingEntity entity : target) {
            if (!hitTarget.contains(entity)) {
                entity.hurt(source, damage);
            }
        }
    }
}
