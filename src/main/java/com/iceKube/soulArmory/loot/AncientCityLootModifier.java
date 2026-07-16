package com.iceKube.soulArmory.loot;

import com.google.common.base.Suppliers;
import com.iceKube.soulArmory.registries.ItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AncientCityLootModifier extends LootModifier {

    public static final Supplier<Codec<AncientCityLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(Codec.FLOAT.fieldOf("sword_chance").forGetter(m -> m.swordChance))
                    .and(Codec.FLOAT.fieldOf("bow_chance").forGetter(m -> m.bowChance))
                    .apply(inst, AncientCityLootModifier::new)));

    private final float swordChance;
    private final float bowChance;

    public AncientCityLootModifier(LootItemCondition[] conditions, float swordChance, float bowChance) {
        super(conditions);
        this.swordChance = swordChance;
        this.bowChance = bowChance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < swordChance) {
            generatedLoot.add(new ItemStack(ItemRegistry.INCOMPLETE_SOUL_SWORD.get()));
        }
        if (context.getRandom().nextFloat() < bowChance) {
            generatedLoot.add(new ItemStack(ItemRegistry.INCOMPLETE_SOUL_BOW.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
