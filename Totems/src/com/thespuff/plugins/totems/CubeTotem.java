package com.thespuff.plugins.totems;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

public class CubeTotem extends AreaTotem {
	
	private int radius;
	private Block center;

	
	CubeTotem(String owner, Block center, int radius){
		this.owner = owner;
		this.center = center;
		this.radius = radius;
	}

	CubeTotem(Player owner, Block center, int radius){
		this(owner.getName(), center, radius);
	}
	
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
	
	@Override
	public double contribution(Block block){
		
		int tX = Math.abs(block.getX()-center.getX());
		if(tX>radius) { return 0; }
		int tZ = Math.abs(block.getZ()-center.getZ());
		if(tZ>radius) { return 0; }
		int tY = Math.abs(block.getY()-center.getY());
		if(tY>radius) { return 0; }
		
		double weight = (1-((tX+tZ+tY)/(3*radius)));
		
		return weight;
	}
	
	@Override
	public double summary(String player, Block block, Interaction flag){
		
		boolean defaultTo = true; //Is this right?
		boolean isOwner;
		double permissionScale = 0;
		
		player = player.toLowerCase();
		
		if(!block.getWorld().equals(center.getWorld())) { return 0; }

		double weight = contribution(block);
		
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
