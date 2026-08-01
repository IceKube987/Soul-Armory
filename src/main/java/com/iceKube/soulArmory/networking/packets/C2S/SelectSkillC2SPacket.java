package com.iceKube.soulArmory.networking.packets.C2S;

import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import com.iceKube.soulArmory.networking.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent when the player commits a pick in the skill radial menu, carrying the index of the chosen
 * skill within the held item's available-skill list.
 * <p>
 * Unlike {@link SwitchSkillC2SPacket}, which just asks for the next skill, this one names a target.
 * The index is client-supplied and therefore untrusted — every {@code setCurrentSkill}
 * implementation re-reads the skill list server-side and rejects an index that doesn't fit.
 */
public class SelectSkillC2SPacket extends BasePacket {
    private final int index;

    public SelectSkillC2SPacket(int index) {
        this.index = index;
    }

    public SelectSkillC2SPacket(FriendlyByteBuf buf) {
        super(buf);
        this.index = buf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();

            // Any soul skill item is fair game — the menu is opened off the interface, not off a
            // particular weapon, so the handler is too.
            if (stack.getItem() instanceof UseSoulSkillSystem skillItem) {
                skillItem.setCurrentSkill(stack, index, player);
            }
        });

        context.setPacketHandled(true);
        return true;
    }
}
