package com.thespuff.plugins.totems;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

public class CubeTotem extends AreaTotem {
	
	private int radius;
	private Block center;
//	private HashMap<Interaction, Boolean> flags = new HashMap<Interaction, Boolean>();
//	private List<String> friends = new ArrayList<String>();
//	private String owner = "server";

	
	CubeTotem(String owner, Block center, int radius){
		this.owner = owner;
		this.center = center;
		this.radius = radius;
	}

	CubeTotem(Player owner, Block center, int radius){
		this(owner.getName(), center, radius);
	}
	
	public double affects(Block block){

		int tX = Math.abs(block.getX()-center.getX());
		if(tX>radius) { return 0; }
		int tZ = Math.abs(block.getZ()-center.getZ());
		if(tZ>radius) { return 0; }
		int tY = Math.abs(block.getY()-center.getY());
		if(tY>radius) { return 0; }
		
		if(center.getWorld().equals(block.getWorld())) {
			return (1-((tX+tZ+tY)/(3*radius)));
		}
		
		return 0;
		
	}
	
	public double permits(String player, Block block, Interaction flag){
		boolean defaultTo = true; //Is this right?
		boolean isOwner;
		double permissionScale = 0;
		
		player = player.toLowerCase();
		
		double weight = affects(block);
		
		Boolean flagValue = flags.get(flag);
		if(flagValue==null) { flagValue = defaultTo; }

		if(owner.equalsIgnoreCase(player) || //if you own this totem, or
				friends.contains(player)) {//you're a friend of this totem
			//add distance-from-center weight to true
			isOwner = true;
		} else {//if it's not our totem, or our friend's totem,
			//add distance-from-center weight to false
			isOwner = false;
		}

		if(isOwner != flagValue) {
			if(defaultTo) { permissionScale += weight; }
			else { permissionScale -= weight; }
		} else {
			if(isOwner) { permissionScale += weight; }
			else { permissionScale -= weight; }
		}

		return permissionScale;
	}

}
