package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The core piece of the soul armor set. It owns the soul pool and the Soul Rage flag for the whole
 * set; the other three pieces ({@link AdditionalSoulArmorPiece}) only widen the cap and unlock one
 * Soul Rage effect each.
 */
public class SoulChestplateItem extends ArmorItem {

    public static final String SOUL_AMOUNT = "soul_armory.soul_armor.soulAmount";

    public static final String LAST_UPDATE_GAME_TIME = "soul_armory.soul_armor.lastUpdateGameTime";

    public static final String RAGE_ACTIVE = "soul_armory.soul_armor.rageActive";

    public static final String LAST_RAGE_UPDATE_GAME_TIME = "soul_armory.soul_armor.lastRageUpdateGameTime";

    // Re-applied on this cadence while raging rather than every tick, because every successful
    // effect refresh sends a packet to the client.
    private static final int NIGHT_VISION_REFRESH_INTERVAL = 20;

    public SoulChestplateItem(Properties pProperties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, pProperties);
    }

    // Get how many pieces of soul armor is equipped on player. Chestplate excluded.
    private static int getAdditionalPieceAmount(Player player) {
        int i = 0;
        if (isSlotEquippedWithSoulArmor(player,EquipmentSlot.HEAD)) i++;
        if (isSlotEquippedWithSoulArmor(player,EquipmentSlot.LEGS)) i++;
        if (isSlotEquippedWithSoulArmor(player,EquipmentSlot.FEET)) i++;
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

    // -------------------------------------------------------------------------
    // Soul pool access. The soul pool belongs to the chestplate the player is wearing, so every
    // one of these takes the player and returns a no-op / zero when no chestplate is equipped.
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
        return isRaging(getWornChestplate(player));
    }

    /**
     * Starts Soul Rage if the player is wearing a charged enough chestplate.
     * <p>
     * Rage cannot be called off: running the soul pool dry is the only way out. In particular
     * taking the chestplate off does not stop it — the drain keeps running on the stack wherever
     * it ends up, so stripping it mid-rage is not an escape hatch. Pressing the key again while
     * a rage is already running therefore does nothing.
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

    // Does not break
    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    // ArmorItem's constructor forces a durability onto the properties, so unlike the soul weapons
    // the getDamage override above is not enough on its own.
    @Override
    public boolean canBeDepleted() {
        return false;
    }

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

        float soul = NBT.getFloat(SOUL_AMOUNT);

        // Losing a piece lowers the cap, so trim any soul that no longer fits. Done every tick
        // rather than on an unequip hook because there is no single place a piece can leave from:
        // dropping, dying, hoppers and inventory drags all bypass one. Only meaningful while worn:
        // off the body there is no wearer to read the other pieces from, and getMaxSoulForArmor
        // would report a cap of zero.
        if (equipped) soul = Math.min(soul, getMaxSoulForArmor(player));

        if (NBT.getBoolean(RAGE_ACTIVE)) {
            // Burns soul wherever the chestplate is — worn, in a backpack, or in a chest. Only the
            // effects are tied to actually wearing it.
            soul = applyRageDrain(NBT, soul, level.getGameTime());
            if (equipped) applyRageEffects(level, player);
        } else {
            soul = applySoulDecay(NBT, soul, level.getGameTime());
        }

        // Every NBT write resyncs the whole slot to the client, so only write on an actual change.
        if (soul != NBT.getFloat(SOUL_AMOUNT)) NBT.putFloat(SOUL_AMOUNT, soul);

        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    /**
     * Burns the soul that Soul Rage owes for the time since this last ran, and ends the rage once
     * the pool is empty — which is the only thing that ends it.
     * <p>
     * Billed off a timestamp rather than a fixed subtraction per tick, the same way the soul
     * weapons handle their decay. That keeps the drain honest across stretches where this never
     * runs at all, such as the chestplate sitting in a chest, instead of letting a rage be paused
     * by putting the armor away.
     */
    private float applyRageDrain(CompoundTag NBT, float soul, long currentGameTime) {
        // An older stack that went into rage before this timestamp existed: start billing now
        // rather than charging it for every tick since the world was created.
        if (!NBT.contains(LAST_RAGE_UPDATE_GAME_TIME)) {
            NBT.putLong(LAST_RAGE_UPDATE_GAME_TIME, currentGameTime);
            return soul;
        }

        long elapsed = currentGameTime - NBT.getLong(LAST_RAGE_UPDATE_GAME_TIME);
        if (elapsed <= 0) return soul;

        // Nothing owed yet — leave both timestamps alone so a configured drain rate of zero, which
        // is a deliberate way to ask for an endless rage, doesn't resync the slot every tick.
        double drained = elapsed * Config.soulArmorRageSoulPerTick;
        if (drained <= 0) return soul;

        NBT.putLong(LAST_RAGE_UPDATE_GAME_TIME, currentGameTime);

        // Idle decay is suspended during rage, and holding its timestamp at the current tick means
        // its grace period starts counting from the moment the rage ends rather than from the last
        // soul gained — otherwise a long rage would be followed by a large backdated decay.
        NBT.putLong(LAST_UPDATE_GAME_TIME, currentGameTime);

        soul = (float) Math.max(0, soul - drained);
        if (soul <= 0) NBT.putBoolean(RAGE_ACTIVE, false);

        return soul;
    }

    /**
     * Applies the soul owed to idle decay and bills the time it consumed, so decay is charged
     * exactly once whether this runs every tick or catches up after a spell in a chest.
     */
    private float applySoulDecay(CompoundTag NBT, float soul, long currentGameTime) {
        // Nothing left to decay. Leaving the timestamp stale is fine — the next soul gained
        // refreshes it — and it saves rewriting (and so resyncing) it forever on an empty pool.
        if (soul <= 0) return soul;

        long lastUpdate = NBT.getLong(LAST_UPDATE_GAME_TIME);
        long ticksDecaying = (currentGameTime - lastUpdate) - Config.soulArmorGracePeriod;
        if (ticksDecaying <= 0) return soul;

        int decay = (int) (ticksDecaying / Config.soulArmorSoulDecaySpeed);
        if (decay <= 0) return soul;

        NBT.putLong(LAST_UPDATE_GAME_TIME, lastUpdate + (long) decay * Config.soulArmorSoulDecaySpeed);
        return Math.max(0, soul - decay);
    }

    // The rage effects that have to be pushed onto the player every tick. The ones that only read
    // state when something happens — damage reduction, lifesteal, fall immunity, movement speed —
    // live on their events in ModForgeEvents instead.
    private void applyRageEffects(Level level, Player player) {
        // Soul Helmet: night vision, which outlives the rage that granted it.
        if (isSlotEquippedWithSoulArmor(player, EquipmentSlot.HEAD)
                && level.getGameTime() % NIGHT_VISION_REFRESH_INTERVAL == 0) {
            // Topped back up to the full linger duration on every refresh, so it always has the
            // configured time left over when rage ends. Deliberately not removed at that point:
            // letting it run down is what produces the lingering effect, and it also means we
            // never cut short a night vision potion the player drank themselves.
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
     * The Soul Leggings movement speed bonus, as a fraction, for the shared soul speed modifier in
     * {@code ModForgeEvents}. Same units as {@link CanApplySpeedBoost#getSpeedAdditionPercentage}.
     * <p>
     * Note this shares {@code speedBoostCeil} with the soul weapons: at the default ceiling of 2x
     * the rage bonus alone saturates it, so a held soul weapon adds nothing on top until the
     * ceiling is turned off in the config.
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
