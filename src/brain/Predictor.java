package brain;

import brain.Meal.mealTypes;


public class Predictor {
	private enum sugarLevel { TOO_LOW, PERFECT, TOO_HIGH };
	private Meal meal;

	public Predictor(Meal meal){
		this.meal = meal;
	}

	public int getPrediction(){
		double carbsPerUnit = DatabaseConnector.getCarbsPerUnit();
		double totalCarbCount = 0;
		for(Record r: this.meal.getRecords()){
			Food f = DatabaseConnector.getFoodItem(r.getFoodId()); //return null if no item is found
			if(f != null){
				System.out.println("record but no input");
				//if we have a record but input no carb count
				totalCarbCount += Math.round(r.getAmount() * f.getCarbCount());
			}else if(f == null && r != null && r.getAmount() > 0){
				//no record but we input carbCount
				totalCarbCount += r.getCarbCount();
			}else{
				System.out.println("Prediction Failure");
				return 0;
			}
		}
		System.out.println("TotaCarbs: " + totalCarbCount);
		System.out.println("Carbs per Unit: " + carbsPerUnit);
		double mealCorrection = DatabaseConnector.getMealCorrection(meal.getMealType());
		System.out.println("Meal Correction: " + mealCorrection);
		return (int)Math.round((totalCarbCount) / (carbsPerUnit + mealCorrection) + Math.max(0, (meal.getLevelBefore() - 110) / DatabaseConnector.getSugarPerUnit()) );
	}

