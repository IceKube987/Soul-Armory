package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class SoulChestplateItem extends ArmorItem {

    public static final String SOUL_AMOUNT = "soul_armory.soul_armor.soulAmount";

    public static final String LAST_UPDATE_GAME_TIME = "soul_armory.soul_armor.lastUpdateGameTime";

    public static final String RAGE_ACTIVE = "soul_armory.soul_armor.rageActive";

    public static final String LAST_RAGE_UPDATE_GAME_TIME = "soul_armory.soul_armor.lastRageUpdateGameTime";

    // Re-applied on this cadence while raging rather than every tick.
    private static final int NIGHT_VISION_REFRESH_INTERVAL = 20;

    public SoulChestplateItem(Properties pProperties) {
        super(ModArmorMaterials.SOUL_ARMOR, Type.CHESTPLATE, pProperties);
    }

    // Get how many pieces of soul armor is equipped on player. Chestplate excluded.
    private static int getAdditionalPieceAmount(Player player) {
        int i = 0;
        if (isSlotEquippedWithSoulArmor(player, EquipmentSlot.HEAD)) i++;
        if (isSlotEquippedWithSoulArmor(player, EquipmentSlot.LEGS)) i++;
        if (isSlotEquippedWithSoulArmor(player, EquipmentSlot.FEET)) i++;
        return i;
    }

    public static boolean isSlotEquippedWithSoulArmor(Player player, EquipmentSlot equipmentSlot) {
        if (player.getItemBySlot(equipmentSlot).getItem() instanceof AdditionalSoulArmorPiece) return true;
        if (equipmentSlot == EquipmentSlot.CHEST && player.getItemBySlot(equipmentSlot).getItem() instanceof SoulChestplateItem)
            return true;
        return false;
    }

    public static int getMaxSoulForArmor(Player player) {
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SoulChestplateItem)) return 0;
        return Config.soulChestplateMaxSoul + Config.soulArmorAdditionalSoul * getAdditionalPieceAmount(player);
    }

    /**
     * @return true if the player is wearing all four pieces. The chestplate is checked separately
     * because {@link #getAdditionalPieceAmount} deliberately excludes it.
     */
    public static boolean isFullSet(Player player) {
        return !getWornChestplate(player).isEmpty() && getAdditionalPieceAmount(player) == 3;
    }

    /**
     * {@link #isFullSet} plus the config switch: the gate every set bonus branch goes through.
     */
    public static boolean isFullSetBonusActive(Player player) {
        return Config.soulArmorFullSetBonusEnabled && isFullSet(player);
    }

    // -------------------------------------------------------------------------
    // Soul pool access. The soul pool belongs to the chestplate the player is wearing, so every
    // one of these takes the player and returns a null or zero when no chestplate is equipped.
    // -------------------------------------------------------------------------

    /**
     * @return the worn soul chestplate, or {@link ItemStack#EMPTY} if the player isn't wearing one
     */
    public static ItemStack getWornChestplate(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        return stack.getItem() instanceof SoulChestplateItem ? stack : ItemStack.EMPTY;
    }

    public static float getSoul(ItemStack chestplate) {
        CompoundTag tag = chestplate.getTag();
        if (tag == null) return 0;
        return tag.getFloat(SOUL_AMOUNT);
    }

    /**
     * Adds soul to the worn chestplate, capped at the set's current maximum, and marks this as the
     * moment the player last gained soul so the idle decay grace period restarts.
     */
    public static void addSoul(Player player, float amount) {
        if (amount <= 0) return;
        ItemStack chestplate = getWornChestplate(player);
        if (chestplate.isEmpty()) return;

        CompoundTag tag = chestplate.getOrCreateTag();
        float newSoul = Math.min(getMaxSoulForArmor(player), tag.getFloat(SOUL_AMOUNT) + amount);

        tag.putFloat(SOUL_AMOUNT, newSoul);
        tag.putLong(LAST_UPDATE_GAME_TIME, player.level().getGameTime());
    }

    public static boolean isRaging(ItemStack chestplate) {
        CompoundTag tag = chestplate.getTag();
        return tag != null && tag.getBoolean(RAGE_ACTIVE);
    }

    public static boolean isRaging(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof IncompleteSoulChestplateItem prototype) return prototype.isActive(chest);
        return isRaging(getWornChestplate(player));
    }

    /**
     * Starts Soul Rage if the player is wearing a charged enough chestplate.
     *
     * @return true if this call started Soul Rage
     */
    public static boolean tryActivateRage(Player player) {
        ItemStack chestplate = getWornChestplate(player);
        if (chestplate.isEmpty()) return false;

        CompoundTag tag = chestplate.getOrCreateTag();
        if (tag.getBoolean(RAGE_ACTIVE)) return false;
        if (tag.getFloat(SOUL_AMOUNT) < Config.soulArmorRageThreshold) return false;

        tag.putBoolean(RAGE_ACTIVE, true);
        tag.putLong(LAST_RAGE_UPDATE_GAME_TIME, player.level().getGameTime());
        return true;
    }

    // The material already rules out durability and the enchanting table, but Forge's anvil-side
    // defaults are not gated on that, so enchanted books still have to be turned away by hand.
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (level.isClientSide()) {
            super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
            return;
        }

        if (stack.getTag() == null) {
            CompoundTag NBT = new CompoundTag();
            NBT.putFloat(SOUL_AMOUNT, 0);
            NBT.putLong(LAST_UPDATE_GAME_TIME, level.getGameTime());
            NBT.putBoolean(RAGE_ACTIVE, false);
            NBT.putLong(LAST_RAGE_UPDATE_GAME_TIME, level.getGameTime());
            NBT.putUUID("soul_armory.instanceId", UUID.randomUUID());
            stack.setTag(NBT);
        }

        CompoundTag NBT = stack.getTag();
        boolean equipped = player.getItemBySlot(EquipmentSlot.CHEST) == stack;

        boolean fullSet = equipped && isFullSetBonusActive(player);

        float soul = NBT.getFloat(SOUL_AMOUNT);

        // Losing a piece lowers the cap, so trim any soul that no longer fits.
        if (equipped) soul = Math.min(soul, getMaxSoulForArmor(player));

        // The level the full set sustains the pool at. Decay stops here and regen climbs back to here.
        int sustain = fullSet ? Math.min(Config.soulArmorFullSetSoulFloor, getMaxSoulForArmor(player)) : 0;

        if (NBT.getBoolean(RAGE_ACTIVE)) {
            // Burns soul wherever the chestplate is — worn, in a backpack, or in a chest. Note the sustain floor is deliberately not
            // passed here: rage drains straight through it to zero, which is the only way it ends.
            soul = applyRageDrain(NBT, soul, level.getGameTime());
            if (equipped) applyRageEffects(level, player);
        } else {
            soul = applySoulDecay(NBT, soul, level.getGameTime(), sustain);
            if (fullSet) soul = applySoulRegen(NBT, soul, level.getGameTime(), sustain);
        }

        // Every NBT write resyncs the whole slot to the client, so only write on an actual change.
        if (soul != NBT.getFloat(SOUL_AMOUNT)) NBT.putFloat(SOUL_AMOUNT, soul);

        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    /**
     * Burns the soul that Soul Rage owes for the time since this last ran, and ends the rage once
     * the pool is empty — which is the only thing that ends it.
     */
    private float applyRageDrain(CompoundTag NBT, float soul, long currentGameTime) {
        if (!NBT.contains(LAST_RAGE_UPDATE_GAME_TIME)) {
            NBT.putLong(LAST_RAGE_UPDATE_GAME_TIME, currentGameTime);
            return soul;
        }

        long elapsed = currentGameTime - NBT.getLong(LAST_RAGE_UPDATE_GAME_TIME);
        if (elapsed <= 0) return soul;

        double drained = elapsed * Config.soulArmorRageSoulPerTick;
        if (drained <= 0) return soul;

        NBT.putLong(LAST_RAGE_UPDATE_GAME_TIME, currentGameTime);

        // Idle decay is suspended during rage.
        NBT.putLong(LAST_UPDATE_GAME_TIME, currentGameTime);

        soul = (float) Math.max(0, soul - drained);
        if (soul <= 0) NBT.putBoolean(RAGE_ACTIVE, false);

        return soul;
    }

    /**
     * Applies the soul owed to idle decay and bills the time it consumed, so decay is charged
     * exactly once whether this runs every tick or catches up after a spell in a chest.
     *
     * @param floor the level decay stops at — the full set's sustain level, or zero without it
     */
    private float applySoulDecay(CompoundTag NBT, float soul, long currentGameTime, int floor) {
        // Nothing left to decay. Leaving the timestamp stale is fine when the floor is zero — the
        // next soul gained refreshes it — and it saves rewriting (and so resyncing) it forever on
        // an empty pool. Resting on a set bonus floor is a steady state rather than a dead one,
        // though, so that case needs the credit actively thrown away instead.
        // That comment there is AI-generated. Long as heck, isn't it? Welp, keeping it might be beneficial.
        if (soul <= floor) {
            if (floor > 0) discardDeadDecayCredit(NBT, currentGameTime);
            return soul;
        }

        long lastUpdate = NBT.getLong(LAST_UPDATE_GAME_TIME);
        long ticksDecaying = (currentGameTime - lastUpdate) - Config.soulArmorGracePeriod;
        if (ticksDecaying <= 0) return soul;

        int decay = (int) (ticksDecaying / Config.soulArmorSoulDecaySpeed);
        if (decay <= 0) return soul;

        float newSoul = Math.max(floor, soul - decay);

        // Bill only the decay that actually landed, not the whole amount the elapsed time paid for:
        // the clamp may have stopped it early, and charging for decay that never happened would
        // push the timestamp into the future. Rounded up because the pool is a float — addSoul pays
        // in raw damage — and a partial point still used up its tick block.
        // This one, AI-generated too.
        long ticksSpent = (long) Math.ceil(soul - newSoul) * Config.soulArmorSoulDecaySpeed;
        NBT.putLong(LAST_UPDATE_GAME_TIME, lastUpdate + ticksSpent);
        return newSoul;
    }

    /**
     * While the pool rests on the full set's floor, the gap between the decay timestamp and now is
     * credit that can never be spent. Left to grow it would all be
     * redeemed at once the moment a set piece comes off and the floor drops away, wiping the pool.
     */
    private void discardDeadDecayCredit(CompoundTag NBT, long currentGameTime) {
        long lastUpdate = NBT.getLong(LAST_UPDATE_GAME_TIME);
        if (currentGameTime - lastUpdate <= (long) Config.soulArmorGracePeriod + Config.soulArmorSoulDecaySpeed) return;
        NBT.putLong(LAST_UPDATE_GAME_TIME, currentGameTime);
    }

    /**
     * The full set's regeneration back up to its sustain level. Ignores decay grace period.
     * this is <em>not</em> billed off a stored timestamp.
     */
    private float applySoulRegen(CompoundTag NBT, float soul, long currentGameTime, int target) {
        if (soul >= target) return soul;
        if (currentGameTime % Config.soulArmorFullSetRegenSpeed != 0) return soul;

        // Regeneration is soul gained, which is exactly what this timestamp records
        NBT.putLong(LAST_UPDATE_GAME_TIME, currentGameTime);
        return Math.min(target, soul + 1);
    }

    private void applyRageEffects(Level level, Player player) {
        // Rage burns the poison out. ModForgeEvents keeps new applications off for as long as it lasts,
        // so this only ever fires for poison that landed before the rage started.
        if (Config.soulArmorRagePoisonImmunity && player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }

        // Soul Helmet: night vision, which outlives the rage that granted it.
        if (isSlotEquippedWithSoulArmor(player, EquipmentSlot.HEAD)
                && level.getGameTime() % NIGHT_VISION_REFRESH_INTERVAL == 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    Config.soulArmorRageNightVisionLinger + NIGHT_VISION_REFRESH_INTERVAL,
                    0,
                    false,
                    false,
                    true));
        }
    }

    /**
     * The Soul Leggings movement speed bonus.
     * This shares {@code speedBoostCeil} with the soul weapons.
     */
    public static double getArmorSpeedAdditionPercentage(Player player) {
        if (!isRaging(player)) return 0;
        if (!isSlotEquippedWithSoulArmor(player, EquipmentSlot.LEGS)) return 0;
        return 0.01 * Config.soulArmorRageSpeedBonus;
    }

    // TODO: Copied from soul weapons, does not work as maximum soul amount is dynamic. Consider running appendHoverText only when equipped on player.
    // OR, simply NO tooltip. Soul amount for armor only visible via HUD.
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//        if (pStack.getTag() == null) {
//            pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + "0 / " + getMaxSoul()));
//            return;
//        }
//
//        long currentGameTime = pLevel != null ? pLevel.getGameTime() : 0;
//        float currentSoul = pStack.getTag().getFloat(SOUL_AMOUNT);
//        int effectiveSoul = ((int) Math.max(0, currentSoul - calculateSoulDecay(pStack, currentGameTime))); // cast to avoid showing decimals in tooltip
//        pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + effectiveSoul + " / " + getMaxSoul()));
    }
}
