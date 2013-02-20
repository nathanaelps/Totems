package com.thespuff.plugins.totems;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Totems extends JavaPlugin implements Listener {

	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static Totems plugin;
	
	public static BukkitTask asyncTask;
	
	private static HashSet<Player> playerChangeStateList = new HashSet<Player>();
	public static HashMap<String, FriendList> friendList = new HashMap<String, FriendList>();
	public static HashMap<World, WorldTotem> worldTotems = new HashMap<World, WorldTotem>();
	public static Set<AreaTotem> areaTotems = new HashSet<AreaTotem>();

	public enum Interaction {
		ANVIL,
		BED,
		BREAK,
		CONTAINER,
		DAMAGEBLOCK,
		EXPLODE,
		EXPLODECREEPER,
		EXPLODEFIREBALL,
		EXPLODETNT,
		FIRESPREAD,
		FLY,
		GRASSGROW,
		HEARTHSTONE,
		IGNITE,
		MAGIC,
		MOBHEALTHBOOST,
		MYCELGROW,
		PLACE,
		POISON,
		POTION,
		PVE,
		PVP,
		SHEAR,
		SLAUGHTER,
		SPAWNFRIENDLY,
		SPAWNUNFRIENDLY,
		STONEMACHINE,
		TELEPAD,
		TELEPORT,
		WOODMACHINE;
		
		public String getName(){
			return this.toString().toLowerCase();
		}
	}

	public void onSave(WorldSaveEvent event) {
		
	}
	
	public void onDisable() {
		this.getServer().getScheduler().cancelTask(asyncTask.getTaskId());//cancelAllTasks();
		
		Utils.saveTotemsToConfig();

		log("Disabled");
	}

	public void onEnable() {
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		server = this.getServer();
		plugin = this;
		int secondsPerPulse = 8;

		getServer().getPluginManager().registerEvents(this, this);
		
		Utils.loadTotemsFromConfig();

		asyncTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() { public void run() { applyFlagEffects(); } }, 60, secondsPerPulse*20);
		
		log("Enabled.");
	}

	protected void applyFlagEffects() {
		Player[] players = server.getOnlinePlayers();
		for(int i=0; i<players.length; i++){
			Player player = players[i];
			if(!player.hasPermission("totems.special.mayFly")) { //Flight
				player.setFlySpeed(.1f);
				if(player.getGameMode()==GameMode.SURVIVAL) {
					player.setAllowFlight(canEdit(player, player.getLocation().getBlock(), Interaction.FLY));
				}
			}
		}
	}


	public void log(Object in) {
		System.out.println("[" + pluginName + "] " + String.valueOf(in));
	}
	
	/* Special totem-related functions ======================================================================*/
	
	//Breaking a totem
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTotemBreak(BlockBreakEvent event) {
		try {
			if(event.getBlock().hasMetadata("Totem")) {
				String world = event.getBlock().getWorld().getName();
				String serial = event.getBlock().getMetadata("Totem").get(0).asString();
				String player = event.getPlayer().getName();
				String owner = this.getConfig().getString("totems."+world+"."+serial+".owner");
				if(owner.equalsIgnoreCase(player)) {
					this.getConfig().set("totems."+world+"."+serial, null);
					event.getBlock().removeMetadata("Totem", this);
					event.getPlayer().sendMessage("You broke a totem!");
					saveConfig();
				} else {
					event.setCancelled(true);
					event.getPlayer().sendMessage("You may not break "+owner+"'s totem.");
				}
			}
		} catch (NullPointerException e) {
			return;
		}
	}

	//Building a totem
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTotemCreate(PlayerInteractEvent event) {
		try {
			if(event.isCancelled()) { return; } //Not allowed to interact. Does this ever get called?
			if(!event.hasBlock()) { return; }
			if(!canEdit(event.getPlayer(),event.getClickedBlock(),Interaction.PLACE)) { return; }
			if(event.getItem().getTypeId()!=284) { return; } //Gold Spade
			if(event.getClickedBlock().hasMetadata("Totem")) { return; }

			HashMap<String,Boolean> flags = new HashMap<String,Boolean>();
			float radius = 5;
			
			switch(event.getClickedBlock().getTypeId()){
				case 57: //Diamond block defaults to the following permissions:
					flags.put("anvil", false);
					flags.put("bed", false);
					flags.put("break", false);
					flags.put("container", false);
					flags.put("explode", false);
					flags.put("firespread", false);
					flags.put("hearthstone", true);
					flags.put("ignite", false);
					flags.put("mobHealthBoost", false);
					flags.put("place", false);
					flags.put("slaughter", false);
					flags.put("stonemachine", false);
					radius = 20f;
					break;
				case 41: //Gold block
					flags.put("break", false);
					flags.put("explode", false);
					flags.put("firespread", false);
					flags.put("hearthstone", true);
					flags.put("place", false);
					radius = 3f;
					break;
				default:
					return;
			}
			Set<String> keys = flags.keySet();

			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			
			List<String> friends = new ArrayList<String>();
			
			String serial = (new SimpleDateFormat("yyMMddHHmmss").format(new Date()));


			String path = "totems."+player.getWorld().getName()+"."+serial;
			this.getConfig().set(path+".x", block.getX());
			this.getConfig().set(path+".y", block.getY());
			this.getConfig().set(path+".z", block.getZ());
			this.getConfig().set(path+".radius", radius);
			for(String key:keys){
				this.getConfig().set(path+".flags."+key, flags.get(key));
			}
			this.getConfig().set(path+".friends", friends);
			this.getConfig().set(path+".owner", player.getName());

			block.setMetadata("Totem", new FixedMetadataValue(this, serial));
			
			player.sendMessage("Totem created!");

		} catch (NullPointerException e) {
			return;
		}
	}

	@EventHandler public void onPlayerChangeState(PlayerMoveEvent event) {
		if(event.getFrom().equals(event.getTo())) { return; }
		if(event.getPlayer().getItemInHand().getType().equals(Material.MAP)) {
			Player player = event.getPlayer();
			if(Utils.isOwner(player.getName(), player.getLocation().getBlock())) {
				if(!playerChangeStateList.contains(player)) {
					playerChangeStateList.add(player);
					player.sendMessage("You own this area.");
				}
			} else {
				if(playerChangeStateList.contains(player)) {
					playerChangeStateList.remove(player);
					player.sendMessage("You don't own this area.");
				}
			}
		}

	}
	
	/* Events to watch for =================================================================================*/
		
	@EventHandler (priority = EventPriority.LOW) public void onBlockBreak(BlockBreakEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getBlock(),Interaction.BREAK));
	}
	
	@EventHandler (priority = EventPriority.LOW) public void onBlockSpread(BlockSpreadEvent event) {
		if(event.getSource().getTypeId()==110) {
			event.setCancelled(!canEdit("NoPlayerEvent",event.getBlock(),Interaction.MYCELGROW));
		} else if(event.getSource().getTypeId()==2) {
			event.setCancelled(!canEdit("NoPlayerEvent",event.getBlock(),Interaction.GRASSGROW));
		}
	}
	
	@EventHandler public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getBlock(),Interaction.PLACE));
	}

	@EventHandler public void onHangingBreak(HangingBreakEvent event) {
		switch(event.getCause()){
		case EXPLOSION:
			event.setCancelled(!canEdit("NoPlayerEvent",event.getEntity().getLocation().getBlock(),Interaction.EXPLODE));
			break;
		case ENTITY:
			//Deal with this in onHangingBreakByEntity
			break;
		case OBSTRUCTION:
		case PHYSICS:
			event.setCancelled(!canEdit("NoPlayerEvent",event.getEntity().getLocation().getBlock(),Interaction.BREAK));
			break;
		default:
			event.setCancelled(!canEdit("NoPlayerEvent",event.getEntity().getLocation().getBlock(),Interaction.BREAK));
			break;
		}
	}

	@EventHandler public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if(!(event.getRemover() instanceof Player)) {
			event.setCancelled(!canEdit("NoPlayerEvent",event.getEntity().getLocation().getBlock(),Interaction.BREAK));
		} else {
			if(((Player) event.getRemover()).isOp()) { return; }			
			event.setCancelled(!canEdit((Player) event.getRemover(),event.getEntity().getLocation().getBlock(),Interaction.BREAK));
		}
	}

	@EventHandler public void onHangingPlace(HangingPlaceEvent event) {
		if(event.getPlayer().isOp()) { return; }			
		event.setCancelled(!canEdit(event.getPlayer(),event.getEntity().getLocation().getBlock(),Interaction.PLACE));
	}

	@EventHandler public void onEntityExplode(EntityExplodeEvent event) {
		event.setCancelled(!canEdit("NoPlayerEvent",event.getLocation().getBlock(),Interaction.EXPLODE));
		if(event.isCancelled()) { return; }
		try{
			if(event.getEntityType().equals(EntityType.CREEPER)) {
				event.setCancelled(!canEdit("NoPlayerEvent",event.getLocation().getBlock(),Interaction.EXPLODECREEPER));
			} else if(event.getEntityType().equals(EntityType.FIREBALL)) {
				event.setCancelled(!canEdit("NoPlayerEvent",event.getLocation().getBlock(),Interaction.EXPLODEFIREBALL));
			} else if(event.getEntityType().equals(EntityType.PRIMED_TNT)) {
				event.setCancelled(!canEdit("NoPlayerEvent",event.getLocation().getBlock(),Interaction.EXPLODETNT));
			}
		} catch(NullPointerException e) { return; }
	}
	
	@EventHandler public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getBed(),Interaction.BED));
	}

	
	@EventHandler public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getPlayer().isOp()) { return; }
		int mat;
		Block block;
		try{
			block = event.getClickedBlock();
			mat = block.getTypeId();
		} catch (NullPointerException e) { //Air will give 'null'.
			return;
		}
		switch (mat) {
			//Wood machines!
			case 64: // wood door
			case 69: // lever
			case 72: // wood plate
			case 96: // trapdoor
			case 107: // gate
			case 131: // tripwire hook
			case 132: // tripwire
			case 143: // wood button
				event.setCancelled(!canEdit(event.getPlayer(),block,Interaction.WOODMACHINE));
				break;
			
			//Stone machines!
			case 70: // stone plate
			case 77: // stone button
				event.setCancelled(!canEdit(event.getPlayer(),block,Interaction.STONEMACHINE));
				break;
			
			//Containers!
			case 23: // dispenser
			case 54: // chest
			case 61: // furnace
			case 62: // burning furnace
			case 84: // jukebox
			case 117: // brewing stand
				event.setCancelled(!canEdit(event.getPlayer(),block,Interaction.CONTAINER));
				break;
				
			//Anvil
			case 145:
				event.setCancelled(!canEdit(event.getPlayer(),block,Interaction.ANVIL));
				break;

			//TelePad
			case 49:
				event.setCancelled(!canEdit(event.getPlayer(),block,Interaction.TELEPAD));
				break;

			default: break;
		}
	}
	
	@EventHandler public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getBlockClicked().getRelative(event.getBlockFace()),Interaction.PLACE));
	}

	@EventHandler public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getEntity().getLocation().getBlock(),Interaction.SHEAR));
	}

	@EventHandler public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if(event.getPlayer().isOp()) { return; }
		event.setCancelled(!canEdit(event.getPlayer(),event.getBlockClicked().getRelative(event.getBlockFace()),Interaction.BREAK));
	}

	@EventHandler public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getPlayer() instanceof Player) {
			if(event.getPlayer().isOp()) { return; }
			event.setCancelled(!canEdit(event.getPlayer(),event.getBlock(),Interaction.IGNITE));
		} else {
			event.setCancelled(!canEdit("NoPlayerEvent",event.getBlock(),Interaction.FIRESPREAD));
		}
	}
	

	/* canEdit, the queen of Sea-Cows =======================================================================*/
	
	public boolean canEdit(Object player, Block block, Object flag) {
		return Utils.canEdit(player, block, flag);
	}
		
	/* Commands ==============================================================================================*/
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		String command = cmd.getName();

		if(command.equalsIgnoreCase("tr")){
			return Commands.reloadConfig(sender);
		}

		if(sender instanceof Player){
			Player player = (Player) sender;
			
			if(command.equalsIgnoreCase("whose")) { return Commands.whose(player); }
			
			if(args.length>0){
				
			}
			
			if(args.length>1){
				if(command.equalsIgnoreCase("setTotemDefault")) { return Commands.setTotemDefault(player, args); }

			}
			
			if(cmd.getName().equalsIgnoreCase("tfriend")){
				if(args.length<1) { return false; }
				this.getConfig().set("totems.groups."+player.getName().toLowerCase()+"."+args[0].toLowerCase(), true);
				this.saveConfig();
				sender.sendMessage(args[0]+" is now permitted to do stuff in your areas.");
				return true;
			}
			
			if(cmd.getName().equalsIgnoreCase("tunfriend")){
				if(args.length<1) { return false; }
				this.getConfig().set("totems.groups."+player.getName().toLowerCase()+"."+args[0].toLowerCase(), false);
				this.saveConfig();
				sender.sendMessage(args[0]+" is now unpermitted to do stuff in your areas.");
				return true;
			}			
		}
		return false;
	}
}