	public boolean updatePredictor(){
		if(meal.getLevelAfter() < 0 || meal.getLevelBefore() < 0 || meal.getRecords() == null){
			return false;
		}
		System.out.println("\nUpdating Predictor...");
		//static vars
		double carbsPerUnit = DatabaseConnector.getCarbsPerUnit();
		int carbsPerUnitScore = DatabaseConnector.getCarbsPerUnitScore();
		double sugarPerUnit = DatabaseConnector.getSugarPerUnit();
		int sugarPerUnitScore = DatabaseConnector.getSugarPerUnitScore();
		
		//Preprocess
		double initialExcessSugar = meal.getLevelBefore() - 110;
		int amtBolusUnits = (int)Math.round(initialExcessSugar / sugarPerUnit);
		if(initialExcessSugar < sugarPerUnit){
			sugarPerUnitScore = 0;
		}
		double excessInsulin = meal.getUnitsTaken() - meal.getUnitsPredicted();
		double excessSugar = meal.getLevelAfter() - 110;
		double excessCarbs = (excessSugar / sugarPerUnit + excessInsulin) * carbsPerUnit;
		int totalScore = 0;
		int numOfItems = 0;
		for(Record r : meal.getRecords()){
			totalScore += r.getFood().getScore();
			numOfItems++;
		}
		double totalDistributionScore = totalScore + carbsPerUnitScore + sugarPerUnitScore;
		double totalDistributionScoreFlipped = (totalDistributionScore - totalScore)/totalDistributionScore
				+ (totalDistributionScore - carbsPerUnitScore)/totalDistributionScore
				+ ((sugarPerUnitScore == 0) ? 0 : (totalDistributionScore - sugarPerUnitScore)/totalDistributionScore);
		
		double totalCarbScoreFlipped = 0;
		for(Record r : meal.getRecords()){
			totalCarbScoreFlipped += (totalScore - r.getFood().getScore()) / (double)totalScore;
		}
		System.out.println("totalDistributionScore: "+totalDistributionScore);
		System.out.println("totalDistributionScoreFlipped: "+totalDistributionScoreFlipped);
		System.out.println("totalCarbScoreFlipped: "+totalCarbScoreFlipped);
		System.out.println("Total Score: " + totalScore);
		System.out.println("Total Number of Items: " + numOfItems);
		System.out.println("Excess Carbs: " + excessCarbs);
		System.out.println("totalCarbs: " + meal.getTotalCarbs());
		
		//Update Data
		
		//Update Food Carbs
		double distributionPercent = (totalDistributionScore - totalScore)/totalDistributionScore/totalDistributionScoreFlipped
				+ Math.min(meal.getTotalCarbs(),  1500) / 1500
				- Math.min(amtBolusUnits, 20) / 20 / 2;
		distributionPercent = (distributionPercent > 1) ? 1 : distributionPercent;
		distributionPercent = (distributionPercent < 0) ? 0 : distributionPercent;
		System.out.println("disributionAmount FOOD: " + distributionPercent);
		double extraCarbsForFood = excessCarbs * distributionPercent;
		System.out.println("extraCarbsForFood: " + extraCarbsForFood);
		for(int i = 0; i < meal.getRecords().length; i++){
			Record record = meal.getRecords()[i];
			Food food = record.getFood();
			System.out.println("\nUpdating Food Item: " + record.getDesc() + " with score: " + food.getScore());

			double savedCarbCount = food.getCarbCount() * record.getAmount();
			double newCarbCount = record.getCarbCount(); //is 0 if there is none
			double newCarbScore = (mealSuccess() == sugarLevel.PERFECT) ? Math.max(5, Math.round(food.getScore() / 3)) : 1;

			double modifierPercent = getModifierPercent(totalCarbScoreFlipped, meal.getTotalCarbs(), (totalScore - food.getScore()) / (double)totalScore, ((newCarbCount > 0) ? newCarbCount : savedCarbCount));
			System.out.println("foodModifer: " + modifierPercent);
			double newCarbTemp = ((newCarbCount > 0) ? newCarbCount : savedCarbCount) + (extraCarbsForFood*modifierPercent);
			double averagedCarbCount = ((food.getScore() * savedCarbCount) + (newCarbScore*newCarbTemp)) / (double)(food.getScore() + newCarbScore) / record.getAmount();
			food.setCarbCount(averagedCarbCount); //TODO: Make sure record.getAmount() != 0
			System.out.println("Updated Carb Count: " + averagedCarbCount);
			
			//Update Score:
			if(mealSuccess() == sugarLevel.TOO_LOW && excessInsulin > 1){
				food.setScore(Math.max(1, food.getScore() + 1));
			}else if(mealSuccess() == sugarLevel.TOO_LOW && excessInsulin <= 1 && excessInsulin >= -1){
				food.setScore(Math.max(1, food.getScore() - 1));
			}else if(mealSuccess() == sugarLevel.TOO_LOW && excessInsulin < -1){
				food.setScore(Math.max(1, food.getScore() - 5));
			}else if(mealSuccess() == sugarLevel.PERFECT && excessInsulin > 1){
				food.setScore(Math.max(1, food.getScore() - 2));
			}else if(mealSuccess() == sugarLevel.PERFECT && excessInsulin <= 1 && excessInsulin >= -1){
				food.setScore(Math.max(1, food.getScore() + 6));
			}else if(mealSuccess() == sugarLevel.PERFECT && excessInsulin < -1){
				food.setScore(Math.max(1, food.getScore() - 2));
			}else if(mealSuccess() == sugarLevel.TOO_HIGH && excessInsulin > 1){
				food.setScore(Math.max(1, food.getScore() - 5));
			}else if(mealSuccess() == sugarLevel.TOO_HIGH && excessInsulin <= 1 && excessInsulin >= -1){
				food.setScore(Math.max(1, food.getScore() - 1));
			}else if(mealSuccess() == sugarLevel.TOO_HIGH && excessInsulin < -1){
				food.setScore(Math.max(1, food.getScore() + 1));
			}
			
			//Save the Updated values:
			food.updateFood();
		}
		System.out.println("Done with food\n\n");
		//Update units per carb
		distributionPercent = (totalDistributionScore - carbsPerUnitScore)/totalDistributionScore/totalDistributionScoreFlipped
				- Math.min(meal.getTotalCarbs(),  1500) / 1500 / ((sugarPerUnitScore == 0) ? 1 : 2)
				- Math.min(amtBolusUnits, 20) / 20 / 2;
		distributionPercent = (distributionPercent < 0) ? 0 : distributionPercent;
		System.out.println("disributionAmount CARBSPERUNIT: " + distributionPercent);
		double extraCarbsForCarbData = excessCarbs * distributionPercent;
		double newCarbsPerUnit;
		if(Math.abs(meal.getUnitsPredicted()) > 0){
			newCarbsPerUnit = carbsPerUnit - (extraCarbsForCarbData / Math.abs(meal.getUnitsPredicted()));
		}else{
			newCarbsPerUnit = carbsPerUnit;
		}
		double averagedCarbsPerUnit = Math.max(1, (carbsPerUnitScore * carbsPerUnit +  newCarbsPerUnit)/(double)(carbsPerUnitScore + 1));
		DatabaseConnector.updateCarbsPerUnit(averagedCarbsPerUnit);
		System.out.println("Updated CarbsPerUnit: " + averagedCarbsPerUnit);
		DatabaseConnector.updateCarbsPerUnitScore(carbsPerUnitScore + 1);
		
		//Update sugar per unit
		if(sugarPerUnitScore != 0){
			distributionPercent = (totalDistributionScore - sugarPerUnitScore)/totalDistributionScore/totalDistributionScoreFlipped
					- Math.min(meal.getTotalCarbs(),  1500) / 1500 / 2
					+ Math.min(amtBolusUnits, 20) / 20;
			distributionPercent = (distributionPercent > 1) ? 1 : distributionPercent;
			distributionPercent = (distributionPercent < 0) ? 0 : distributionPercent;
			System.out.println("disributionAmount SUGARSPERUNIT: " + distributionPercent);
			double extraCarbsForSugarData = excessCarbs * distributionPercent;
			double newSugarPerUnit = sugarPerUnit - (extraCarbsForSugarData / amtBolusUnits);//amtBolusUnits will always be > 0
			double averagedSugarPerUnit = Math.max(1, (sugarPerUnitScore * sugarPerUnitScore +  newSugarPerUnit * amtBolusUnits)/(double)(sugarPerUnitScore + amtBolusUnits));
			DatabaseConnector.updateSugarPerUnit(averagedSugarPerUnit);
			System.out.println("Updated SugarsPerUnit: " + averagedSugarPerUnit);
			DatabaseConnector.updateSugarPerUnitScore(sugarPerUnitScore + 1);
		}
		
		
		//Update Meal Corrections
		if(meal.getMealId() % 100 == 0){
			this.updateMealCorrections();
		}

		return true;
	}
	
	
	//Run this every x meals. (ie 100 meals)
	private void updateMealCorrections(){
		System.out.println("Starting");
		double breakfastCarbError = DatabaseConnector.getAverageCarbError(mealTypes.BREAKFAST);
		double lunchCarbError = DatabaseConnector.getAverageCarbError(mealTypes.LUNCH);
		double dinnerCarbError = DatabaseConnector.getAverageCarbError(mealTypes.DINNER);
		double mean = (breakfastCarbError + lunchCarbError + dinnerCarbError) / 3;
		double carbsPerUnit = DatabaseConnector.getCarbsPerUnit();
		System.out.println("Mean  " + mean);
		
		double temp = mean - breakfastCarbError;
		double newBreakfastCarbError = (Math.abs(temp) > carbsPerUnit) ? temp : 0.0;
		double newBreakfastCarbCorrection = (DatabaseConnector.getMealCorrection(mealTypes.BREAKFAST) + newBreakfastCarbError) / 2;
		DatabaseConnector.updateMealCorrection(mealTypes.BREAKFAST, newBreakfastCarbCorrection);
		System.out.println("newBreakfastCarbError  " + newBreakfastCarbError);
		System.out.println("newBreakfastCarbCorrection  " + newBreakfastCarbCorrection);
		
		temp = mean - lunchCarbError;
		double newLunchCarbError = ((Math.abs(temp) > carbsPerUnit)) ? temp : 0.0;
		newLunchCarbError = newLunchCarbError / meal.getUnitsTaken();
		double newLunchCarbCorrection = (DatabaseConnector.getMealCorrection(mealTypes.LUNCH) + newLunchCarbError) / 2;
		DatabaseConnector.updateMealCorrection(mealTypes.LUNCH, newLunchCarbCorrection);
		System.out.println("newLunchCarbError  " + newLunchCarbError);
		System.out.println("newLunchCarbCorrection  " + newLunchCarbCorrection);
		
		temp = mean - dinnerCarbError;
		double newDinnerCarbError = ((Math.abs(temp) > carbsPerUnit)) ? temp : 0.0;
		double newDinnerCarbCorrection = (DatabaseConnector.getMealCorrection(mealTypes.DINNER) + newDinnerCarbError) / 2;
		DatabaseConnector.updateMealCorrection(mealTypes.DINNER, newDinnerCarbCorrection);
		System.out.println("newDinnerCarbError  " + newDinnerCarbError);
		System.out.println("newDinnerCarbCorrection  " + newDinnerCarbCorrection);
	}

	private double getModifierPercent(double totalScore, double totalCarbs, double score, double carbs){
		System.out.println("in getModifierPercent");
		System.out.println(totalScore);
		System.out.println(totalCarbs);
		System.out.println(score);
		System.out.println(carbs);
		if(totalScore == score){
			return 1;
		}
		double scorePart = score / totalScore;
		double carbPart = carbs / totalCarbs;
		return (2*scorePart + 1*carbPart)/3;
	}

	private sugarLevel mealSuccess(){
		if(meal.getLevelAfter() >= 90 && meal.getLevelAfter() <= 130){
			return sugarLevel.PERFECT;
		}else if( meal.getLevelAfter() < 90){
			return sugarLevel.TOO_LOW;
		}else{
			return sugarLevel.TOO_HIGH;
		}
	}

	/*public static void main(String[] args){
		Predictor p = new Predictor(new Meal());
		System.out.println(p.getModifierPercent(2, 100, 1, 100));
		System.out.println(p.getModifierPercent(2, 100, 1, 0));
		//System.out.println(p.getModifierPercent(110, 60, 7, 10));
	}*/

}
