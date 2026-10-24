package example.playerradar;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
public record TestPayload(int sequence) implements CustomPayload {
 public static final Id<TestPayload> ID = new Id<>(Identifier.of("player_radar","test_ping"));
 public static final PacketCodec<RegistryByteBuf,TestPayload> CODEC =
  PacketCodec.tuple(PacketCodecs.VAR_INT,TestPayload::sequence,TestPayload::new);
 @Override public Id<? extends CustomPayload> getId(){ return ID; }
}
