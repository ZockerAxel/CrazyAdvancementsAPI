package eu.endercentral.crazy_advancements.save;

import eu.endercentral.crazy_advancements.NameKey;

public class CriteriaData {
	
	private final NameKey name;
	private final Iterable<String> criteria;
	
	public CriteriaData(NameKey name, Iterable<String> criteria) {
		this.name = name;
		this.criteria = criteria;
	}
	
	public NameKey getName() {
		return name;
	}
	
	public Iterable<String> getCriteria() {
		return criteria;
	}
	
}