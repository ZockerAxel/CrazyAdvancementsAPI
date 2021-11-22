package eu.endercentral.crazy_advancements.advancement;

public enum AdvancementFlag {
	
	SHOW_TOAST,
	DISPLAY_MESSAGE,
	SEND_WITH_HIDDEN_BOOLEAN,
	
	;
	
	public static final AdvancementFlag[] TOAST_AND_MESSAGE = new AdvancementFlag[] {SHOW_TOAST, DISPLAY_MESSAGE};
	
}