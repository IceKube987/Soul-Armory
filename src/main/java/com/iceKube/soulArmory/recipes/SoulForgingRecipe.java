package com.iceKube.soulArmory.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.Forgeable;
import com.iceKube.soulArmory.registries.RecipeRegistry;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * A shapeless crafting recipe that assigns a forging task to a {@link Forgeable} soul weapon.
 * <p>
 * Matches only when the grid contains exactly one Forgeable stack whose
 * {@link BaseSoulWeaponItem#CURRENT_FORGING_TASK} tag is present and equals
 * {@link BaseSoulWeaponItem#NO_FORGING_TASK}. The output is that same stack (all other NBT
 * preserved) with the tag set to the recipe's configured forging task id.
 */
public class SoulForgingRecipe extends ShapelessRecipe {
    private final ResourceLocation forgingTaskId;

    public SoulForgingRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                             ItemStack result, NonNullList<Ingredient> ingredients,
                             ResourceLocation forgingTaskId) {
        super(id, group, category, result, ingredients);
        this.forgingTaskId = forgingTaskId;
    }

    public ResourceLocation getForgingTaskId() {
        return this.forgingTaskId;
    }

    @Override
    public boolean matches(CraftingContainer pInv, Level pLevel) {
        if (!super.matches(pInv, pLevel)) return false;

        ItemStack forgeable = ItemStack.EMPTY;
        for (int i = 0; i < pInv.getContainerSize(); i++) {
            ItemStack stack = pInv.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof Forgeable)) continue;
            if (!forgeable.isEmpty()) return false; // exactly one Forgeable allowed
            forgeable = stack;
        }
        if (forgeable.isEmpty()) return false;

        // getTag (not getOrCreateTag): matches must not mutate stacks, and a missing tag must fail
        CompoundTag tag = forgeable.getTag();
        if (tag == null
                || !tag.contains(BaseSoulWeaponItem.CURRENT_FORGING_TASK, Tag.TAG_STRING)
                || !tag.getString(BaseSoulWeaponItem.CURRENT_FORGING_TASK).equals(BaseSoulWeaponItem.NO_FORGING_TASK)) {
            return false;
        }

        // If the task unlocks a skill the weapon already has, the recipe is unavailable.
        ForgingTask task = ForgingTasks.getTask(this.forgingTaskId);
        if (task != null && task.unlockedSkill != null
                && hasSkill(tag, task.unlockedSkill.soulSkillId.toString())) {
            return false;
        }

        return true;
    }

    private static boolean hasSkill(CompoundTag tag, String skillId) {
        ListTag listTag = tag.getList(BaseSoulWeaponItem.AVAILABLE_SKILLS, Tag.TAG_STRING);
        for (int i = 0; i < listTag.size(); i++) {
            if (listTag.getString(i).equals(skillId)) return true;
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof Forgeable) {
                ItemStack result = stack.copyWithCount(1);
                result.getOrCreateTag().putString(BaseSoulWeaponItem.CURRENT_FORGING_TASK, this.forgingTaskId.toString());
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        // The result depends on the input weapon's NBT, so recipe book / JEI previews would mislead
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.SOUL_FORGING_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<SoulForgingRecipe> {
        @Override
        public SoulForgingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String group = GsonHelper.getAsString(pJson, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(pJson, "category", null), CraftingBookCategory.MISC);

            NonNullList<Ingredient> ingredients = NonNullList.create();
            JsonArray array = GsonHelper.getAsJsonArray(pJson, "ingredients");
            for (int i = 0; i < array.size(); i++) {
                ingredients.add(Ingredient.fromJson(array.get(i), false));
            }
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for soul forging recipe");
            }
            if (ingredients.size() > 9) { // 3x3 grid; ShapedRecipe.MAX_WIDTH/MAX_HEIGHT are package-private
                throw new JsonParseException("Too many ingredients for soul forging recipe. The maximum is 9");
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));

            ResourceLocation taskId = new ResourceLocation(GsonHelper.getAsString(pJson, "forging_task"));
            if (ForgingTasks.getTask(taskId) == null) {
                throw new JsonSyntaxException("Unknown forging task: " + taskId);
            }

            return new SoulForgingRecipe(pRecipeId, group, category, result, ingredients, taskId);
        }

        @Override
        public SoulForgingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            String group = pBuffer.readUtf();
            CraftingBookCategory category = pBuffer.readEnum(CraftingBookCategory.class);
            int count = pBuffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
            for (int i = 0; i < count; i++) {
                ingredients.set(i, Ingredient.fromNetwork(pBuffer));
            }
            ItemStack result = pBuffer.readItem();
            ResourceLocation taskId = pBuffer.readResourceLocation();
            return new SoulForgingRecipe(pRecipeId, group, category, result, ingredients, taskId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SoulForgingRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.getGroup());
            pBuffer.writeEnum(pRecipe.category());
            pBuffer.writeVarInt(pRecipe.getIngredients().size());
            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }
            // ShapelessRecipe.getResultItem ignores the RegistryAccess argument
            pBuffer.writeItem(pRecipe.getResultItem(RegistryAccess.EMPTY));
            pBuffer.writeResourceLocation(pRecipe.forgingTaskId);
        }
    }
}
