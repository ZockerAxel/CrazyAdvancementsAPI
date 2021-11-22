package eu.endercentral.crazy_advancements.advancement.progress;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;

public class AdvancementProgress {
	
	private HashSet<String> awardedCriteria = new HashSet<>();
	private net.minecraft.advancements.AdvancementProgress nmsProgress = new net.minecraft.advancements.AdvancementProgress();
	private long lastUpdate = -1;
	
	public AdvancementProgress(Map<String, Criterion> criteria, String[][] requirements) {
		nmsProgress.a(criteria, requirements);
	}
	
	/**
	 * Grants the Advancement, does not update for the player
	 * 
	 * @return The result of this oepration
	 */
	public GenericResult grant() {
		GenericResult result = GenericResult.UNCHANGED;
		
		Iterable<String> missing = getNmsProgress().getRemainingCriteria();
		Iterator<String> missingIterator = missing.iterator();
		
		while(missingIterator.hasNext()) {
			String next = missingIterator.next();
			CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(next);
			setGranted(criterionProgress);
			result = GenericResult.CHANGED;
			setLastUpdate();
		}
		
		return result;
	}
	
	/**
	 * Revokes the Advancemnt, does not update for the player
	 * 
	 * @return The result of this operation
	 */
	public GenericResult revoke() {
		GenericResult result = GenericResult.UNCHANGED;
		
		Iterable<String> awarded = getNmsProgress().getAwardedCriteria();
		Iterator<String> awardedIterator = awarded.iterator();
		long current = StreamSupport.stream(awarded.spliterator(), false).count();
		
		while(current > 0 && awardedIterator.hasNext()) {
			String next = awardedIterator.next();
			CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(next);
			setUngranted(criterionProgress);
			current--;
			result = GenericResult.CHANGED;
			setLastUpdate();
		}
		
		return result;
	}
	
	/**
	 * Grants Criteria, does not update for the player
	 * 
	 * @param criteria The Criteria to grant
	 * @return The result of this operation
	 */
	public GrantCriteriaResult grantCriteria(String... criteria) {
		GrantCriteriaResult result = GrantCriteriaResult.UNCHANGED;
		boolean doneBefore = getNmsProgress().isDone();
		
		if(!doneBefore) {//Only grant criteria if the advancement is not already granted
			for(String criterion : criteria) {
				if(!awardedCriteria.contains(criterion)) {
					CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(criterion);
					if(criterionProgress != null) {
						setGranted(criterionProgress);
						awardedCriteria.add(criterion);
						result = GrantCriteriaResult.CHANGED;
						setLastUpdate();
					}
				}
			}
			
			if(getNmsProgress().isDone()) {
				return GrantCriteriaResult.COMPLETED;
			}
		}
		return result;
	}
	
	/**
	 * Revokes Criteria, does not update for the player
	 * 
	 * @param criteria The Criteria to revoke
	 * @return The result of this operation
	 */
	public GenericResult revokeCriteria(String... criteria) {
		GenericResult result = GenericResult.UNCHANGED;
		
		for(String criterion : criteria) {
			if(awardedCriteria.contains(criterion)) {
				CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(criterion);
				if(criterionProgress != null) {
					setUngranted(criterionProgress);
					awardedCriteria.remove(criterion);
					result = GenericResult.CHANGED;
					setLastUpdate();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Sets Criteria, does not update for the player
	 * 
	 * @param number The Criteria to set
	 * @return The result of this operation
	 */
	public SetCriteriaResult setCriteriaProgress(int number) {
		SetCriteriaResult result = SetCriteriaResult.UNCHANGED;
		boolean doneBefore = getNmsProgress().isDone();
		
		Iterable<String> awarded = getNmsProgress().getAwardedCriteria();
		Iterator<String> awardedIterator = awarded.iterator();
		long current = StreamSupport.stream(awarded.spliterator(), false).count();
		
		Iterable<String> missing = getNmsProgress().getRemainingCriteria();
		Iterator<String> missingIterator = missing.iterator();
		
		while(current < number && missingIterator.hasNext()) {
			String next = missingIterator.next();
			CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(next);
			setGranted(criterionProgress);
			current++;
			result = SetCriteriaResult.CHANGED;
			setLastUpdate();
		}
		
		while(current > number && awardedIterator.hasNext()) {
			String next = awardedIterator.next();
			CriterionProgress criterionProgress = getNmsProgress().getCriterionProgress(next);
			setUngranted(criterionProgress);
			current--;
			result = SetCriteriaResult.CHANGED;
			setLastUpdate();
		}
		
		if(!doneBefore && getNmsProgress().isDone()) {
			result = SetCriteriaResult.COMPLETED;
		}
		
		return result;
	}
	
	private void setGranted(CriterionProgress criterionProgress) {
		criterionProgress.b();
	}
	
	private void setUngranted(CriterionProgress criterionProgress) {
		criterionProgress.c();
	}
	
	/**
	 * Gets a list of awarded Criteria
	 * 
	 * @return The list of awarded Criteria Names
	 */
	public HashSet<String> getAwardedCriteria() {
		return new HashSet<>(awardedCriteria);
	}
	
	/**
	 * Gets the nms progress instance
	 * 
	 * @return The nms progress instance
	 */
	public net.minecraft.advancements.AdvancementProgress getNmsProgress() {
		return nmsProgress;
	}
	
	/**
	 * Gets the timestamp for the last update or -1 if it has not been updated yet
	 * 
	 * @return The timestamp in milliseconds or -1 if it has not been updated yet
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	/**
	 * Sets the timestamp for the last update to the current system time
	 * 
	 */
	public void setLastUpdate() {
		lastUpdate = System.currentTimeMillis();
	}
	
}