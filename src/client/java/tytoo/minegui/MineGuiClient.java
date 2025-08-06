package tytoo.minegui;

import net.fabricmc.api.ClientModInitializer;

public class MineGuiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("MineGui Client Initialized");
    }
}