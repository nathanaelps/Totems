package com.thespuff.plugins.totems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.thespuff.plugins.totems.Totems.Interaction;

public class AreaTotem implements Totem {

	protected HashMap<Interaction, Boolean> flags = new HashMap<Interaction, Boolean>();
	protected List<String> friends = new ArrayList<String>();
	protected String owner = "server";

	
	@Override
	public boolean affects(Block block) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean permits(String player, Interaction flag) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public double contribution(Block block) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double summary(String player, Block block, Interaction flag) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setFlag(Interaction flag, boolean value) {
		flags.put(flag, value);
	}
	public void setFlag(String flag, boolean value) {
		setFlag(Interaction.valueOf(flag.toUpperCase()), value);
	}
	public void setFlags(boolean value, Interaction... flags) {
	    for ( int i = 0; i < flags.length; ++i ) {
	    	setFlag(flags[i], value);
	    }
	}
	
	public void removeFlag(Interaction flag) {
		flags.remove(flag);
	}
	public void removeFlag(String flag) {
		removeFlag(Interaction.valueOf(flag.toUpperCase()));
	}
	public void removeFlags(Interaction... flags) {
	    for ( int i = 0; i < flags.length; ++i ) {
	    	removeFlag(flags[i]);
	    }
	}
	
	public void setOwner(String player){
		owner = player;
	}
	public void setOwner(Player player){
		setOwner(player.getName());
	}
	public String getOwner() { return owner; }
	public List<String> getFriends() { return friends; }
	public HashMap<Interaction, Boolean> getFlags() { return flags; }
	public boolean getFlag(String flag) { return this.flags.get(Interaction.valueOf(flag)); }
	public boolean getFlag(Interaction flag) { return this.flags.get(flag); }
	
	public void addFriend(String friend) {
		friends.add(friend);
	}
	public void removeFriend(String friend) {
		friends.remove(friend);
	}


}
