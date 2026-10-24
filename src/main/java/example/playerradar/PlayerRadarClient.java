package example.playerradar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PlayerRadarClient implements ClientModInitializer {
    private static final long TEST_DURATION_MS = 30_000L;
    private static final int MAX_PACKETS_PER_SECOND = 1000;
    private static final long MIN_INTERVAL_MS = 200L;

    private static KeyBinding openKey, toggleKey;
    private static boolean running = false;
    private static long lastSendMs = 0, secondStart = 0, testStartedMs = 0;
    private static int sequence = 0, sentThisSecond = 0;

    private static void setRunning(boolean value) {
        running = value;
        if (value) {
            long now = System.currentTimeMillis();
            testStartedMs = now;
            secondStart = now;
            lastSendMs = 0;
            sentThisSecond = 0;
        }
    }

    private static void notify(MinecraftClient client, String msg) {
        if (client.player != null) client.player.sendMessage(Text.literal(msg), true);
    }

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.player_radar.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_8, "category.player_radar"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.player_radar.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.player_radar"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new RadarScreen());
                else if (client.currentScreen instanceof RadarScreen) client.setScreen(null);
            }
            while (toggleKey.wasPressed()) {
                setRunning(!running);
                notify(client, "Packet test: " + (running ? "ON for 30s (max 5/s)" : "OFF"));
            }

            if (!running) return;
            if (client.player == null || client.getNetworkHandler() == null) { running = false; return; }
            if (client.getServer() == null) {
                running = false;
                notify(client, "Test stopped: start a local singleplayer world first.");
                return;
            }
            if (!ClientPlayNetworking.canSend(TestPayload.ID)) {
                running = false;
                notify(client, "Test stopped: server does not accept the payload.");
                return;
            }
            long now = System.currentTimeMillis();
            if (now - testStartedMs >= TEST_DURATION_MS) {
                running = false;
                notify(client, "Packet test finished (30s).");
                return;
            }
            if (now - secondStart >= 1000) { secondStart = now; sentThisSecond = 0; }
            if (sentThisSecond >= MAX_PACKETS_PER_SECOND || now - lastSendMs < MIN_INTERVAL_MS) return;
            ClientPlayNetworking.send(new TestPayload(++sequence));
            sentThisSecond++;
            lastSendMs = now;
        });
    }

    private static String buttonLabel() {
        return running ? "Stop test" : "Start 30s test (max 5/s)";
    }

    private static class RadarScreen extends Screen {
        protected RadarScreen() { super(Text.literal("Player Radar - Packet Test")); }

        @Override
        protected void init() {
            addDrawableChild(ButtonWidget.builder(Text.literal(buttonLabel()), b -> {
                setRunning(!running);
                b.setMessage(Text.literal(buttonLabel()));
            }).dimensions(width / 2 - 100, height / 2 - 10, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                    .dimensions(width / 2 - 100, height / 2 + 20, 200, 20).build());
        }

        @Override
        public void render(DrawContext c, int mx, int my, float delta) {
            // super.render() already draws the background in 1.21.x; drawing it twice crashes (blur once per frame).
            super.render(c, mx, my, delta);
            c.drawCenteredTextWithShadow(textRenderer, "30-second custom-payload test (max 5 packets/sec)", width / 2, height / 2 - 40, 0xFFFFFF);
            c.drawCenteredTextWithShadow(textRenderer, "Local singleplayer only; install this mod on the server too.", width / 2, height / 2 + 60, 0xAAAAAA);
        }

        @Override
        public void close() { if (client != null) client.setScreen(null); }
    }
}
