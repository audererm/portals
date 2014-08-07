/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package portals;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author michael
 */
public class CreationState 
{
    public World world;
    public Block firstBlock;
    public Block lastBlock;
    public State state;
    
    public CreationState(World w)
    {
        world = w;
        state = State.FIRST_BLOCK;
    }
    
    public enum State
    {
        FIRST_BLOCK,
        LAST_BLOCK;
    }
}
