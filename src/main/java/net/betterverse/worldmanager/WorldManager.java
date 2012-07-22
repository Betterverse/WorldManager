package net.betterverse.worldmanager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.betterverse.worldmanager.util.YamlFile;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldManager extends JavaPlugin implements Listener {
    private final Map<String, Location> spawns = new HashMap<String, Location>();
    private YamlFile spawnFile;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("setspawn")) {
                    if (player.hasPermission("worldmanager.admin.setspawn")) {
                        World world = player.getWorld();
                        Location spawnLoc = player.getLocation();
                        spawns.put(world.getName(), spawnLoc);
                        saveSpawn(world, spawnLoc);

                        player.sendMessage(ChatColor.GREEN + "You set the spawn of world " + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + " to your location.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid arguments. '/" + cmdLabel + " setspawn'");
                }
            } else if (args.length == 2) {
                // TODO /wm tp <world>
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
        spawnFile = new YamlFile(this, new File(getDataFolder(), "spawns.yml"), "spawns");

        // Load spawn locations
        for (String key : spawnFile.getKeys("")) {
            spawns.put(
                    key,
                    new Location(getServer().getWorld(spawnFile.getString(key)), spawnFile.getDouble(key + ".x"), spawnFile.getDouble(key + ".y"), spawnFile.getDouble(key + ".z"), spawnFile
                            .getLong(key + ".yaw"), spawnFile.getLong(key + ".pitch")));
        }

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
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Allow players to spawn at their beds if possible
        if (!event.isBedSpawn()) {
            String world = event.getRespawnLocation().getWorld().getName();
            if (spawns.containsKey(world)) {
                event.setRespawnLocation(spawns.get(world));
            }
        }
    }

    public void log(Level level, String message) {
        getServer().getLogger().log(level, "[" + getDescription().getName() + "] " + message);
    }

    public void log(String message) {
        log(Level.INFO, message);
    }

    private void saveSpawn(World world, Location location) {
        spawnFile.set(world.getName() + ".x", location.getX());
        spawnFile.set(world.getName() + ".y", location.getY());
        spawnFile.set(world.getName() + ".z", location.getZ());
        spawnFile.set(world.getName() + ".pitch", location.getPitch());
        spawnFile.set(world.getName() + ".yaw", location.getYaw());

        spawnFile.save();
    }
}