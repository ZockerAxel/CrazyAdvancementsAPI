package eu.endercentral.crazy_advancements.save;

import java.util.Set;

import eu.endercentral.crazy_advancements.NameKey;

public class CriteriaData {
	
	private final NameKey name;
	private final Set<String> criteria;
	
	public CriteriaData(NameKey name, Set<String> criteria) {
		this.name = name;
		this.criteria = criteria;
	}
	
	public NameKey getName() {
		return name;
	}
	
	public Set<String> getCriteria() {
		return criteria;
	}
	
}