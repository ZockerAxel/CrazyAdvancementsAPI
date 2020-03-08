package eu.endercentral.crazy_advancements;

import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;

public class JSONMessage {
	
	private final String json;
	
	/**
	 * 
	 * @param json A JSON representation of an ingame Message {@link <a href="https://github.com/skylinerw/guides/blob/master/java/text%20component.md">Read More</a>}
	 */
	public JSONMessage(String json) {
		this.json = json;
	}
	
	/**
	 * 
	 * @return the JSON representation of an ingame Message
	 */
	public String getJson() {
		return json;
	}
	
	/**
	 * 
	 * @return An {@link IChatBaseComponent} representation of an ingame Message
	 */
	public IChatBaseComponent getBaseComponent() {
		return ChatSerializer.a(json);
	}
	
	@Override
	public String toString() {
		return json;
	}
	
}