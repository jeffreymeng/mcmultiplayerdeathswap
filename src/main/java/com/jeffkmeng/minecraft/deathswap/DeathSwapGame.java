package com.jeffkmeng.minecraft.deathswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
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
    boolean[] gotPointThisRound; // parallel to oldTargets. Ensures that dying twice in the same round doesn't grant two points.

    // one per player
    ArrayList<Team> scoreboardTeams;

    Objective objective;

    int swapTime; // in seconds
    boolean active;
    int taskid;

    Server server;
    Plugin plugin;

    int time;



    public DeathSwapGame(Plugin plugin, Server server) {
        this.players = new ArrayList<>();
        this.swapTime = 60 * 5;
        this.time = this.swapTime;
        this.active = false;
        this.server = server;
        this.plugin = plugin;
        this.taskid = 0;

    }

    private void sendErr(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.RED + msg);
    }

    public void handleDeath(PlayerDeathEvent event) {
        String name = event.getEntity().getName();
        for (int i = 0; i < oldTargets.length; i ++) {
            if (oldTargets[i].equals(name)) {
                if (gotPointThisRound[i]) {
                    return;
                }
                gotPointThisRound[i] = true;
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
                event.setDeathMessage(event.getDeathMessage() + " and lost a point to " + targeter);
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                return;
            }
        }

    }

    public boolean onCommand(CommandSender sender, String[] args) {
        String action = args[0].toLowerCase();
        switch (action) {
            case "help":
                sender.sendMessage("List of avaliable deathswap commands:");
                sender.sendMessage(ChatColor.RED +  "/deathswap help" + ChatColor.GRAY + ": show this help menu");
                sender.sendMessage(ChatColor.RED +  "/deathswap timer set [seconds]" + ChatColor.GRAY + ": set the number of seconds per round (default 300)");
                sender.sendMessage(ChatColor.RED +  "/deathswap swap" + ChatColor.GRAY + ": swap instantly (within 1 second) if the game has been started");
                sender.sendMessage(ChatColor.RED +  "/deathswap start" + ChatColor.GRAY + ": start death swap");
                sender.sendMessage(ChatColor.RED +  "/deathswap stop" + ChatColor.GRAY + ": stop death swap");
                sender.sendMessage(ChatColor.RED +  "/deathswap players addAll" + ChatColor.GRAY + ": add all online players to the death swap game");
                sender.sendMessage(ChatColor.RED +  "/deathswap players help" + ChatColor.GRAY + ": display help menu for editing the players list.");
                return true;
            case "timer":
                if (args[1].equals("set")) {
                    sender.sendMessage("Set round length to " + args[2] + " seconds.");
                    this.swapTime = Integer.parseInt(args[2]);
                    if (this.time > this.swapTime) {
                        this.time = this.swapTime;
                    }
                    return true;
                }
            case "swap":

                this.time = 11;
                objective.getScore("timer").setScore(11);
                return true;
            case "start":
                if (players.size() < 2) {
                    sendErr(sender, "Must have at least two players to start.");
                    return true;
                }
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
                if (taskid != 0) {
                    Bukkit.getScheduler().cancelTask(taskid);
                }
                taskid = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        Score s = objective.getScore("timer");
                        time --;
                        s.setScore(time);
                        if (time % 60 == 0) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "Swapping in " + (time/60) + " minute" + (time == 60 ? "" : "s") + "...");
                        }
                        if (time == 10) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "Swapping in 10 seconds...");
                        }
                        if (time > 0 && time < 4) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "Swapping in " + time + " seconds...");
                        }
                        if (time <= 0) {
                            time = swapTime;
                            oldTargets = new String[targets.length];
                            for (int i = 0; i < targets.length; i ++) {
                                oldTargets[i] = targets[i];
                                gotPointThisRound[i] = false;
                            }

                            for (int i = 0; i < targets.length; i +=  2) {
                                // this could definitely be more efficient lol

                                // if there are an odd number of players, and we are on the third to last player (whom will have an odd index)
                                // then do a three way pairing.
                                if (targets.length %  2 == 1 && i == targets.length - 3) {
                                    Player A = Bukkit.getPlayer(targets[targets.length - 3]);
                                    Player B = Bukkit.getPlayer(targets[targets.length - 2]);
                                    Player C = Bukkit.getPlayer(targets[targets.length - 1]);
                                    if (A == null || !A.isOnline()) {
                                        if (B != null)
                                            B.sendMessage(ChatColor.GRAY + "You will not be teleported because your opponent for this round could not be found online. Ask an operator to do " + ChatColor.RED + "/deathswap remove " + targets[targets.length - 2] + ChatColor.GRAY + " to remove them from this game and prevent this from happening again.");
                                    }
                                    assert A != null;
                                    Location LA = A.getLocation();
                                    if (B == null || !B.isOnline()) {
                                        if (C != null)
                                            C.sendMessage(ChatColor.GRAY + "You will not be teleported because your opponent for this round could not be found online. Ask an operator to do " + ChatColor.RED + "/deathswap remove " + targets[targets.length - 1] + ChatColor.GRAY + " to remove them from this game and prevent this from happening again.");
                                    }
                                    assert B != null;
                                    Location LB = B.getLocation();
                                    if (C == null || !C.isOnline()) {
                                        if (A != null)
                                            A.sendMessage(ChatColor.GRAY + "You will not be teleported because your opponent for this round could not be found online. Ask an operator to do " + ChatColor.RED + "/deathswap remove " + targets[targets.length - 3] + ChatColor.GRAY + " to remove them from this game and prevent this from happening again.");
                                    }
                                    assert C != null;
                                    Location LC = C.getLocation();

                                    B.teleport(LA);
                                    C.teleport(LB);
                                    A.teleport(LC);
                                    break;
                                }
                                Player A = Bukkit.getPlayer(targets[i]);
                                Player B = Bukkit.getPlayer(targets[i + 1]);
                                if (A == null || !A.isOnline()) {
                                    if (B != null)
                                        B.sendMessage(ChatColor.GRAY + "You will not be teleported because your opponent for this round could not be found online. Ask an operator to do " + ChatColor.RED + "/deathswap remove " + targets[i] + ChatColor.GRAY + " to remove them from this game and prevent this from happening again.");
                                }
                                if (B == null || !B.isOnline()) {
                                    if (A != null)
                                        A.sendMessage(ChatColor.GRAY + "You will not be teleported because your opponent for this round could not be found online. Ask an operator to do " + ChatColor.RED + "/deathswap remove " + targets[i + 1] + ChatColor.GRAY + " to remove them from this game and prevent this from happening again.");

                                }
                                assert A != null;
                                assert B != null;
                                Location LA = A.getLocation();
                                Location LB = B.getLocation();
                                A.teleport(LB);
//                                if(!LB.getChunk().isLoaded()) {
//                                    Bukkit.broadcastMessage("[DEBUG]: reloading unloaded chunks");
//                                    LB.getChunk().load(true);
//                                }
//                                if(!LB.getChunk().isLoaded()) {
//                                    Bukkit.broadcastMessage("[DEBUG]: Chunk still not loaded!");
//                                }

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
                sender.sendMessage("deathswap has been stopped");
                return true;
            case "players":
                String subAction = args[1].toLowerCase();
                switch (subAction) {
                    case "help":
                        sender.sendMessage("Avaliable player commands (note: edits to the player list will not take effect until the next time /deathswap start is called");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players help" + ChatColor.GRAY + ": display this help menu");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players add <playerName>" + ChatColor.GRAY + ": add a player");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players addAll" + ChatColor.GRAY + ": add all players that are online, and remove all offline players");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players remove <playerName>" + ChatColor.GRAY + ": remove a player");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players removeAll" + ChatColor.GRAY + ": remove all players from the list");
                        sender.sendMessage(ChatColor.RED +  "/deathswap players list" + ChatColor.GRAY + ": list all players");
                    case "add":
                        if (args.length > 2) {
                            if (Bukkit.getPlayer(args[2]) == null) {
                                if (args[2].equalsIgnoreCase("all")) {
                                    sendErr(sender, "No player with username 'all' could be found online. Did you mean to add all online players with "
                                            + ChatColor.GRAY + "/deathswap players addAll" + ChatColor.RED + " (no space)?");
                                    return true;
                                }
                                sendErr(sender, "That player could not be found (are they online?)");
                                return true;
                            }
                            this.players.add(Bukkit.getPlayer(args[2]));
                            sender.sendMessage("Added " + args[2] + " to the game");
                            return true;
                        } else {

                                sendErr(sender, "No player to add was specified. Use /deathswap players addAll to add all players");
                                return true;

                        }
                    case "addall":
                        this.players = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
                        sender.sendMessage("Added all online players to the game.");
                        return true;
                    case "remove":
                        int oldLength = this.players.size();
                        this.players.removeIf(p -> {
                            return p.getDisplayName().equalsIgnoreCase(args[2]);
                        });
                        if (oldLength != this.players.size()) {
                            sender.sendMessage("Removed " + args[2] + " from the players list.");
                            return true;
                        } else {
                            if (args[2].equalsIgnoreCase("all")) {
                                sendErr(sender, "No player with username 'all' could be found on  the list. Did you mean to remove all the players with "
                                        + ChatColor.GRAY + "/deathswap players removeAll" + ChatColor.RED + " (no space)?");
                                return true;
                            }
                            sendErr(sender, "Unable to remove " + args[2] + " from the players list (are you sure they are on the list? use /deathswap players list to check");
                            return true;
                        }
                    case "removeall":
                        this.players = new ArrayList<Player>();
                        sender.sendMessage("Removed all players from the players list.");
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
