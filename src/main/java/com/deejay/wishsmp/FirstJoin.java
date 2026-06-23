package com.deejay.wishsmp;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Random;
import java.util.UUID;

public class FirstJoin {

    private static final Random RANDOM = new Random();

    public static void init() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();

            server.execute(() -> handleJoin(player, uuid));
        });
    }

    private static void handleJoin(ServerPlayerEntity player, UUID uuid) {

        // If already has class → just load it
        if (PlayerClassManager.hasClass(uuid)) {
            ClassType type = PlayerClassManager.getClass(uuid);
            sendFinalTitle(player, type);
            return;
        }

        // Blindness effect
        player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false)
        );

        ClassType[] cycle = {
                ClassType.STRENGTH,
                ClassType.SPEED,
                ClassType.HASTE,
                ClassType.HEALTH
        };

        // 1 second intervals (20 ticks)
        for (int i = 0; i < cycle.length; i++) {

            int delay = i * 20;
            ClassType type = cycle[i];

            schedule(() -> {
                sendTitle(player, type, getColor(type));
                playClick(player);
            }, delay);
        }

        // Final reveal after cycle (4 seconds total)
        schedule(() -> {

            ClassType chosen = cycle[RANDOM.nextInt(cycle.length)];

            PlayerClassManager.setClass(uuid, chosen);

            sendFinalTitle(player, chosen);
            playFinalSound(player);

            player.removeStatusEffect(StatusEffects.BLINDNESS);

        }, 80);

    }

    // ----------------------------
    // TITLE SYSTEM (CENTER SCREEN)
    // ----------------------------

    private static void sendTitle(ServerPlayerEntity player, ClassType type, int color) {

        Text title = Text.literal(type.getDisplayName())
                .formatted(Formatting.BOLD)
                .styled(style -> style.withColor(color));

        player.networkHandler.sendPacket(
                new TitleS2CPacket(title)
        );

        player.networkHandler.sendPacket(
                new SubtitleS2CPacket(Text.literal(""))
        );
    }

    private static void sendFinalTitle(ServerPlayerEntity player, ClassType type) {

        Text title = Text.literal(type.getDisplayName().toUpperCase())
                .formatted(Formatting.BOLD)
                .styled(style -> style.withColor(getColor(type)));

        player.networkHandler.sendPacket(
                new TitleS2CPacket(title)
        );

        player.networkHandler.sendPacket(
                new SubtitleS2CPacket(Text.literal("Your class has been chosen"))
        );
    }

    // ----------------------------
    // SOUND
    // ----------------------------

    private static void playClick(ServerPlayerEntity player) {
        player.playSound(
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundCategory.PLAYERS,
                1.0f,
                1.2f
        );
    }

    private static void playFinalSound(ServerPlayerEntity player) {
        player.playSound(
                SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }

    // ----------------------------
    // COLORS
    // ----------------------------

    private static int getColor(ClassType type) {
        return switch (type) {
            case STRENGTH -> 0x8B0000;
            case SPEED -> 0x1E90FF;
            case HASTE -> 0xFFA500;
            case HEALTH -> 0x00AA00;
        };
    }

    // ----------------------------
    // SIMPLE SCHEDULER (TEMP)
    // ----------------------------

    private static void schedule(Runnable task, int delayTicks) {
        new Thread(() -> {
            try {
                Thread.sleep(delayTicks * 50L);
                task.run();
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
