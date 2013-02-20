package com.thespuff.plugins.totems;


import java.text.SimpleDateFormat;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

public class CubeTotem extends AreaTotem {
	
	private int radius;
	private Block center;

	
	public CubeTotem(String owner, Block center, int radius){
		this.owner = owner;
		this.center = center;
		this.radius = radius;
		
		//TODO: Is this really the best way to do this?
		this.precedence = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").toString());
	}

	public CubeTotem(Player owner, Block center, int radius){
		this(owner.getName(), center, radius);
	}
	
	public CubeTotem(ConfigurationSection config) {
		try{
			owner = config.getString("owner");
			precedence = config.getLong("precedence");

			
			World world = Totems.server.getWorld(config.getString("world"));
			int x = config.getInt("x");
			int y = config.getInt("y");
			int z = config.getInt("z");
			center = world.getBlockAt(x,y,z);
			
			radius = config.getInt("radius");

			if(config.contains("flags")){
				setFlags(config.getConfigurationSection("flags"));
			}
		} catch (NullPointerException e) {
			log("Failed to load totem.");
		}
	}
	
	@Override
	public void save(){
		String totemName = String.format("%05d", center.getX())+String.format("%05d", center.getY())+String.format("%05d", center.getZ());
		String worldName = center.getWorld().getName();
		String path = "world."+worldName+".totems."+totemName+".";

		Totems.plugin.getConfig().set(path+"owner", owner);
		Totems.plugin.getConfig().set(path+"precedence", precedence);
		
		Totems.plugin.getConfig().set(path+"radius", radius);
		Totems.plugin.getConfig().set(path+"world", center.getWorld().getName());
		Totems.plugin.getConfig().set(path+"x", center.getX());
		Totems.plugin.getConfig().set(path+"y", center.getY());
		Totems.plugin.getConfig().set(path+"z", center.getZ());

		Set<Interaction> flagKeys = flags.keySet();
		for(Interaction flag : flagKeys){
			Totems.plugin.getConfig().set(path+"flags."+flag.getName(), flags.get(flag));
		}  
	}
	
	public Block getCenter(){
		return center;
	}
	
	@Override
	public boolean affects(Block block){
		if(!block.getWorld().equals(center.getWorld())) { return false; }

		int tX = Math.abs(block.getX()-center.getX());
		if(tX>radius) { return false; }
		int tZ = Math.abs(block.getZ()-center.getZ());
		if(tZ>radius) { return false; }
		int tY = Math.abs(block.getY()-center.getY());
		if(tY>radius) { return false; }
		
		return true;
		
	}
	
	@Override
	public boolean permits(String player, Interaction flag){
		
		player = player.toLowerCase();
		
		if(flags.containsKey(flag)) {}
		Boolean flagValue = flags.get(flag);
		if(flagValue==null) { return false; }

		if(owner.equalsIgnoreCase(player) || //if you own this totem, or
				friends.contains(player)) {//you're a friend of this totem
			return flagValue;
		} 

		return false;
	}
	
}
