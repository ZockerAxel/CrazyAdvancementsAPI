package eu.endercentral.crazy_advancements.packet;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;

/**
 * Represents an Advancement Packet which respects Advancement Visibility
 * 
 * @author Axel
 *
 */
public class VisibilityAdvancementsPacket extends AdvancementsPacket {
	
	private static List<Advancement> stripInvisibleAdvancements(Player player, List<Advancement> advancements) {
		List<Advancement> strippedList = new ArrayList<>();
		
		for(Advancement advancement: advancements) {
			AdvancementDisplay display = advancement.getDisplay();
			
			boolean visible = display.isVisible(player, advancement);
			advancement.saveVisibilityStatus(player, visible);
			if(visible) {
			    advancements.add(advancement);
			}
		}
		
		return strippedList;
	}
	
	/**
	 * Constructor for creating Advancement Packets that respect Advancement Visiblity
	 * 
	 * @param player The target Player
	 * @param reset Whether the Client will clear the Advancement Screen before adding the Advancements
	 * @param advancements A list of advancements that should be added to the Advancement Screen
	 * @param removedAdvancements A list of NameKeys which should be removed from the Advancement Screen
	 */
	public VisibilityAdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NameKey> removedAdvancements) {
		super(player, reset, stripInvisibleAdvancements(player, advancements), removedAdvancements);
	}
	
}