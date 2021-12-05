package eu.endercentral.crazy_advancements.packet;

import java.util.List;

import org.bukkit.entity.Player;

import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.Advancement;

/**
 * Represents an Advancements Packet for Toast Notifications
 * 
 * @author Axel
 *
 */
public class ToastAdvancementsPacket extends AdvancementsPacket {
	
	/**
	 * Constructor for creating Toast Advancement Packets
	 * 
	 * @param player The target Player
	 * @param reset Whether the Client will clear the Advancement Screen before adding the Advancements
	 * @param advancements A list of advancements that should be added to the Advancement Screen
	 * @param removedAdvancements A list of NameKeys which should be removed from the Advancement Screen
	 */
	public ToastAdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NameKey> removedAdvancements) {
		super(player, reset, advancements, removedAdvancements);
	}
	
	@Override
	protected net.minecraft.advancements.Advancement convertAdvancement(Advancement advancement) {
		return PacketConverter.toNmsToastAdvancement(advancement);
	}
	
	
}