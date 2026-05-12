package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record SpectatorReplayDetailSyncS2CPacket(long requestId,
                                                 UUID targetUuid,
                                                 List<Text> replayLines) implements CustomPayload {
    public static final Id<SpectatorReplayDetailSyncS2CPacket> ID =
            new Id<>(Identifier.of(Noellesroles.MOD_ID, "spectator_replay_detail_sync"));

    public static final PacketCodec<RegistryByteBuf, SpectatorReplayDetailSyncS2CPacket> CODEC =
            PacketCodec.of(SpectatorReplayDetailSyncS2CPacket::write, SpectatorReplayDetailSyncS2CPacket::read);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarLong(requestId);
        buf.writeUuid(targetUuid);
        buf.writeVarInt(replayLines.size());
        for (Text line : replayLines) {
            TextCodecs.PACKET_CODEC.encode(buf, line);
        }
    }

    private static SpectatorReplayDetailSyncS2CPacket read(PacketByteBuf buf) {
        long requestId = buf.readVarLong();
        UUID targetUuid = buf.readUuid();
        int lineCount = buf.readVarInt();
        List<Text> replayLines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            replayLines.add(TextCodecs.PACKET_CODEC.decode(buf));
        }
        return new SpectatorReplayDetailSyncS2CPacket(requestId, targetUuid, replayLines);
    }
}

