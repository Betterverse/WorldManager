package net.betterverse.worldmanager;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
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
                        getWorld(world).setSpawnLocation(spawnLoc);

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
                                    getWorld(world).setGameMode(gameMode);
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
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!getWorld(event.getLocation().getWorld()).canCreatureSpawn(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            // Player is attacking another player, check for world's pvp setting
            Player damager = (Player) event.getDamager();
            if (!getWorld(damager.getWorld()).isPvPAllowed()) {
                damager.sendMessage(ChatColor.RED + "PvP is not allowed in this world.");
                ((Player) event.getEntity()).sendMessage(ChatColor.RED + "PvP is not allowed in this world.");
                event.setCancelled(true);
            }
        }
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
            event.setRespawnLocation(getWorld(event.getRespawnLocation().getWorld()).getSpawnLocation());
        }
    }

    @EventHandler
    public void onRedstoneChange(BlockRedstoneEvent event) {
        if (!getWorld(event.getBlock().getWorld()).isRedstoneAllowed()) {
            event.setNewCurrent(0);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        // Prevent the weather from changing if the world's weather option is not "DEFAULT"
        if (!getWorld(event.getWorld()).isWeather("DEFAULT")) {
            event.setCancelled(true);
        }
    }

    public void log(Level level, String message) {
        getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + message);
    }

    public void log(String message) {
        log(Level.INFO, message);
    }

    private WorldOptions getWorld(World world) {
        String name = world.getName();

        if (!worlds.containsKey(name)) {
            // Check if file for the given world exists
            File folder = getDataFolder();
            if (!folder.exists()) {
                folder.mkdir();
            }
            for (File file : folder.listFiles()) {
                if (file.getName().equals(name + ".yml")) {
                    WorldOptions options = new WorldOptions(this, world, file);
                    options.load();
                    worlds.put(name, options);
                    return options;
                }
            }

            // Create a new world file if the world does not have one yet
            File file = new File(folder, name + ".yml");
            try {
                file.createNewFile();
                WorldOptions options = new WorldOptions(this, world, file);
                options.load();
                worlds.put(name, options);
                return options;
            } catch (IOException e) {
                log(Level.SEVERE, "Could not create world file '" + file.getName() + "'.");
            }
        }

        return worlds.get(name);
    }

    private void checkGameMode(Player player) {
        GameMode change = getWorld(player.getWorld()).getGameMode();
        if (player.getGameMode() != change && !player.hasPermission("worldmanager.admin.gamemode")) {
            player.setGameMode(change);
        }
    }
}
