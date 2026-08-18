package com.iceKube.soulArmory.commands;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.Forgeable;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.SkillUnlockHelper;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SoulArmoryCommand {

    // Suggests the skills the held weapon can still learn.
    private static final SuggestionProvider<CommandSourceStack> SKILL_SUGGESTIONS = (context, builder) -> {
        List<ResourceLocation> ids = new ArrayList<>();

        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof UseSoulSkillSystem skillItem) {
                List<BaseSoulSkill> known = skillItem.getAvailableSkills(stack);
                for (BaseSoulSkill skill : skillItem.getAllPossibleSkills()) {
                    if (known == null || !known.contains(skill)) {
                        ids.add(skill.soulSkillId);
                    }
                }
            }
        }

        return SharedSuggestionProvider.suggestResource(ids, builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soul-armory")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("grant-skill")
                        .then(Commands.literal("everything")
                                .executes(SoulArmoryCommand::grantEverything))
                        .then(Commands.argument("skill", ResourceLocationArgument.id())
                                .suggests(SKILL_SUGGESTIONS)
                                .executes(SoulArmoryCommand::grantOne))));
    }

    private static int grantOne(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ItemStack stack = getSkillWeapon(source, player);
        if (stack == null) return 0;

        ResourceLocation id = ResourceLocationArgument.getId(context, "skill");
        BaseSoulSkill skill = resolveSkill(id);
        if (skill == null) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.unknown_skill", id.toString()));
            return 0;
        }

        UseSoulSkillSystem skillItem = (UseSoulSkillSystem) stack.getItem();

        if (!skillItem.getAllPossibleSkills().contains(skill)) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.incompatible",
                    Component.translatable(skill.getTranslationKey()), stack.getHoverName()));
            return 0;
        }

        List<BaseSoulSkill> known = skillItem.getAvailableSkills(stack);
        if (known != null && known.contains(skill)) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.already_unlocked",
                    Component.translatable(skill.getTranslationKey())));
            return 0;
        }

        grant(player, stack, skill);

        source.sendSuccess(() -> Component.translatable("commands.soul_armory.grant_skill.success",
                Component.translatable(skill.getTranslationKey()), player.getDisplayName()), true);
        return 1;
    }

    private static int grantEverything(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        ItemStack stack = getSkillWeapon(source, player);
        if (stack == null) return 0;

        UseSoulSkillSystem skillItem = (UseSoulSkillSystem) stack.getItem();
        List<BaseSoulSkill> known = skillItem.getAvailableSkills(stack);

        int granted = 0;
        for (BaseSoulSkill skill : skillItem.getAllPossibleSkills()) {
            if (known != null && known.contains(skill)) continue;
            grant(player, stack, skill);
            granted++;
        }

        if (granted == 0) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.everything_none"));
            return 0;
        }

        int count = granted;
        source.sendSuccess(() -> Component.translatable("commands.soul_armory.grant_skill.success_multiple",
                count, player.getDisplayName()), true);
        return granted;
    }

    /**
     * @return the main hand stack, or null if it cannot take a skill. Sends the failure itself.
     */
    @Nullable
    private static ItemStack getSkillWeapon(CommandSourceStack source, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof UseSoulSkillSystem)) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.no_weapon"));
            return null;
        }

        // The NBT is only written on the weapon's first inventoryTick.
        if (!stack.hasTag()) {
            source.sendFailure(Component.translatable("commands.soul_armory.grant_skill.not_ready"));
            return null;
        }

        return stack;
    }

    // Retries ids that parsed into the minecraft namespace against this mod's namespace.
    @Nullable
    private static BaseSoulSkill resolveSkill(ResourceLocation id) {
        BaseSoulSkill skill = SoulSkills.getSkill(id);
        if (skill == null && id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            skill = SoulSkills.getSkill(new ResourceLocation(SoulArmoryMod.MODID, id.getPath()));
        }
        return skill;
    }

    // Clears the active forging task only when it is the one unlocking this skill.
    private static void grant(ServerPlayer player, ItemStack stack, BaseSoulSkill skill) {
        boolean clearForgingTask = false;
        if (stack.getItem() instanceof Forgeable forgeable) {
            ForgingTask task = forgeable.getActiveForgingTask(stack);
            clearForgingTask = task != null && task.unlockedSkill == skill;
        }

        SkillUnlockHelper.unlockSkill(player, stack, player.level(), skill, clearForgingTask);
    }
}
