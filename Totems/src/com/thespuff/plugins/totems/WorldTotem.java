package com.thespuff.plugins.totems;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.thespuff.plugins.totems.Totems.Interaction;

public class WorldTotem extends AreaTotem {
//	protected HashMap<Interaction, Boolean> flags = new HashMap<Interaction, Boolean>();
//	protected List<String> friends = new ArrayList<String>();
//	protected String owner = "server";
	private World world;
	private boolean defaultTo = false;
	
	@Override
	public double affects(Block block) {
		if(block.getWorld().equals(world)) {
			return 1;
		}
		return 0;
	}

	@Override
	public double permits(String player, Block block, Interaction flag){
		double weight = affects(block);
		if(weight<=0) { return weight; }
		Boolean flagValue = flags.get(flag);
		if(flagValue==null) {
			if(defaultTo) { return weight; }
			else { return -weight; }

		} else {
			if(flagValue) { return weight; }
			else { return -weight; }
		}
	}

}
