package net.betterverse.worldmanager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldManager extends JavaPlugin implements Listener {
    private final Map<String, WorldOptions> worlds = new HashMap<String, WorldOptions>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("setspawn")) {
                    if (player.hasPermission("worldmanager.admin.setspawn")) {
                        World world = player.getWorld();
                        Location spawnLoc = player.getLocation();
                        worlds.get(world.getName()).setSpawnLocation(spawnLoc);

                        player.sendMessage(ChatColor.GREEN + "You set the spawn of world " + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + " to your location.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission.");
                    }
                } else if (args[0].equalsIgnoreCase("world")) {
                    player.sendMessage(ChatColor.GREEN + "You are currently in the world " + ChatColor.YELLOW + player.getWorld().getName() + ChatColor.GREEN + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid arguments.");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                    World world = getServer().getWorld(args[1]);
                    if (world != null) {
                        if (player.hasPermission("worldmanager.teleport." + world.getName())) {
                            player.teleport(worlds.get(world.getName()).getSpawnLocation());
                            player.sendMessage(ChatColor.GREEN + "You have teleported to the spawn of the world " + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + ".");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have permission.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The world '" + args[1] + "' does not exist.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid arguments.");
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("config")) {
                    World world = getServer().getWorld(args[1]);
                    if (world != null) {
                        if (args[2].equalsIgnoreCase("gamemode") || args[2].equalsIgnoreCase("gm")) {
                            if (player.hasPermission("worldmanager.admin.gamemode")) {
                                try {
                                    GameMode gameMode = GameMode.getByValue(Integer.parseInt(args[3]));
                                    worlds.get(world.getName()).setGameMode(gameMode);
                                    player.sendMessage(ChatColor.GREEN + "You have changed the game mode of " + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + " to " + ChatColor.YELLOW
                                            + gameMode + ChatColor.GREEN + ".");
                                } catch (NumberFormatException ex) {
                                    player.sendMessage(ChatColor.RED + "Invalid game mode '" + args[3] + "'.");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Invalid arguments.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The world '" + args[1] + "' does not exist.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid arguments.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Invalid arguments.");
            }
        } else {
            sender.sendMessage("Only in-game players can use '/" + cmdLabel + "'.");
        }

        return true;
    }

    @Override
    public void onDisable() {
        log(toString() + " disabled.");
    }

    @Override
    public void onEnable() {
        // Load world settings
        loadWorlds();

        getServer().getPluginManager().registerEvents(this, this);

        log(toString() + " enabled.");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getDescription().getName() + " v" + getDescription().getVersion() + " [Written by: ");
        List<String> authors = getDescription().getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            builder.append(authors.get(i) + (i + 1 != authors.size() ? ", " : ""));
        }
        builder.append("]");

        return builder.toString();
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        checkGameMode(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkGameMode(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Allow players to spawn at their beds if possible
        if (!event.isBedSpawn()) {
            event.setRespawnLocation(worlds.get(event.getRespawnLocation().getWorld().getName()).getSpawnLocation());
        }
    }

    public void log(Level level, String message) {
        getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + message);
    }

    public void log(String message) {
        log(Level.INFO, message);
    }

    private void checkGameMode(Player player) {
        GameMode change = worlds.get(player.getWorld().getName()).getGameMode();
        if (player.getGameMode() != change && !player.hasPermission("worldmanager.admin.gamemode")) {
            player.setGameMode(change);
        }
    }

    private void loadWorlds() {
        for (File file : getDataFolder().listFiles()) {
            if (file.getName().endsWith(".yml")) {
                WorldOptions options = new WorldOptions(this, file);
                options.load();
                worlds.put(file.getName().replace(".yml", ""), options);
            }
        }
    }
}
