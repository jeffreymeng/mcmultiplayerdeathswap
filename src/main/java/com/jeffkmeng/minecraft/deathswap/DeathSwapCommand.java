package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.command.*;

public class DeathSwapCommand implements CommandExecutor {
    protected DeathSwapGame game;
    public DeathSwapCommand(DeathSwapGame game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
       return game.onCommand(sender, args);
    }
}
