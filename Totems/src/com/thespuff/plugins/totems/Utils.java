package com.thespuff.plugins.totems;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Utils {

//	private static String pluginName = Totems.pluginName;
//	private static String pluginVersion = Totems.pluginVersion;
	private static Server server = Totems.server;
	private static Totems plugin = Totems.plugin;
	
	public static HashMap<String,Block> getTotems(){
		HashMap<String,Block> out = new HashMap<String,Block>();
		List<World> worldList = server.getWorlds();
		for(World world : worldList){
			try{
				out.putAll(getTotems(world.getName()));
			}catch(NullPointerException e) {/*Do Nothing*/}
		}
		return out;
	}
	
	public static HashMap<String,Block> getTotems(Object world){
		World inWorld = null;
		if(world instanceof String) {
			inWorld = server.getWorld((String) world);
			if(inWorld==null) { return null; }
		} else if(world instanceof World) {
			inWorld = (World) world;
		} else {
			plugin.log("Bad Object type in Utils.getTotems()");
			return null;
		}

		HashMap<String,Block> out = new HashMap<String,Block>();
		
		String path="";
		try{
		Set<String> totems = plugin.getConfig().getConfigurationSection("totems."+inWorld.getName()).getKeys(false);
		for(String totem:totems){
			if(totem.equalsIgnoreCase("defaults")) { continue; }
			path = "totems."+inWorld.getName()+"."+totem;
			int x = plugin.getConfig().getInt(path+".x");
			int z = plugin.getConfig().getInt(path+".z");
			int y = plugin.getConfig().getInt(path+".y");
			Block block = inWorld.getBlockAt(x, y, z);
			out.put(totem, block);
		}
		} catch(NullPointerException e) {
			plugin.log("No totems recorded for world "+ inWorld.getName());
			return null;
		}

		return out;
	}
	
	public static HashMap<String,Double> getTotemsAtLocation(Location location) {
		String path = "";
		String world = location.getWorld().getName();
		HashMap<String,Double> out = new HashMap<String,Double>();
		double radius = 0.0;
		int tX,tY,tZ;
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		
		Set<String> totems = plugin.getConfig().getConfigurationSection("totems."+world).getKeys(false);
		for(String totem:totems){
			if(totem.equals("defaults")) { continue; }
			path = "totems."+world+"."+totem;
			radius = plugin.getConfig().getDouble(path+".radius");
			tX = plugin.getConfig().getInt(path+".x");
			if(Math.abs(tX-x)>radius) { continue; }
			tZ = plugin.getConfig().getInt(path+".z");
			if(Math.abs(tZ-z)>radius) { continue; }
			tY = plugin.getConfig().getInt(path+".y");
			if(Math.abs(tY-y)>radius) { continue; }
			out.put(totem, ((tX+tZ+tY)/(3*radius)));
		}
		return out;
	}
}
