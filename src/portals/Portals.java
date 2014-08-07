package portals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import portals.CreationState.State;

/**
 *
 * @author michael
 */
public class Portals extends JavaPlugin implements Listener 
{
    public HashMap<Player, CreationState> creationMap;
    public List<PortalRegion> portals;
    public File portalFile;
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        creationMap = new HashMap<>();
        portals = new ArrayList<>();
        portalFile = new File(getDataFolder().getPath() + "/portals.dat");
        if (!portalFile.exists())
        {
            getDataFolder().mkdir();
            try
            {
                portalFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try (BufferedReader reader = new BufferedReader(new FileReader(portalFile)))
            {
                String line = reader.readLine();
                while (line != null)
                {
                    String[] temp = line.split(":");
                    World world = getServer().getWorld(temp[0]);
                    Location loc1 = csvLocation(world, temp[1]);
                    Location loc2 = csvLocation(world, temp[2]);
                    World dest = getServer().getWorld(temp[3]);
                    portals.add(new PortalRegion(world, dest, 
                        loc1.getBlockX(), loc2.getBlockX(),
                        loc1.getBlockY(), loc2.getBlockY(),
                        loc1.getBlockZ(), loc2.getBlockZ()));
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        Bukkit.getLogger().info("Enabling portals...");
    }
    
    private Location csvLocation(World world, String csv)
    {
        String[] temp = csv.split(",");
        return new Location(world, Integer.valueOf(temp[0]),
            Integer.valueOf(temp[1]), Integer.valueOf(temp[2]));
    }
    
    @Override
    public void onDisable()
    {
        Bukkit.getLogger().info("Portals is now disabled.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args)
    {
        if (label.equalsIgnoreCase("createportal"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("You cannot do this from the console.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("portals.create"))
            {
                player.sendMessage("You do not have permission to do this.");
                return true;
            }
            switch (args.length)
            {
                case 1:
                    World world;
                    if ((world = player.getServer().getWorld(args[0])) != null)
                    {
                        creationMap.put(player, new CreationState(world));
                        player.sendMessage("Please right-click the first block.");
                        return true;
                    }
                    else
                    {
                        player.sendMessage("That world does not exist!");
                        return true;
                    }
                    
                default:
                    player.sendMessage("Invalid syntax. Usage:");
                    return false;
            }
        }
        
        return false;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        for (PortalRegion portal : portals)
        {
            if (portal.contains(event.getTo()))
            {
                event.setCancelled(true);
                event.getPlayer().teleport(portal.getDestination().getSpawnLocation());
                break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            Player player = event.getPlayer();
            if (creationMap.containsKey(player))
            {
                CreationState state = creationMap.get(player);
                switch (state.state)
                {
                    case FIRST_BLOCK:
                        state.firstBlock = event.getClickedBlock();
                        state.state = State.LAST_BLOCK;
                        player.sendMessage("Please right-click the second block.");
                        creationMap.put(player, state);
                        break;
                    case LAST_BLOCK:
                        state.lastBlock = event.getClickedBlock();
                        createPortal(state);
                        player.sendMessage("Portal to " + state.world
                                + " created!");
                        creationMap.remove(player);
                        break;
                }
            }
        }
    }
    
    private void createPortal(CreationState state)
    {
        portals.add(new PortalRegion(state.firstBlock.getWorld(),
            state.world,
            state.firstBlock.getX(), state.lastBlock.getX(),
            state.firstBlock.getY(), state.lastBlock.getY(),
            state.firstBlock.getZ(), state.lastBlock.getZ()));
    }
}
