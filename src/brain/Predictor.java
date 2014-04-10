package brain;


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
			if(f == null && r.getCarbCount() <= 0){
				//if no prediction can be made because we dont know the carbs count and we dont have a record
				return 999;
			}else if(f != null && r.getAmount() >= 0 && r.getCarbCount() == 0){
				System.out.println("record but no input");
				//if we have a record but input no carb count
				totalCarbCount += Math.round(r.getAmount() * f.getCarbCount());
			}else if(f == null && r.getCarbCount() > 0){
				//no record but we input carbCount
				totalCarbCount += r.getCarbCount();
			}else if(f != null && r.getCarbCount() > 0){
				//we both have a record and a new carb count
				totalCarbCount += (r.getCarbCount() + Math.round(r.getAmount() * f.getCarbCount())) / 2;
			}else{
				return 999;
			}
		}
		return (int)Math.round(totalCarbCount / carbsPerUnit + (meal.getLevelBefore() - 110) / DatabaseConnector.getSugarPerUnit() );
	}

	public boolean updatePredictor(){
		if(meal.getLevelAfter() < 0 || meal.getLevelBefore() < 0 || meal.getTotalCarbs() < 0 || meal.getRecords() == null){
			return false;
		}
		System.out.println("Updating Predictor...");
		//static vars
		double carbsPerUnit = DatabaseConnector.getCarbsPerUnit();
		int carbsPerUnitScore = DatabaseConnector.getCarbsPerUnitScore();
		double sugarPerUnit = DatabaseConnector.getSugarPerUnit();
		int sugarPerUnitScore = DatabaseConnector.getSugarPerUnitScore();
		
		//Preprocess
		double initialExcessSugar = meal.getLevelBefore() - 110;
		if(initialExcessSugar < sugarPerUnit){
			sugarPerUnitScore = 0;
		}
		double excessInsulin = meal.getUnitsTaken() - meal.getUnitsPredicted();
		double excessSugar = meal.getLevelAfter() - 110;
		double excessCarbs = (excessSugar / sugarPerUnit + excessInsulin) * carbsPerUnit;
		meal.sortRecords();
		int totalScore = 0;
		int numOfItems = 0;
		for(Record r : meal.getRecords()){
			totalScore += r.getFood().getScore();
			numOfItems++;
		}
		double totalDistributionScore = totalScore + carbsPerUnitScore + sugarPerUnitScore;
		double totalDistributionScoreFlipped = (totalDistributionScore - totalScore)/totalDistributionScore
				+ (totalDistributionScore - carbsPerUnitScore)/totalDistributionScore
				+ (totalDistributionScore - sugarPerUnitScore)/totalDistributionScore;
		
		double totalScoreFlipped = 0;
		for(Record r : meal.getRecords()){
			totalScoreFlipped += (totalScore - r.getFood().getScore()) / (double)totalScore;
		}
		System.out.println("Total Score: " + totalScore);
		System.out.println("Total Number of Items: " + numOfItems);
		System.out.println("Excess Carbs: " + excessCarbs);
		
		
		//Update Data
		
		//Update Food Carbs
		double extraCarbsForFood = excessCarbs * (totalDistributionScore - totalScore)/totalDistributionScore/totalDistributionScoreFlipped;
		System.out.println("extraCarbsForFood: " + extraCarbsForFood);
		for(int i = 0; i < meal.getRecords().length; i++){
			Record record = meal.getRecords()[i];
			Food food = record.getFood();
			System.out.println("Updating Food Item: " + record.getDesc());

			double savedCarbCount = food.getCarbCount() * record.getAmount();
			double newCarbCount = record.getCarbCount(); //is 0 if there is none
			double newCarbScore = (mealSuccess() == sugarLevel.PERFECT) ? 10 : 1;

			double modifierPercent = getModifierPercent(totalScoreFlipped, meal.getTotalCarbs(), (totalScore - record.getFood().getScore()) / totalScore, record.getCarbCount());
			System.out.println(modifierPercent);
			double newCarbTemp = ((newCarbCount > 0) ? newCarbCount : savedCarbCount) + extraCarbsForFood*modifierPercent;
			System.out.println(newCarbTemp);
			food.setCarbCount(((food.getScore() * savedCarbCount) + (newCarbScore*newCarbTemp)) / (double)(food.getScore() + newCarbScore*1) / record.getAmount());
			System.out.println(((food.getScore() * savedCarbCount) + (newCarbScore*newCarbTemp)) / (double)(food.getScore() + newCarbScore*1) / record.getAmount());
			
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

		//Update units per carb
		double extraCarbsForCarbData = excessCarbs * (totalDistributionScore - carbsPerUnitScore)/totalDistributionScore/totalDistributionScoreFlipped;
		double newCarbsPerUnit = carbsPerUnit - extraCarbsForCarbData;
		DatabaseConnector.updateCarbsPerUnit(Math.max(1, (carbsPerUnitScore * carbsPerUnit +  newCarbsPerUnit)/(double)(carbsPerUnitScore + 1)));
		DatabaseConnector.updateCarbsPerUnitScore(carbsPerUnitScore + 1);
		
		//Update sugar per unit
		if(sugarPerUnitScore != 0){
			double extraCarbsForSugarData = excessCarbs * (totalDistributionScore - sugarPerUnitScore)/totalDistributionScore/totalDistributionScoreFlipped;
			double newSugarPerUnit = sugarPerUnit - extraCarbsForSugarData;
			DatabaseConnector.updateSugarPerUnit(Math.max(1, (sugarPerUnitScore * sugarPerUnitScore +  newSugarPerUnit)/(double)(sugarPerUnitScore + 1)));
			DatabaseConnector.updateSugarPerUnitScore(sugarPerUnitScore + 1);
		}

		return true;
	}

	private double getModifierPercent(double totalScore, double totalCarbs, int score, double carbs){
		if(totalScore == score){
			return 1;
		}
		double scorePart = score / totalScore;
		scorePart = (scorePart == 0) ? 1 : scorePart;
		double carbPart = carbs / totalCarbs;
		return (1*scorePart + 1*carbPart)/2;


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
