package portals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import portals.CreationState.State;

/**
 *
 * @author michael
 */
public class Portals extends JavaPlugin implements Listener {
    
    public HashMap<Player, CreationState> creationMap;
    public File portalFile;
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        creationMap = new HashMap<>();
        portalFile = new File(getDataFolder().getPath() + "/portals.dat");
        Bukkit.getLogger().info("Enabling portals...");
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
                        savePortal(state);
                        player.sendMessage("Portal to " + state.world
                                + " created!");
                        creationMap.remove(player);
                        break;
                }
            }
        }
    }
    
    public void savePortal(CreationState state)
    {
       int x1 = state.firstBlock.getX();
       int x2 = state.lastBlock.getX();
       int y1 = state.firstBlock.getY();
       int y2 = state.lastBlock.getY();
       int z1 = state.firstBlock.getZ();
       int z2 = state.lastBlock.getZ();
       savePortal(state.firstBlock.getWorld().getName(), state.world.getName(),
               x1, x2, y1, y2, z1, z2);
    }
    
    public void savePortal(String world, String destination, 
            int x1, int x2, int y1, int y2, int z1, int z2)
    {
              try
       {
           PrintWriter writer = new PrintWriter(portalFile);
           writer.println(world + ":"
                            + x1 + "," + y1 + "," + z1 + ":"
                            + x2 + "," + y2 + "," + z2 + ":"
                            + destination);
           writer.close();
       }
       catch (FileNotFoundException e)
       {
           try
           {
               portalFile.createNewFile();
           }
           catch (IOException e2)
           {
               e2.printStackTrace();
           }
       }
    }
}
