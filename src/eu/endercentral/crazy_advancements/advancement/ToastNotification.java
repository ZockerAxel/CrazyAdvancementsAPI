package eu.endercentral.crazy_advancements.advancement;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import eu.endercentral.crazy_advancements.JSONMessage;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame;
import eu.endercentral.crazy_advancements.packet.ToastAdvancementsPacket;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Represents a Toast Notification
 * 
 * @author Axel
 *
 */
public class ToastNotification {
	
	private final ItemStack icon;
	private final JSONMessage message;
	private final AdvancementFrame frame;
	private final Advancement toastAdvancement;
	
	/**
	 * Constructor for creating Toast Notifications
	 * 
	 * @param icon The displayed Icon
	 * @param message The displayed Message
	 * @param frame Determines the displayed Title and Sound Effect (evaluated client-side and modifiable via resource packs)
	 */
	public ToastNotification(ItemStack icon, JSONMessage message, AdvancementFrame frame) {
		this.icon = icon;
		this.message = message;
		this.frame = frame;
		
		AdvancementDisplay toastAdvancementDisplay = new AdvancementDisplay(icon, this.message, new JSONMessage(new TextComponent("Toast Notification")), frame, AdvancementVisibility.ALWAYS);
		this.toastAdvancement = new Advancement(new NameKey("eu.endercentral", "notification"), toastAdvancementDisplay, AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN);
	}
	
	/**
	 * Constructor for creating Toast Notifications
	 * 
	 * @param icon The displayed Icon
	 * @param message The displayed Message
	 * @param frame Determines the displayed Title and Sound Effect (evaluated client-side and modifiable via resource packs)
	 */
	public ToastNotification(ItemStack icon, String message, AdvancementFrame frame) {
		this.icon = icon;
		this.message = new JSONMessage(new TextComponent(message));
		this.frame = frame;
		
		AdvancementDisplay toastAdvancementDisplay = new AdvancementDisplay(icon, this.message, new JSONMessage(new TextComponent("Toast Notification")), frame, AdvancementVisibility.ALWAYS);
		this.toastAdvancement = new Advancement(new NameKey("eu.endercentral", "notification"), toastAdvancementDisplay, AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN);
	}
	
	/**
	 * Constructor for creating Toast Notifications
	 * 
	 * @param icon The displayed Icon
	 * @param message The displayed Message
	 * @param frame Determines the displayed Title and Sound Effect (evaluated client-side and modifiable via resource packs)
	 */
	public ToastNotification(Material icon, JSONMessage message, AdvancementFrame frame) {
		this.icon = new ItemStack(icon);
		this.message = message;
		this.frame = frame;
		
		AdvancementDisplay toastAdvancementDisplay = new AdvancementDisplay(icon, this.message, new JSONMessage(new TextComponent("Toast Notification")), frame, AdvancementVisibility.ALWAYS);
		this.toastAdvancement = new Advancement(new NameKey("eu.endercentral", "notification"), toastAdvancementDisplay, AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN);
	}
	
	/**
	 * Constructor for creating Toast Notifications
	 * 
	 * @param icon The displayed Icon
	 * @param message The displayed Message
	 * @param frame Determines the displayed Title and Sound Effect (evaluated client-side and modifiable via resource packs)
	 */
	public ToastNotification(Material icon, String message, AdvancementFrame frame) {
		this.icon = new ItemStack(icon);
		this.message = new JSONMessage(new TextComponent(message));
		this.frame = frame;
		
		AdvancementDisplay toastAdvancementDisplay = new AdvancementDisplay(icon, this.message, new JSONMessage(new TextComponent("Toast Notification")), frame, AdvancementVisibility.ALWAYS);
		this.toastAdvancement = new Advancement(new NameKey("eu.endercentral", "notification"), toastAdvancementDisplay, AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN);
	}
	
	/**
	 * Gets the Icon
	 * 
	 * @return The Icon
	 */
	public ItemStack getIcon() {
		return icon;
	}
	
	/**
	 * Gets the TItle
	 * 
	 * @return The Title
	 */
	public JSONMessage getMessage() {
		return message;
	}
	
	/**
	 * Gets the Frame
	 * 
	 * @return The Frame
	 */
	public AdvancementFrame getFrame() {
		return frame;
	}
	
	/**
	 * Sends this Toast Notification to a Player
	 * 
	 * @param player The target Player
	 */
	public void send(Player player) {
		toastAdvancement.getProgress(player).grant();
		ToastAdvancementsPacket addPacket = new ToastAdvancementsPacket(player, false, Arrays.asList(toastAdvancement), null);
		ToastAdvancementsPacket removePacket = new ToastAdvancementsPacket(player, false, null, Arrays.asList(toastAdvancement.getName()));
		
		addPacket.send();
		removePacket.send();
	}
	
}