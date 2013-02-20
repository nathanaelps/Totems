package com.thespuff.plugins.totems;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

public class Utils {

	private static Server server = Totems.server;
	private static Totems plugin = Totems.plugin;
	
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
		
		World world = block.getWorld();
		
		
		AreaTotem temp = new AreaTotem();

		boolean byDefault = Totems.worldTotems.get(block.getWorld()).permits(playerName, flag);

		for(AreaTotem totem : Totems.areaTotems){
			if(totem.affects(block) && totem.getPrecedence()>temp.getPrecedence()){
				temp = totem;
			}
		}
		
		String ownerName = temp.getOwner();
				
		boolean byFriendly = ( temp.isFriendly(playerName) || Totems.friendList.get(ownerName).friendlyTowards(playerName) );
		
		//World Default, Is Totem Owner, Totem Permits: Two out of three makes it true.
		
		if((byFlag && (byDefault || byFriendly)) || (byDefault && byFriendly)) { return true; }
		return false;
	}
	
	/* End canEdit =======================================================================*/
	
	public static Totem getAffectiveTotem(Block block){
		AreaTotem out = new AreaTotem();

		for(AreaTotem totem : Totems.areaTotems){
			if(totem.affects(block) && totem.getPrecedence()>out.getPrecedence()){
				out = totem;
			}
		}

		return out;
	}
	
	public static boolean isOwner(Object player, Block block) {
		String playerName = "";
		if(player instanceof String) {
			playerName = (String) player;
		} else if (player instanceof Player) {
			playerName = ((Player) player).getName();
		}
		if(getAffectiveTotem(block).getOwner().equalsIgnoreCase(playerName)) { return true; }
		return false;
	}


	public static void getTotem(Block totemBlock) {
		// TODO Auto-generated method stub
		
	}

	public static void loadFriendListsFromConfig() {
		
//		Set<Totem> allTotems = Totems.allTotems;
		HashMap<String, FriendList> friendList = Totems.friendList;
				
		// Load world defaults

		Set<String> playerNames = plugin.getConfig().getConfigurationSection("friendlist").getKeys(false);
		for(String playerName:playerNames){
			friendList.put(playerName, new FriendList(playerName, plugin.getConfig().getConfigurationSection("friendlist."+playerName)));
		}
	}
	
	public static void loadTotemsFromConfig() {
		
//		Set<Totem> allTotems = Totems.allTotems;
		HashMap<World, WorldTotem> worldTotems = Totems.worldTotems;
		Set<AreaTotem> areaTotems = Totems.areaTotems;
		
		// Load server defaults
		
//		allTotems.add(new ServerTotem(plugin.getConfig().getConfigurationSection("defaults")));
		
		// Load world defaults

		List<World> worlds = server.getWorlds();
		for(World world:worlds){
			if(!plugin.getConfig().contains("world."+world.getName())) { continue; }
			worldTotems.put(world, new WorldTotem(world.getName(), plugin.getConfig().getConfigurationSection("world."+world.getName())));

			// While we're here, Load totems for world
			Set<String> totemNames = plugin.getConfig().getConfigurationSection("world."+world.getName()+".totems").getKeys(false);
			for(String totemName:totemNames){
				areaTotems.add(new CubeTotem(plugin.getConfig().getConfigurationSection("world."+world.getName()+".totems."+totemName)));
			
			}
		}
	}

	public static void saveTotemsToConfig() {
		
		Collection<WorldTotem> worldTotems = Totems.worldTotems.values();
		
		for(Totem totem : worldTotems){
			totem.save();
		}
		
		for(Totem totem : Totems.areaTotems){
			totem.save();
		}
		plugin.saveConfig();
	}
}
