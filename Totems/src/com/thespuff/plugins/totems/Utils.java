package com.thespuff.plugins.totems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

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

	public static boolean setWorldDefault(World world, String flag, boolean wDefault) {
		if(Interaction.valueOf(flag.toUpperCase()) == null) { return false; }
		
		plugin.getConfig().set("totems."+world.getName()+".defaults."+flag.toLowerCase(), wDefault);
		
		return true;
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
		
		out.remove("defaults");
		return out;
	}
	
//	public static HashMap<String,Double> getDEFAULTTotemsAffectingLocation(Location location) {
//		return getDEFAULTTotemsAffectingLocation(location.getBlock());
//	}
//	
//	public static HashMap<String,Double> getDEFAULTTotemsAffectingLocation(Block block) {
//		String path = "";
//		String world = block.getWorld().getName();
//		HashMap<String,Double> out = new HashMap<String,Double>();
//		double radius = 0.0;
//		int tX,tY,tZ;
//		int x = block.getX();
//		int y = block.getY();
//		int z = block.getZ();
//		
//		Set<String> totems = plugin.getConfig().getConfigurationSection("totems."+world).getKeys(false);
//		for(String totem:totems){
//			if(totem.equals("defaults")) { continue; }
//			path = "totems."+world+"."+totem;
//			radius = plugin.getConfig().getDouble(path+".radius");
//			tX = Math.abs(plugin.getConfig().getInt(path+".x")-x);
//			if(tX>radius) { continue; }
//			tZ = Math.abs(plugin.getConfig().getInt(path+".z")-z);
//			if(tZ>radius) { continue; }
//			tY = Math.abs(plugin.getConfig().getInt(path+".y")-y);
//			if(tY>radius) { continue; }
//			out.put(totem, 1-((tX+tZ+tY)/(3*radius)));
//		}
//		
//		out.remove("defaults");
//		return out;
//	}
	
	/* canEdit, the queen of Sea-Cows =======================================================================*/
	
	public static boolean canEdit(Object player, Block block, Object flag) {
		if(flag instanceof Interaction) {
			if(player instanceof String) {
				return canEditMeat((String) player, block, (Interaction) flag);
			} else if (player instanceof Player) {
				return canEditMeat(((Player) player).getName(), block, (Interaction) flag);
			}
		} else if (flag instanceof String) {
			if(player instanceof String) {
				return canEditMeat((String) player, block, Interaction.valueOf((String) flag));
			} else if (player instanceof Player) {
				return canEditMeat(((Player) player).getName(), block, Interaction.valueOf((String) flag));
			}

		}

		//If all else fails, 
		return false;
	}
		
	public static boolean canEditMeat(String playerName, Block block, Interaction flag) {
		Set<Totem> totems = Utils.getTotemsAffectingLocation(block);
		double permission=0;
		
		for(Totem totem : totems){
			permission += totem.contribution(block);
		}
		return (permission>0);
	}
	
	/* End canEdit =======================================================================*/
	
	public static Set<Totem> getTotemsAffectingLocation(Location location) {
		return getTotemsAffectingLocation(location.getBlock());
	}
	

	public static Set<Totem> getTotemsAffectingLocation(Block block) {
		Set<Totem> out = new HashSet<Totem>();

		for(Totem totem : Totems.allTotems){
			if(totem.affects(block)){
				out.add(totem);
			}
		}
		
		return out;
	}
	
	public static boolean isOwner(Object player, Block block) {
		double permissionScale = 0.0;
		String inPlayer = "";
		
		if(player instanceof String) {
			inPlayer = (String) player;
		} else if (player instanceof Player) {
			inPlayer = ((Player) player).getName();
		} else {
			return false;
		}
		
		Set<Totem> totems = getTotemsAffectingLocation(block);

		for(Totem totem:totems){
			double contribution = totem.contribution(block);
			if(contribution>0) {
				if(totem.getOwner().equalsIgnoreCase(inPlayer)) {
					permissionScale += contribution;
				} else {
					permissionScale -= contribution;
				}
			}
		}

		return (permissionScale>0);
	}


	public static void getTotem(Block totemBlock) {
		// TODO Auto-generated method stub
		
	}

	public static Set<Totem> getAllTotems() {
		Set<Totem> out = new HashSet<Totem>();

		List<World> worldList = server.getWorlds();
		
		for(World world : worldList){
			
			String path="";
			try{
				Set<String> totems = plugin.getConfig().getConfigurationSection("totems."+world.getName()).getKeys(false);
				totems.remove("defaults");
				for(String totem:totems){
					if(totem.equalsIgnoreCase("defaults")) { continue; }
					path = "totems."+world.getName()+"."+totem;
					
					String owner = plugin.getConfig().getString(path+".owner");
					
					int x = plugin.getConfig().getInt(path+".x");
					int z = plugin.getConfig().getInt(path+".z");
					int y = plugin.getConfig().getInt(path+".y");
					Block block = world.getBlockAt(x, y, z);
					
					int radius = plugin.getConfig().getInt(path+".radius");

					CubeTotem area = new CubeTotem(owner, block, radius);
					
					 Set<String> flags = Totems.plugin.getConfig().getConfigurationSection(path+".flags").getKeys(false);
					 
					 for(String flag: flags){
						 area.setFlag(flag, Totems.plugin.getConfig().getBoolean(path+".flags."+flag));
					 }
					
					out.add(area);
				}
			} catch(NullPointerException e) {
				plugin.log("No totems recorded for world "+ world.getName());
				return null;
			}

			out.remove("defaults");
		}


		return out;
	}
}
