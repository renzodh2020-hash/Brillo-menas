package com.paolo.autosell;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PaoloAutoSellClient implements ClientModInitializer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Random random = new Random();

    private static KeyBinding toggleKey;
    private static KeyBinding resetKey;

    private static boolean enabled = false;
    private static boolean restMode = false;

    private static int sellDelayTicks = 0;
    private static int phaseTicks = 15 * 60 * 20;
    private static int phaseIndex = 0;

    private static int sellCount = 0;
    private static boolean homeScheduled = false;
    private static int homeDelayTicks = 0;

    private static final int[] ACTIVE_PHASES_MINUTES = {15, 10, 13};

    private static final List<Integer> integerPool = new ArrayList<>();
    private static final List<Integer> fractionPool = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.paolo_autosell.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.paolo_autosell"
        ));

        resetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.paolo_autosell.reset",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.paolo_autosell"
        ));

        refillIntegerPool();
        refillFractionPool();
        scheduleNextSell();

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                sendMessage(enabled ? "AutoSell activado." : "AutoSell pausado.");
            }

            while (resetKey.wasPressed()) {
                resetLoop();
                sendMessage("AutoSell reiniciado.");
            }

            tickLoop();
        });
    }

    private static void tickLoop() {
        if (!enabled) return;
        if (client.player == null || client.world == null) return;

        if (!client.isIntegratedServerRunning()) {
            enabled = false;
            sendMessage("AutoSell desactivado: solo funciona en mundo individual.");
            return;
        }

        if (restMode) {
            phaseTicks--;

            if (phaseTicks <= 0) {
                restMode = false;
                phaseTicks = ACTIVE_PHASES_MINUTES[phaseIndex] * 60 * 20;
                scheduleNextSell();
                sendMessage("Pausa terminada. AutoSell continúa.");
            }

            return;
        }

        phaseTicks--;

        if (phaseTicks <= 0) {
            startRandomRest();
            return;
        }

        if (client.player.getOffHandStack().isEmpty()) {
            return;
        }

        if (homeScheduled) {
            homeDelayTicks--;

            if (homeDelayTicks <= 0) {
                runCommand("home up");
                homeScheduled = false;
            }
        }

        sellDelayTicks--;

        if (sellDelayTicks <= 0) {
            runCommand("sellall");
            sellCount++;

            if (sellCount >= 5) {
                sellCount = 0;
                scheduleHomeUp();
            }

            scheduleNextSell();
        }
    }

    private static void runCommand(String command) {
        if (client.player != null && client.player.networkHandler != null) {
            client.player.networkHandler.sendChatCommand(command);
        }
    }

    private static void scheduleNextSell() {
        int integerPart = getNextIntegerPart();
        int fractionPart = getNextFractionPart();

        int milliseconds = integerPart * 1000 + fractionPart;
        sellDelayTicks = Math.max(1, milliseconds / 50);
    }

    private static void scheduleHomeUp() {
        int milliseconds = 1000 + random.nextInt(2001);
        homeDelayTicks = Math.max(1, milliseconds / 50);
        homeScheduled = true;
    }

    private static void startRandomRest() {
        restMode = true;

        int restMilliseconds = 168000 + random.nextInt(24001);
        phaseTicks = Math.max(1, restMilliseconds / 50);

        phaseIndex++;
        if (phaseIndex >= ACTIVE_PHASES_MINUTES.length) {
            phaseIndex = 0;
        }

        sendMessage("AutoSell en pausa automática.");
    }

    private static int getNextIntegerPart() {
        if (integerPool.isEmpty()) {
            refillIntegerPool();
        }

        return integerPool.remove(0);
    }

    private static int getNextFractionPart() {
        if (fractionPool.isEmpty()) {
            refillFractionPool();
        }

        return fractionPool.remove(0);
    }

    private static void refillIntegerPool() {
        integerPool.clear();
        integerPool.add(3);
        integerPool.add(4);
        integerPool.add(5);
        integerPool.add(6);
        Collections.shuffle(integerPool);
    }

    private static void refillFractionPool() {
        fractionPool.clear();

        for (int i = 0; i <= 999; i++) {
            fractionPool.add(i);
        }

        Collections.shuffle(fractionPool);
    }

    private static void resetLoop() {
        enabled = false;
        restMode = false;

        sellDelayTicks = 0;
        phaseIndex = 0;
        phaseTicks = 15 * 60 * 20;

        sellCount = 0;
        homeScheduled = false;
        homeDelayTicks = 0;

        refillIntegerPool();
        refillFractionPool();
        scheduleNextSell();
    }

    private static void sendMessage(String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[Paolo AutoSell] " + message), false);
        }
    }
}
