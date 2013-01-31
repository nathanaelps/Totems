package com.thespuff.plugins.totems;

import org.bukkit.block.Block;

import com.thespuff.plugins.totems.Totems.Interaction;

public interface Totem {
	
	/**
	 * Does this totem affect this block?
	 * @param block Block to be examined
	 * @return
	 */
	public boolean affects(Block block);
	
	/**
	 * Does this totem permit this player to do this?
	 * @param player The player in question
	 * @param flag The action
	 * @return
	 */
	public boolean permits(String player, Interaction flag);
	
	/**
	 * How much does this totem affect this block?
	 * @param block The block affected
	 * @return
	 */
	public double contribution(Block block);
	
	/**
	 * Summary of the totemic effect on this player at this block.
	 * Returns a negative number for denial, a positive number for permission,
	 * and 0 if there is no effect.
	 * @param player
	 * @param block
	 * @param flag
	 * @return
	 */
	public double summary(String player, Block block, Interaction flag);
	
	public String getOwner();
	
}
