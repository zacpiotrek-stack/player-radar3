package example.playerradar;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PlayerRadar implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(TestPayload.ID, TestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TestPayload.ID, TestPayload.CODEC);
        // Server-side receiver so the payload is consumed instead of logged as unhandled.
        ServerPlayNetworking.registerGlobalReceiver(TestPayload.ID, (payload, context) -> { });
    }
}
