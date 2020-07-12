package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathSwap extends JavaPlugin {
    protected DeathSwapGame game;



    @Override
    public void onEnable() {
        this.game = new DeathSwapGame(this, getServer());
        getServer().getPluginManager().registerEvents(new DeathListener(game), this);
        getLogger().info("Death swap game ready. '/deathswap players addAll' then '/deathswap start' to start, '/deathswap help' for more info.");
        this.getCommand("deathswap")
                .setExecutor(new DeathSwapCommand(game));

    }
    @Override
    public void onDisable() {
        getLogger().info("stopping death swap");
    }

}