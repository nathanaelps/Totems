package com.thespuff.plugins.totems;

import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.thespuff.plugins.totems.Totems.Interaction;

public class WorldTotem extends AreaTotem {

	/*
	 * A WorldTotem will--by default--allow any action, including the sketchy ones like flying.
	 * Don't want people to fly here? Set fly: false in the config.
	 */
	
	private World world;
	
	public WorldTotem(String name, ConfigurationSection config) {
		world = Totems.server.getWorld(name);
		try{
			if(config.contains("flags")){
				setFlags(config.getConfigurationSection("flags"));
			}
		} catch (NullPointerException e) {
			log("Failed to properly load totem.");
		}
	}

	@Override
	public void save(){
		//"world."+worldName+".totems."+totemName
		String worldName = world.getName();
		String path = "world."+worldName+".";
		Totems.plugin.getConfig().set(path+"owner", owner);
		Set<Interaction> flagKeys = flags.keySet();
		for(Interaction flag : flagKeys){
			Totems.plugin.getConfig().set(path+"flags."+flag.getName(), flags.get(flag));
		}  
	}
	
	@Override
	public boolean affects(Block block) {
		return(block.getWorld().equals(world));
	}

	@Override
	public boolean permits(String player, Interaction flag){
		
		if(flags.containsKey(flag)) { return flags.get(flag); }
		else { return true; }
		
	}
	
}
