package com.thespuff.plugins.totems;

import org.bukkit.block.Block;

import com.thespuff.plugins.totems.Totems.Interaction;

public interface Totem {
	
	public double affects(Block block);
	public double permits(String player, Block block, Interaction flag);
	
}
