package com.thespuff.plugins.totems;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Commands {

//	private static String pluginName = Totems.pluginName;
//	private static String pluginVersion = Totems.pluginVersion;
//	private static Server server = Totems.server;
	private static Totems plugin = Totems.plugin;
	
	public static boolean whose(Player player) {
		if(!player.hasPermission("totems.command.whose")) {
			player.sendMessage("You don't have permissions to ask such questions!");
			return true;
		}
		Location location = player.getLocation();

		try{
			HashMap<String, Double> affectiveTotems = Utils.getTotemsAtLocation(location);
			
			if(affectiveTotems.isEmpty()){
				player.sendMessage("This area is not protected.");
				return true;
			} else {
				String out = "(World: "+location.getWorld().getName()+")";
				Iterator<String> outIt = affectiveTotems.keySet().iterator();
				while(outIt.hasNext()){
					out = out+", "+outIt.next();
				}
				player.sendMessage(out);
				return true;
			}
		} catch (NullPointerException e) {
			plugin.log("NPE in Commands.whose()");
		}
		return false;
	}

	public static boolean setTotemDefault(Player player, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
}
