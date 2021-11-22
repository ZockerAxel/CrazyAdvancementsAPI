package eu.endercentral.crazy_advancements.save;

import eu.endercentral.crazy_advancements.NameKey;

public class ProgressData {
	
	private final NameKey name;
	private final int progress;
	
	public ProgressData(NameKey name, int progress) {
		this.name = name;
		this.progress = progress;
	}
	
	public NameKey getName() {
		return name;
	}
	
	public int getProgress() {
		return progress;
	}
	
}