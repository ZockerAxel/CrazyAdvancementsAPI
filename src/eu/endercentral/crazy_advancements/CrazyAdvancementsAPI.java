package eu.endercentral.crazy_advancements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame;
import eu.endercentral.crazy_advancements.advancement.AdvancementFlag;
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import eu.endercentral.crazy_advancements.advancement.criteria.CriteriaType;
import eu.endercentral.crazy_advancements.advancement.progress.GenericResult;
import eu.endercentral.crazy_advancements.advancement.progress.GrantCriteriaResult;
import eu.endercentral.crazy_advancements.advancement.serialized.SerializedAdvancement;
import eu.endercentral.crazy_advancements.advancement.serialized.SerializedAdvancementDisplay;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import eu.endercentral.crazy_advancements.packet.AdvancementsPacket;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionInstance;
import net.minecraft.advancements.critereon.LootSerializationContext;
import net.minecraft.network.protocol.game.PacketPlayOutSelectAdvancementTab;
import net.minecraft.resources.MinecraftKey;

/**
 * Represents the API's Plugin
 * 
 * @author Axel
 *
 */
public class CrazyAdvancementsAPI extends JavaPlugin implements Listener {
	
	private static final Gson gson;
	private static final List<String> SELECTORS = Arrays.asList("@a", "@p", "@s", "@r");
	private static CrazyAdvancementsAPI instance;
	
	static {
		gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}
	
	/**
	 * Criterion Instance for Internal Use
	 */
	public static final Criterion CRITERION = new Criterion(new CriterionInstance() {
		@Override
		public JsonObject a(LootSerializationContext arg0) {
			return null;
		}
		
		@Override
		public MinecraftKey a() {
			return new MinecraftKey("", "");
		}
	});
	
	private static AdvancementPacketReceiver packetReciever;
	private static HashMap<String, NameKey> activeTabs = new HashMap<>();
	
	private AdvancementManager fileAdvancementManager;
	
	@Override
	public void onLoad() {
		instance = this;
		fileAdvancementManager = new AdvancementManager(new NameKey("crazy_advancements", "file"));
		fileAdvancementManager.makeAccessible();
		loadFileAdvancements();
	}
	
	private void loadFileAdvancements() {
		File location = new File(getDataFolder().getAbsolutePath() + File.separator + "advancements" + File.separator);
		
		HashMap<NameKey, SerializedAdvancement> advancements = new HashMap<NameKey, SerializedAdvancement>();
		
		location.mkdirs();
		File[] files = location.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {
				String namespace = file.getName();
				advancements.putAll(loadNamespace(namespace, "", file));
			}
		}
		
		List<NameKey> missingAdvancements = new ArrayList<>(advancements.keySet());
		HashMap<NameKey, Advancement> createdAdvancements = new HashMap<NameKey, Advancement>();
		
