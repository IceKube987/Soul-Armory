package com.iceKube.soulArmory.loot;

import com.google.common.base.Suppliers;
import com.iceKube.soulArmory.Config;
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
                    .apply(inst, AncientCityLootModifier::new)));

    public AncientCityLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < Config.swordSpawnChance) {
            generatedLoot.add(new ItemStack(ItemRegistry.INCOMPLETE_SOUL_SWORD.get()));
        }
        if (context.getRandom().nextFloat() < Config.bowSpawnChance) {
            generatedLoot.add(new ItemStack(ItemRegistry.INCOMPLETE_SOUL_BOW.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
