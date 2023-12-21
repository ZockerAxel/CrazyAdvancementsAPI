package eu.endercentral.crazy_advancements.advancement;

import org.bukkit.entity.Player;

/**
 * Represents a Reward that is awarded upon Completion of an Advancement
 * 
 * @author Axel
 *
 */
public abstract class AdvancementReward {
	
	/**
	 * Gives the Reward
	 * 
	 * @param player The Receiver
	 */
	public abstract void onGrant(Player player);
	
}