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
	 * Returns the state of the flag.
	 * @param flag The flag to check.
	 * @return
	 */
	public boolean getFlag(Interaction flag);
	
	/**
	 * Is this player friendly to this totem?
	 * Includes the owner.
	 * @param player The player to be tested
	 * @return
	 */
	public boolean isFriendly(String player);
	
	/**
	 * Returns the owner of this totem as a string.
	 * @return The name of the totem owner.
	 */
	public String getOwner();

	/**
	 * Adds the totem to the currently loaded Config file.
	 * Does not save the file, only adds the totem.
	 */
	public void save();

	public long getPrecedence();
	
}
