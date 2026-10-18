package com.example.homearrow;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class HomeArrowClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("homearrow", "main"));

    private static boolean arrowVisible = false;

    @Override
    public void onInitializeClient() {
        HomeStore.load();

        KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.homearrow.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                arrowVisible = !arrowVisible;
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("homearrow", "arrow"),
                (graphics, tickCounter) -> renderArrow(graphics));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("sethome").executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.level == null) return 0;

                HomeStore.set(new HomeStore.Home(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        dimensionId(mc)));

                mc.player.displayClientMessage(
                        Component.literal("Home set here. Press N to toggle the arrow."), false);
                return 1;
            }));

            dispatcher.register(ClientCommands.literal("clearhome").executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                HomeStore.clear();
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.literal("Home cleared."), false);
                }
                return 1;
            }));
        });
    }

    private static void renderArrow(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        HomeStore.Home home = HomeStore.get();
        if (!arrowVisible || home == null || mc.player == null || mc.level == null || mc.options.hideGui) return;

        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 3;

        if (!home.dimension().equals(dimensionId(mc))) {
            graphics.drawCenteredString(mc.font,
                    Component.literal("Home is in another dimension"), centerX, centerY, 0xFFFFFFFF);
            return;
        }

        double dx = home.x() - mc.player.getX();
        double dz = home.z() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 3) {
            graphics.drawCenteredString(mc.font,
                    Component.literal("You are home"), centerX, centerY, 0xFF55FF55);
            return;
        }

        // Work out where home is relative to the direction the player is facing.
        double yaw = Math.toRadians(mc.player.getYRot());
        double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
        double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
        float angle = (float) Math.atan2(right, forward);

        drawArrow(graphics, centerX, centerY, angle);

        graphics.drawCenteredString(mc.font,
                Component.literal(Math.round(dist) + " blocks"), centerX, centerY + 18, 0xFFFFFFFF);
    }

    /** Draws an arrow pointing up, then rotates it by the given angle (radians, clockwise on screen). */
    private static void drawArrow(GuiGraphicsExtractor graphics, int x, int y, float angle) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.rotate(angle);

        int color = 0xFFFFD700; // gold
        graphics.fill(-2, -10, 2, 12, color); // shaft
        for (int i = 0; i < 12; i++) {        // arrowhead, widening toward the base
            int half = i + 1;
            graphics.fill(-half, -22 + i, half, -21 + i, color);
        }

        pose.popMatrix();
    }

    private static String dimensionId(Minecraft mc) {
        return mc.level.dimension().toString();
    }
}
