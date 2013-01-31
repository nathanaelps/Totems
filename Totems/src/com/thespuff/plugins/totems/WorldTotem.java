package com.thespuff.plugins.totems;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.thespuff.plugins.totems.Totems.Interaction;

public class WorldTotem extends AreaTotem {

	private World world;
	
	@Override
	public boolean affects(Block block) {
		return(block.getWorld().equals(world));
	}

	@Override
	public boolean permits(String player, Interaction flag){
		
		if(flags.containsKey(flag)) { return flags.get(flag); }
		else { return false; }
		
	}
	
	@Override
	public double contribution(Block block) {
		if(!affects(block)) { return 0; }
		
		return 1;
	}

	@Override
	public double summary(String player, Block block, Interaction flag) {
		if(affects(block)) {
			if(permits(player, flag)) { return 1; }
		}
		
		return 0;
	}

}
