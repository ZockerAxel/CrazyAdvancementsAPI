package eu.endercentral.crazy_advancements;

import java.util.Optional;
import java.util.stream.Stream;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

/**
 * Represents a Message in JSON Format
 * 
 * @author Axel
 *
 */
public class JSONMessage {
	
	private static final Provider COMPONENT_SERIALIZER_PROVIDER = new TextHolderLookupProvider();
	
	
	private final BaseComponent json;
	
	/**
	 * Constructor for creating a JSON Message
	 * 
	 * @param json A JSON representation of an ingame Message <a href="https://www.spigotmc.org/wiki/the-chat-component-api/">Read More</a>
	 */
	public JSONMessage(BaseComponent json) {
		this.json = json;
	}
	
	/**
	 * Gets the Message as a BaseComponent
	 * 
	 * @return the BaseComponent of an ingame Message
	 */
	public BaseComponent getJson() {
		return json;
	}
	
	/**
	 * Gets an NMS representation of an ingame Message
	 * 
	 * @return An {@link Component} representation of an ingame Message
	 */
	public Component getBaseComponent() {
		return Component.Serializer.fromJson(ComponentSerializer.toString(json), COMPONENT_SERIALIZER_PROVIDER);
	}
	
	@Override
	public String toString() {
		return json.toPlainText();
	}
	
	
	
	private static class TextHolderLookupProvider implements Provider {
		
		@Override
		public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
			return Stream.of();
		}
		
		@Override
		public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryRef) {
			return Optional.empty();
		}
		
	}
	
}