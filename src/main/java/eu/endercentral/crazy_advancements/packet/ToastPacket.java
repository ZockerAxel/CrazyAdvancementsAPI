package eu.endercentral.crazy_advancements.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an Advancements Packet for Toast Notifications
 * 
 * @author Axel
 *
 */
public class ToastPacket {
	
	private final Player player;
	private final boolean add;
	private final ToastNotification notification;
	
	/**
	 * Constructor for creating Toast Packets
	 * 
	 * @param player The target Player
	 * @param add Whether to add or remove the Advancement
	 * @param notification The Notification
	 */
	public ToastPacket(Player player, boolean add, ToastNotification notification) {
		this.player = player;
		this.add = add;
		this.notification = notification;
	}
	
	/**
	 * Gets the target Player
	 * 
	 * @return The target Player
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets whether the Advancement is added or removed
	 * 
	 * @return Whether the Advancement is added or removed
	 */
	public boolean isAdd() {
		return add;
	}
	
	/**
	 * Gets the Notification
	 * 
	 * @return The Notification
	 */
	public ToastNotification getNotification() {
		return notification;
	}
	
	/**
	 * Builds a packet that can be sent to a Player
	 * 
	 * @return The Packet
	 */
	public ClientboundUpdateAdvancementsPacket build() {
		//Create Lists
		List<AdvancementHolder> advancements = new ArrayList<>();
		Set<ResourceLocation> removedAdvancements = new HashSet<>();
		Map<ResourceLocation, AdvancementProgress> progress = new HashMap<>();
		
		//Populate Lists
		if(add) {
			advancements.add(new AdvancementHolder(ToastNotification.NOTIFICATION_NAME.getMinecraftKey(), PacketConverter.toNmsToastAdvancement(getNotification())));
			progress.put(ToastNotification.NOTIFICATION_NAME.getMinecraftKey(), ToastNotification.NOTIFICATION_PROGRESS.getNmsProgress());
		} else {
			removedAdvancements.add(ToastNotification.NOTIFICATION_NAME.getMinecraftKey());
		}
		
		//Create Packet
		ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, advancements, removedAdvancements, progress);
		return packet;
	}
	
	/**
	 * Sends the Packet to the target Player
	 * 
	 */
	public void send() {
		ClientboundUpdateAdvancementsPacket packet = build();
		((CraftPlayer) getPlayer()).getHandle().connection.send(packet);
	}
	
	
	
	
}