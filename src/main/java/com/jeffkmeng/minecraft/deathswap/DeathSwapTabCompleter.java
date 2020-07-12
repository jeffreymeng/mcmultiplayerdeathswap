package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class DeathSwapTabCompleter implements TabCompleter {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> cmds = new ArrayList<String>();

        switch (args.length) {
            case 1:
                cmds.add("help");
                cmds.add("start");
                cmds.add("players");
                cmds.add("stop");
                cmds.add("timer");
                return StringUtil.copyPartialMatches(args[0], cmds, new ArrayList<String>());

            case 2:
                switch (args[0].toLowerCase()) {
                    case "timer":
                        cmds.add("set");
                        return StringUtil.copyPartialMatches(args[1], cmds, new ArrayList<String>());
                    case "players":
                        cmds.add("help");
                        cmds.add("add");
                        cmds.add("addAll");
                        cmds.add("remove");
                        cmds.add("removeAll");
                        cmds.add("list");
                        return StringUtil.copyPartialMatches(args[1], cmds, new ArrayList<String>());
                }
            case 3:
                if (args[0].toLowerCase() == "players") {
                    switch(args[1].toLowerCase()) {
                        case "add":
                        case "remove":
                            for (Player p : getServer().getOnlinePlayers()) cmds.add(p.getName());
                            return StringUtil.copyPartialMatches(args[1], cmds, new ArrayList<String>());
                    }
                }
        }
        return null;
    }
}
