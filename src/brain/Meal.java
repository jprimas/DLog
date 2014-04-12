package brain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.joda.time.DateTime;

public class Meal {
	
	public enum mealTypes {SNACK, BREAKFAST, LUNCH, AFTERNOON_SNACK, DINNER, BEDTIME_SNACK, NIGHT_SNACK};
	private int mealId;
	private int totalCarbs;
	private int levelBefore;
	private int levelAfter;
	private int unitsTaken;
	private int unitsPredicted;
	private DateTime mealTime;
	private mealTypes mealType;
	private ArrayList<Record> foodRecords;
	
	public Meal(){
		mealId = -1;
		totalCarbs = -1;
		levelBefore = -1;
		levelAfter = -1;
		unitsTaken = -1;
		unitsPredicted = -1;
		mealTime = new DateTime();
		mealType = this.createMealType();
		foodRecords =  new ArrayList<Record>();
	}
	
	public Meal(int mealId, int totalCarbs, int levelBefore, int levelAfter, int unitsTaken, int unitsPredicted, Date mealTime, int type){
		this.mealId = mealId;
		this.totalCarbs = totalCarbs;
		this.levelBefore = levelBefore;
		this.levelAfter = levelAfter;
		this.unitsTaken = unitsTaken;
		this.unitsPredicted = unitsPredicted;
		this.mealTime = new DateTime(mealTime);
		this.mealType = mealTypes.values()[type];
		foodRecords =  new ArrayList<Record>();
		
	}
	
	public void saveMeal(){
		getTotalCarbs();
		DatabaseConnector.saveMeal(totalCarbs, levelBefore, unitsTaken, unitsPredicted, mealType);
		int mealId = DatabaseConnector.getUnfinishedMealId();
		for(Record f : foodRecords){
			f.saveRecord(mealId);
		}
	}
	
	public void finishMeal(){
		DatabaseConnector.finishMeal(levelAfter);
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
	
	private mealTypes createMealType(){
		if(mealTime == null){
			return mealTypes.SNACK;
		}
		
		int hours = mealTime.getHourOfDay();
		if(hours >= 5 && hours < 10){
			return mealTypes.BREAKFAST;
		}else if(hours >= 10 && hours < 14){
			return mealTypes.LUNCH;
		}else if(hours >= 14 && hours < 17){
			return mealTypes.LUNCH;
		}else if(hours >= 17 && hours < 19){
			return mealTypes.DINNER;
		}else if(hours >= 19 && hours <= 24){
			return mealTypes.BEDTIME_SNACK;
		}else if(hours < 5){
			return mealTypes.NIGHT_SNACK;
		}else{
			return mealTypes.SNACK;
		}
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

	public DateTime getMealTime() {
		return mealTime;
	}

	public void setMealTime(DateTime mealTime) {
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

	public mealTypes getMealType() {
		return mealType;
	}

	public void setMealType(mealTypes mealType) {
		this.mealType = mealType;
	}
}