		while(missingAdvancements.size() > 0) {
			Iterator<NameKey> missingIterator = missingAdvancements.iterator();
			int processedAdvancements = 0;
			
			while(missingIterator.hasNext()) {
				NameKey name = missingIterator.next();
				SerializedAdvancement serializedAdvancement = advancements.get(name);
				NameKey parent = serializedAdvancement.getParent();
				
				if(parent == null || createdAdvancements.containsKey(parent)) {
					SerializedAdvancementDisplay serializedAdvancementDisplay = serializedAdvancement.getDisplay();
					
					//Generate Display
					Material icon = Material.matchMaterial(serializedAdvancementDisplay.getIcon());
					JSONMessage title = new JSONMessage(serializedAdvancementDisplay.getTitle().deserialize());
					JSONMessage description = new JSONMessage(serializedAdvancementDisplay.getDescription().deserialize());
					AdvancementFrame frame = AdvancementFrame.parse(serializedAdvancementDisplay.getFrame());
					AdvancementVisibility visibility = AdvancementVisibility.parseVisibility(serializedAdvancementDisplay.getVisibility());
					
					AdvancementDisplay display = new AdvancementDisplay(icon, title, description, frame, visibility);
					
					if(serializedAdvancementDisplay.getBackgroundTexture() != null) {
						display.setBackgroundTexture(serializedAdvancementDisplay.getBackgroundTexture());
					}
					
					display.setX(serializedAdvancementDisplay.getX());
					display.setY(serializedAdvancementDisplay.getY());
					
					//Generate Advancement
					List<AdvancementFlag> flags = new ArrayList<>();
					for(String flagName : serializedAdvancement.getFlags()) {
						flags.add(AdvancementFlag.valueOf(flagName.toUpperCase(Locale.ROOT)));
					}
					
					Advancement advancement = new Advancement(parent == null ? null : createdAdvancements.get(parent), name, display, flags.toArray(AdvancementFlag[]::new));
					if(serializedAdvancement.getCriteria() != null) {
						advancement.setCriteria(serializedAdvancement.getCriteria().deserialize());
					}
					advancement.setReward(serializedAdvancement.getReward());
					
					//Register
					fileAdvancementManager.addAdvancement(advancement);
					missingIterator.remove();
					createdAdvancements.put(name, advancement);
					processedAdvancements++;
				}
			}
			
			//Abort adding Advancements if no advancements were able to be processed
			if(processedAdvancements == 0) {
				for(NameKey name : missingAdvancements) {
					System.err.println("Unable to load Advancement " + name + ": Parent does not exist");
				}
				break;
			}
		}
	}
	
	private HashMap<NameKey, SerializedAdvancement> loadNamespace(String namespace, String path, File location) {
		File[] files = location.listFiles();
		
		HashMap<NameKey, SerializedAdvancement> advancements = new HashMap<NameKey, SerializedAdvancement>();
		
		for(File file : files) {
			if(file.isDirectory()) {
				advancements.putAll(loadNamespace(namespace, path + file.getName() + "/", file));
			} else if(file.isFile() && file.getName().endsWith(".json")) {
				FileReader os = null;
				try {
					os = new FileReader(file);
					
					JsonElement element = JsonParser.parseReader(os);
					os.close();
					
					SerializedAdvancement advancement = gson.fromJson(element, SerializedAdvancement.class);
					
					String fileName = file.getName();
					String key = fileName.substring(0, fileName.length() - 5);//Remove .json
					advancements.put(new NameKey(namespace, path + key), advancement);
				} catch (Exception e) {
					if(os != null) {
						try {
							os.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					System.err.println("Unable to load Advancement from File " + namespace + "/" + file.getName() + ": " + e.getLocalizedMessage());
				}
			}
		}
		return advancements;
	}
	
	@Override
	public void onEnable() {
		//Init Packet Receiver
		packetReciever = new AdvancementPacketReceiver();
		
		for(Player player : Bukkit.getOnlinePlayers()) {
			packetReciever.initPlayer(player);
			fileAdvancementManager.loadProgress(player);
			fileAdvancementManager.addPlayer(player);
		}
		
		//Register Events
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			AdvancementsPacket packet = new AdvancementsPacket(player, true, null, null);
			packet.send();
		}
	}
	
	/**
	 * Gets the Instance
	 * 
	 * @return The Instance
	 */
	public static CrazyAdvancementsAPI getInstance() {
		return instance;
	}
	
	/**
	 * Gets the Gson Instance
	 * 
	 * @return The Gson Instance
	 */
	public static Gson getGson() {
		return gson;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		packetReciever.initPlayer(player);
		
		//Add Player to File Advancement Manager
		fileAdvancementManager.loadProgress(player);
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			
			@Override
			public void run() {
				fileAdvancementManager.addPlayer(player);
			}
		}, 2);
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent e) {
		packetReciever.close(e.getPlayer(), packetReciever.getHandlers().get(e.getPlayer().getName()));
	}
	
	/**
	 * Clears the active tab
	 * 
	 * @param player The player whose Tab should be cleared
	 */
	public static void clearActiveTab(Player player) {
		setActiveTab(player, null, true);
	}
	
	/**
	 * Sets the active tab
	 * 
	 * @param player The player whose Tab should be changed
	 * @param rootAdvancement The name of the tab to change to
	 */
	public static void setActiveTab(Player player, String rootAdvancement) {
		setActiveTab(player, new NameKey(rootAdvancement));
	}
	
	/**
	 * Sets the active tab
	 * 
	 * @param player The player whose Tab should be changed
	 * @param rootAdvancement The name of the tab to change to
	 */
	public static void setActiveTab(Player player, @Nullable NameKey rootAdvancement) {
		setActiveTab(player, rootAdvancement, true);
	}
	
	static void setActiveTab(Player player, NameKey rootAdvancement, boolean update) {
		if(update) {
			PacketPlayOutSelectAdvancementTab packet = new PacketPlayOutSelectAdvancementTab(rootAdvancement == null ? null : rootAdvancement.getMinecraftKey());
			((CraftPlayer)player).getHandle().b.a(packet);
		}
		activeTabs.put(player.getUniqueId().toString(), rootAdvancement);
	}
	
	/**
	 * Gets the active tab
	 * 
	 * @param player Player to check
	 * @return The active Tab
	 */
	public static NameKey getActiveTab(Player player) {
		return activeTabs.get(player.getUniqueId().toString());
	}
	
	private final String noPermission = "§cI'm sorry but you do not have permission to perform this command. Please contact the server administrator if you believe that this is in error.";
	private final String commandIncompatible = "§cThis Command is incompatible with your Arguments!";
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("showtoast")) {
			if(sender.hasPermission("crazyadvancements.command.*") || sender.hasPermission("crazyadvancements.command.showtoast")) {
				if(args.length >= 3) {
					try {
						List<Player> players = new ArrayList<>();
						for(Entity entity : Bukkit.selectEntities(sender, args[0])) {
							if(entity instanceof Player) {
								players.add((Player) entity);
							}
						}
						
						if(players.size() > 0) {
							Material mat = getMaterial(args[1]);
							
							if(mat != null && mat.isItem()) {
								String message = args[2];
								if(args.length > 3) {
									for(int i = 3; i < args.length; i++) {
										message += " " + args[i];
									}
								}
								
								for(Player player : players) {
									ToastNotification toast = new ToastNotification(mat, message, AdvancementFrame.TASK);
									toast.send(player);
								}
								
								sender.sendMessage(players.size() == 1 ? "§aSuccessfully displayed Toast to §b" + players.get(0).getName() + "§a!" : "§aSuccessfully displayed Toast to §e" + players.size() + "§aPlayers!");
							} else {
								sender.sendMessage("§c'" + args[1] + "' isn't a valid Item Material");
							}
						} else {
							sender.sendMessage(args[0].startsWith("@") ? "§cNo Player found for Selector §e" + args[0] + "§c" : "§cCan't find Player '§e" + args[0] + "§c'");
						}
					} catch(Exception ex) {
						ex.printStackTrace();
						sender.sendMessage(commandIncompatible);
					}
					
					
				} else {
					sender.sendMessage("§cUsage: §r" + cmd.getUsage());
				}
			} else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("grant") || cmd.getName().equalsIgnoreCase("revoke")) {
			boolean grant = cmd.getName().equalsIgnoreCase("grant");
			if(sender.hasPermission("crazyadvancements.command.*") || sender.hasPermission("crazyadvancements.command.grantrevoke")) {
				if(args.length >= 3) {
					try {
						List<Player> players = new ArrayList<>();
						for(Entity entity : Bukkit.selectEntities(sender, args[0])) {
							if(entity instanceof Player) {
								players.add((Player) entity);
							}
						}
						
						if(players.size() > 0) {
							AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
							
							if(manager != null) {
								for(Player player : players) {
									if(manager.getPlayers().contains(player)) {
										Advancement advancement = manager.getAdvancement(new NameKey(args[2]));
										
										if(advancement != null) {
											if(args.length >= 4) {
												
												String[] convertedCriteria = Arrays.copyOfRange(args, 3, args.length);
												
												boolean success = false;
												
												if(grant) {
													if(!advancement.isGranted(player)) {
														GrantCriteriaResult result = manager.grantCriteria(player, advancement, convertedCriteria);
														success = result == GrantCriteriaResult.CHANGED;
													}
												} else {
													GenericResult result = manager.revokeCriteria(player, advancement, convertedCriteria);
													success = result == GenericResult.CHANGED;
												}
												
												String criteriaString = "§c" + convertedCriteria[0];
												if(convertedCriteria.length > 1) {
													for(String criteria : Arrays.copyOfRange(convertedCriteria, 1, convertedCriteria.length - 1)) {
														criteriaString += "§a, §c" + criteria;
													}
													criteriaString += " §aand §c" + convertedCriteria[convertedCriteria.length - 1];
												}
												
												if(success) {
													if(fileAdvancementManager.equals(manager)) {
														fileAdvancementManager.saveProgress(player, advancement);
													}
													sender.sendMessage("§aSuccessfully " + (grant ? "granted" : "revoked") + " Criteria " + criteriaString + " §afor '§e" + advancement.getName() + "§a' " + (grant ? "to" : "from") + " §b" + player.getName());
												} else {
													sender.sendMessage("§cCriteria " + criteriaString + " §afor '§e" + advancement.getName() + "§c' " + (grant ? "is already granted to" : "is already not granted to") + " §b" + player.getName());
												}
												
											} else {
												boolean success = false;
												
												if(grant) {
													if(!advancement.isGranted(player)) {
														GenericResult result = manager.grantAdvancement(player, advancement);
														success = result == GenericResult.CHANGED;
													}
												} else {
													GenericResult result = manager.revokeAdvancement(player, advancement);
													success = result == GenericResult.CHANGED;
												}
												
												if(success) {
													if(fileAdvancementManager.equals(manager)) {
														fileAdvancementManager.saveProgress(player, advancement);
													}
													sender.sendMessage("§aSuccessfully " + (grant ? "granted" : "revoked") + " Advancement '§e" + advancement.getName() + "§a' " + (grant ? "to" : "from") + " §b" + player.getName());
												} else {
													sender.sendMessage("§cAdvancement '§e" + advancement.getName() + "§c' " + (grant ? "is already granted to" : "is already not granted to") + " §b" + player.getName());
												}
											}
											
										} else {
											sender.sendMessage("§cAdvancement with Name '§e" + args[2] + "§c' does not exist in '§e" + args[1] + "§c'");
										}
									} else {
										sender.sendMessage("§c'§e" + args[1] + "§c' does not contain Player '§e" + args[0] + "§c'");
									}
								}
							} else {
								sender.sendMessage("§cManager with Name '§e" + args[1] + "§c' does not exist");
							}
						} else {
							sender.sendMessage(args[0].startsWith("@") ? "§cNo Player found for Selector §e" + args[0] + "§c" : "§cCan't find Player '§e" + args[0] + "§c'");
						}
						
					} catch(Exception ex) {
						ex.printStackTrace();
						sender.sendMessage(commandIncompatible);
					}
					
				} else {
					sender.sendMessage("§cUsage: §r" + cmd.getUsage());
				}
			} else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("setprogress")) {
			if(sender.hasPermission("crazyadvancements.command.*") || sender.hasPermission("crazyadvancements.command.grantrevoke")) {
				if(args.length >= 3) {
					try {
						List<Player> players = new ArrayList<>();
						for(Entity entity : Bukkit.selectEntities(sender, args[0])) {
							if(entity instanceof Player) {
								players.add((Player) entity);
							}
						}
						
						if(players.size() > 0) {
							AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
							
							if(manager != null) {
								for(Player player : players) {
									if(manager.getPlayers().contains(player)) {
										Advancement advancement = manager.getAdvancement(new NameKey(args[2]));
										
										if(advancement != null) {
											if(args.length >= 4) {
												
												int progress = Integer.parseInt(args[3]);
												manager.setCriteriaProgress(player, advancement, progress);
												
												if(fileAdvancementManager.equals(manager)) {
													fileAdvancementManager.saveProgress(player, advancement);
												}
												
												sender.sendMessage("§aSuccessfully set Criteria Progress to " + progress + " §afor Advancement '§e" + advancement.getName() + "§a' for Player §b" + player.getName());
											}
											
										} else {
											sender.sendMessage("§cAdvancement with Name '§e" + args[2] + "§c' does not exist in '§e" + args[1] + "§c'");
										}
										
									} else {
										sender.sendMessage("§c'§e" + args[1] + "§c' does not contain Player '§e" + args[0] + "§c'");
									}
								}
							} else {
								sender.sendMessage("§cManager with Name '§e" + args[1] + "§c' does not exist");
							}
						} else {
							sender.sendMessage(args[0].startsWith("@") ? "§cNo Player found for Selector §e" + args[0] + "§c" : "§cCan't find Player '§e" + args[0] + "§c'");
						}
						
					} catch(Exception ex) {
						ex.printStackTrace();
						sender.sendMessage(commandIncompatible);
					}
					
				} else {
					sender.sendMessage("§cUsage: §r" + cmd.getUsage());
				}
			} else {
				sender.sendMessage(noPermission);
			}
			return true;
		}
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		ArrayList<String> tab = new ArrayList<>();
		
		if(cmd.getName().equalsIgnoreCase("showtoast")) {
			if(args.length == 1) {
				for(String selector : SELECTORS) {
					if(selector.toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(selector);
					}
				}
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(player.getName());
					}
				}
			} else if(args.length == 2) {
				for(Material mat : Material.values()) {
					if(mat.isItem() && mat.name().toLowerCase().startsWith(args[1].toLowerCase())) {
						tab.add(mat.name().toLowerCase());
					}
				}
			}
			
		}
		
		if(cmd.getName().equalsIgnoreCase("grant") || cmd.getName().equalsIgnoreCase("revoke")) {
			if(args.length == 1) {
				for(String selector : SELECTORS) {
					if(selector.toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(selector);
					}
				}
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(player.getName());
					}
				}
			} else if(args.length == 2) {
				for(AdvancementManager manager : AdvancementManager.getAccessibleManagers()) {
					if(manager.getName().toString().startsWith(args[1].toLowerCase())) {
						tab.add(manager.getName().toString());
					}
				}
			} else if(args.length == 3) {
				AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
				if(manager != null) {
					for(Advancement advancement : manager.getAdvancements()) {
						if(advancement.getName().toString().startsWith(args[2].toLowerCase()) || advancement.getName().getKey().startsWith(args[2].toLowerCase())) {
							tab.add(advancement.getName().toString());
						}
					}
				}
			} else if(args.length >= 4) {
				AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
				if(manager != null) {
					Advancement advancement = manager.getAdvancement(new NameKey(args[2]));
					if(advancement != null) {
						for(String criterion : advancement.getCriteria().getActionNames()) {
							if(criterion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
								tab.add(criterion);
							}
						}
					}
				}
			}
			
		}
		
		if(cmd.getName().equalsIgnoreCase("setprogress")) {
			if(args.length == 1) {
				for(String selector : SELECTORS) {
					if(selector.toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(selector);
					}
				}
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						tab.add(player.getName());
					}
				}
			} else if(args.length == 2) {
				for(AdvancementManager manager : AdvancementManager.getAccessibleManagers()) {
					if(manager.getName().toString().startsWith(args[1].toLowerCase())) {
						tab.add(manager.getName().toString());
					}
				}
			} else if(args.length == 3) {
				AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
				if(manager != null) {
					for(Advancement advancement : manager.getAdvancements()) {
						if((advancement.getName().toString().startsWith(args[2].toLowerCase()) || advancement.getName().getKey().startsWith(args[2].toLowerCase()))  && advancement.getCriteria().getType() == CriteriaType.NUMBER) {
							tab.add(advancement.getName().toString());
						}
					}
				}
			} else if(args.length == 4) {
				AdvancementManager manager = AdvancementManager.getAccessibleManager(new NameKey(args[1]));
				if(manager != null) {
					Advancement advancement = manager.getAdvancement(new NameKey(args[2]));
					if(advancement != null && advancement.getCriteria().getType() == CriteriaType.NUMBER) {
						tab.add(args[3]);
						tab.add("" + advancement.getCriteria().getRequiredNumber());
					}
				}
			}
		}
		
		return tab;
	}
	
	private Material getMaterial(String input) {
		for(Material mat : Material.values()) {
			if(mat.name().equalsIgnoreCase(input)) {
				return mat;
			}
		}
		return null;
	}
	
}