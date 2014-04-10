package brain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Meal {
	
	private int mealId;
	private int totalCarbs;
	private int levelBefore;
	private int levelAfter;
	private int unitsTaken;
	private int unitsPredicted;
	private Date mealTime;
	private ArrayList<Record> foodRecords;
	
	public Meal(){
		mealId = -1;
		totalCarbs = -1;
		levelBefore = -1;
		levelAfter = -1;
		unitsTaken = -1;
		unitsPredicted = -1;
		mealTime = null;
		foodRecords =  new ArrayList<Record>();
	}
	
	public Meal(int mealId, int totalCarbs, int levelBefore, int levelAfter, int unitsTaken, int unitsPredicted, Date mealTime){
		this.mealId = mealId;
		this.totalCarbs = totalCarbs;
		this.levelBefore = levelBefore;
		this.levelAfter = levelAfter;
		this.unitsTaken = unitsTaken;
		this.unitsPredicted = unitsPredicted;
		this.mealTime = mealTime;
		foodRecords =  new ArrayList<Record>();
		
	}
	
	public void saveMeal(){
		DatabaseConnector.saveMeal(totalCarbs, levelBefore, unitsTaken, unitsPredicted);
		int mealId = DatabaseConnector.getUnfinishedMealId();
		for(Record f : foodRecords){
			f.saveRecord(mealId);
		}
	}
	
	public void finishMeal(){
		DatabaseConnector.finishMeal(levelAfter);
		//TODO: Update Predictor Algo with new foods
	}
	
	public int getTotalCarbs() {
		if(totalCarbs == -1 || (totalCarbs == 0 && !this.isRecordsEmpty())){
			totalCarbs = 0;
			for(Record f : foodRecords){
				totalCarbs += f.getCarbCount();
			}
		}
		return totalCarbs;
	}
	
	public static Meal getUnfinishedMeal(){
		int mealId = DatabaseConnector.getUnfinishedMealId();
		return DatabaseConnector.getMeal(mealId);
	}
	
	public static Meal[] getMealHistory(){
		return DatabaseConnector.getRecentMeals();
	}
	
	public int getMealId() {
		return mealId;
	}

	public int getLevelBefore() {
		return levelBefore;
	}

	public void setLevelBefore(int levelBefore) {
		this.levelBefore = levelBefore;
	}

	public int getLevelAfter() {
		return levelAfter;
	}

	public void setLevelAfter(int levelAfter) {
		this.levelAfter = levelAfter;
	}

	public int getUnitsTaken() {
		return unitsTaken;
	}

	public void setUnitsTaken(int unitsTaken) {
		this.unitsTaken = unitsTaken;
	}

	public int getUnitsPredicted() {
		return unitsPredicted;
	}

	public void setUnitsPredicted(int unitsPredicted) {
		this.unitsPredicted = unitsPredicted;
	}

	public Date getMealTime() {
		return mealTime;
	}

	public void setMealTime(Date mealTime) {
		this.mealTime = mealTime;
	}
	
	public void addRecord(Record f){
		foodRecords.add(f);
	}
	
	public Record[] getRecords(){
		return foodRecords.toArray(new Record[]{});
	}
	
	public boolean isRecordsEmpty(){
		return foodRecords.isEmpty();
	}
	
	public void sortRecords(){
		Collections.sort(foodRecords);
	}
}
