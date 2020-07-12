package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DeathSwapGame {
    ArrayList<Player> players;
    // if array is even: first two are mutual targets, next two are also targets, etc.
    // if odd size: the last three target in a circle, rest is same as normal
    String[] targets;
    String[] oldTargets;

    // one per player
    ArrayList<Team> scoreboardTeams;

    Objective objective;

    int swapTime; // in seconds
    boolean active;
    int taskid;

    Server server;
    Plugin plugin;

    int time;

    public String[] getTargets() {
        return this.targets;
    }

    public DeathSwapGame(Plugin plugin, Server server) {
        this.players = new ArrayList<>();
        this.swapTime = 60 * 5;
        this.active = false;
        this.server = server;
        this.plugin = plugin;

    }

    private void sendErr(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.RED + msg);
    }

    public void handleDeath(String name) {
        for (int i = 0; i < oldTargets.length; i ++) {
            if (oldTargets[i] == name) {
                String targeter;
                if (oldTargets.length % 2 == 1 && i == oldTargets.length - 3) {
                    targeter = oldTargets[oldTargets.length - 1];
                } else if (oldTargets.length % 2 == 1 && i == oldTargets.length - 2) {
                    targeter = oldTargets[oldTargets.length - 3];
                } else if (oldTargets.length % 2 == 1 && i == oldTargets.length - 1) {
                    targeter = oldTargets[oldTargets.length - 2];
                } else if (i % 2 == 0) {
                    //even
                    targeter = oldTargets[i + 1];
                } else {
                    targeter = oldTargets[i - 1];
                }
                Score targetScore = objective.getScore(name);
                targetScore.setScore(targetScore.getScore() - 1);
                Score targeterScore = objective.getScore(targeter);
                targeterScore.setScore(targeterScore.getScore() + 1);
                Bukkit.broadcastMessage(name + " died and lost a point to " + targeter);

                break;
            }
        }

    }

    public boolean onCommand(CommandSender sender, String[] args) {
        String action = args[0];
        switch (action) {
            case "help":
                sender.sendMessage("List of avaliable deathswap commands:");
                sender.sendMessage(ChatColor.RED +  "/deathswap help" + ChatColor.GRAY + ": show this help menu");
                sender.sendMessage(ChatColor.RED +  "/deathswap start" + ChatColor.GRAY + ": start death swap");
                sender.sendMessage(ChatColor.RED +  "/deathswap stop" + ChatColor.GRAY + ": stop death swap");
                sender.sendMessage(ChatColor.RED +  "/deathswap players add <playerName>" + ChatColor.GRAY + ": add a player");
                sender.sendMessage(ChatColor.RED +  "/deathswap players addAll" + ChatColor.GRAY + ": add all players that are online, and remove all offline players");
                sender.sendMessage(ChatColor.RED +  "/deathswap players list" + ChatColor.GRAY + ": list all players");
                return true;
            case "timer":
                if (args[1] == "set") {
                    objective.getScore("timer").setScore(Integer.parseInt(args[2]));
                }
                return true;
            case "start":
                this.active = true;
                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                Scoreboard board = scoreboardManager.getNewScoreboard();
                scoreboardTeams = new ArrayList<Team>();

//

                objective = board.registerNewObjective("points", "dummy", "Points");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);


                for (int i = 0; i < players.size(); i ++) {
                    Player p = players.get(i);
                    Team t = board.registerNewTeam(p.getDisplayName());
                    scoreboardTeams.add(t);

                    Score score = objective.getScore(p.getDisplayName());
                    score.setScore(0);
                }
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.setScoreboard(board);
                }

                Team timerTeam = board.registerNewTeam("timer");

                BukkitScheduler scheduler = server.getScheduler();
                time = swapTime;
                objective.getScore("timer").setScore(time);
                targets = assignTargets(players);
                taskid = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Score s = objective.getScore("timer");
                        time --;
                        if (time < 0) {
                            time = swapTime;
                            for (int i = 0; i < targets.length; i ++) {
                                oldTargets[i] = targets[i];
                            }
                            s.setScore(time);
                            for (int i = 0; i < targets.length; i +=  2) {
                                if (targets.length %  2 == 1 && i == targets.length - 3) {
                                    Player A = Bukkit.getPlayer(targets[targets.length - 3]);
                                    Player B = Bukkit.getPlayer(targets[targets.length - 2]);
                                    Player C = Bukkit.getPlayer(targets[targets.length - 1]);
                                    Location LA = A.getLocation();
                                    Location LB = B.getLocation();
                                    Location LC = C.getLocation();

                                    B.teleport(LA);
                                    C.teleport(LB);
                                    A.teleport(LC);
                                    break;
                                }
                                Player A = Bukkit.getPlayer(targets[i]);
                                Player B = Bukkit.getPlayer(targets[i + 1]);
                                Location LA = A.getLocation();
                                Location LB = B.getLocation();
                                A.teleport(LB);
                                B.teleport(LA);
                            }
                            targets = assignTargets(players);

                        }


                    }
                }, 0L, 20L);


                return true;
            case "stop":
                this.active = false;
                Bukkit.getScheduler().cancelTask(taskid);
                sender.sendMessage("Stopped Plugin");
                return true;
            case "players":
                String subAction = args[1];
                switch (subAction) {
                    case "add":
                        if (args.length > 2) {
                            if (Bukkit.getPlayer(args[2]) == null) {
                                sendErr(sender, "That player could not be found (are they online?)");
                                return false;
                            }
                            this.players.add(Bukkit.getPlayer(args[2]));
                            sender.sendMessage("Added " + args[2] + " to the game");
                            return true;
                        } else {
                            if (sender instanceof Player)  {
                                this.players.add((Player) sender);
                                sender.sendMessage("Added " + ((Player) sender).getDisplayName() + " to the game.");
                                return true;
                            } else {
                                sendErr(sender, "The command " +  ChatColor.RED + "/deathswap add" + ChatColor.GRAY +  " with no additional arguments can " +
                                        "only be called as a player, because it adds the current player.");
                                return false;
                            }
                        }
                    case "addAll":
                        this.players = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
                        sender.sendMessage("Added all online players to the game.");
                        return true;
                    case "list":
                        StringBuilder names = new StringBuilder("The current game has " + players.size() + " player" + (players.size() == 1 ? "" : "s") + ": ");
                        for (Player p : players) {
                            names.append(p.getDisplayName()).append(", ");
                        }
                        // remove last ", "
                        names.delete(names.length() - 2, names.length() - 1);
                        sender.sendMessage(names.toString());
                        return true;
                }

        }
        sendErr(sender, ChatColor.GRAY + "Deathswap command not found! " +  ChatColor.RED + "/deathswap help" + ChatColor.GRAY +  " for help.");
        return false;
    }


    public String[] assignTargets(ArrayList<Player> players) {
        String[] targets = new String[players.size()];
        for (int i = 0; i < targets.length; i ++) {
            targets[i] = players.get(i).getDisplayName();
        }
        Random rnd = ThreadLocalRandom.current();
        for (int i = targets.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = targets[index];
            targets[index] = targets[i];
            targets[i] = a;
        }
        //  announce new targets; int  division =  truncate
        Bukkit.broadcastMessage("New Targets:");
        for (int i = 0; i < targets.length; i +=  2) {
            if (targets.length %  2 == 1 && i == targets.length - 3) {
                Bukkit.broadcastMessage(targets[targets.length - 3] + ": " + targets[targets.length - 2]);
                Bukkit.broadcastMessage(targets[targets.length - 2] + ": " + targets[targets.length - 1]);
                Bukkit.broadcastMessage(targets[targets.length - 1] + ": " + targets[targets.length - 3]);
                break;
            }
            Bukkit.broadcastMessage(targets[i] + ": " + targets[i + 1]);
            Bukkit.broadcastMessage(targets[i + 1] + ": " + targets[i]);

        }


        return targets;
    }



}
