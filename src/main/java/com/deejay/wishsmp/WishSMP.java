package com.deejay.wishsmp;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class WishSMP implements ModInitializer {

    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        System.out.println("[WishSMP] Server Mod Loaded Successfully!");
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
