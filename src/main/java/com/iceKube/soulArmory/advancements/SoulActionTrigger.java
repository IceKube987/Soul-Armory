package com.iceKube.soulArmory.advancements;

import com.google.gson.JsonObject;
import com.iceKube.soulArmory.SoulArmoryMod;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

/**
 * A single criterion trigger shared by every Soul Armory advancement, keyed on a {@link SoulAction}.
 */
public class SoulActionTrigger extends SimpleCriterionTrigger<SoulActionTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(SoulArmoryMod.MODID, "soul_action");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    // The two-arg createInstance is final in SimpleCriterionTrigger; this is the one to implement.
    // The predicate is never null, EntityPredicate.fromJson falls back to ContextAwarePredicate.ANY.
    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new Instance(player, SoulAction.byName(GsonHelper.getAsString(json, "action")));
    }

    public void trigger(ServerPlayer player, SoulAction action) {
        this.trigger(player, instance -> instance.action == action);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {

        final SoulAction action;

        public Instance(ContextAwarePredicate player, SoulAction action) {
            super(ID, player);
            this.action = action;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("action", action.getSerializedName());
            return json;
        }
    }
}
