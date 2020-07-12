package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathListener implements Listener {
    DeathSwapGame game;
    public DeathListener(DeathSwapGame game) {
        this.game = game;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        game.handleDeath(event);
    }
}