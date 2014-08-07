package portals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author michael
 */
public class PortalRegion 
{
    private final World world;
    private final World destination;
    private final int xMin, xMax, yMin, yMax, zMin, zMax;
    
    public PortalRegion(World w, World dest, int x1, int x2, int y1, int y2,
            int z1, int z2)
    {
        world = w;
        destination = dest;
        xMin = Math.min(x1, x2);
        xMax = Math.max(x1, x2);
        yMin = Math.min(y1, y2);
        yMax = Math.max(y1, y2);
        zMin = Math.min(z1, z2);
        zMax = Math.max(z1, z2);
    }
    public World getWorld()
    {
        return world;
    }
    
    public World getDestination()
    {
        return destination;
    }
    
    public boolean contains(Block block)
    {
        return contains(block.getLocation());
    }
    
    public boolean contains(Player player)
    {
        return contains(player.getLocation());
    }
    
    public boolean contains(Location location)
    {
        return xMin <= location.getX() &&
                yMin <= location.getY() &&
                zMin <= location.getZ() &&
                xMax >= location.getX() &&
                yMax >= location.getY() &&
                zMax >= location.getZ();
    }
    
    public void savePortal(File portalFile)
    {
       try
       {
           PrintWriter writer = new PrintWriter(portalFile);
           writer.println(world + ":"
                            + xMin + "," + yMin + "," + zMin + ":"
                            + xMax + "," + yMax + "," + zMax + ":"
                            + destination);
           writer.close();
       }
       catch (FileNotFoundException e)
       {
           e.printStackTrace();
       }
    }
}
