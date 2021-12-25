package eu.endercentral.crazy_advancements.advancement;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_18_R1.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;

/**
 * Represents a Reward in form of a .mcfunction function
 * 
 * @author Axel
 *
 */
public class AdvancementFunctionReward extends AdvancementReward {
	
	private final String name;
	private final int delay;
	
	/**
	 * Constructor for creating a Reward that is given through a function
	 * 
	 * @param function The function name
	 * @param delay The delay in ticks before the function is executed
	 */
	public AdvancementFunctionReward(String function, int delay) {
		this.name = function;
		this.delay = delay;
	}
	
	/**
	 * Gets the Name of the function
	 * 
	 * @return The Name of the function
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the delay in ticks before the function is executed
	 * 
	 * @return The delay in ticks
	 */
	public int getDelay() {
		return delay;
	}
	
	@Override
	public final void onGrant(Player player) {
		OpSender sender = new OpSender(player);
		String command = (delay > 0 ? "schedule function " + getName() + " " + getDelay() + " append" : "function " + getName());
		Bukkit.dispatchCommand(sender, command);
	}
	
	
	private static class OpSender extends ProxiedNativeCommandSender {

		private static final CommandSender CONSOLE = Bukkit.getConsoleSender();
		
		public OpSender(Player callee) {
			super(VanillaCommandWrapper.getListener(CONSOLE), CONSOLE, callee);
		}
		
		
	}
	
}