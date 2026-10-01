package com.example.autoaim;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entrypoint: config, keybind, and the single tick hook. */
public final class AutoAimClient implements ClientModInitializer {
    public static final String MOD_ID = "autoaim";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        AutoAimConfig.load();

        // Appears under Options > Controls > Key Binds in the "Auto Aim" category. Rebindable to any key.
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
        ));

        // START of tick: rotation is set before the client sends its movement/rotation packet this tick.
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                AutoAimManager.toggle(client);
            }
            AutoAimManager.tick(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AutoAimManager.reset());
    }
}
