package logger;

import java.util.Scanner;

import brain.Food;
import brain.Meal;
import brain.Predictor;
import brain.Record;

public class Logger {
	
	Scanner scanner;
	private final String delimiter = "***********************************************";
	
	public Logger(){
		scanner = new Scanner( System.in );
	}
	
	private void startMeal(){
		Meal meal = new Meal();
		System.out.println("\n\n\nLogging a New Meal");
		System.out.println(delimiter);
		
		//Get level before:
		String levelBeforeStr = null;
		while(!isInsulinLevel(levelBeforeStr)){
			System.out.print( "Enter Current Sugar Level: " );
		    levelBeforeStr = scanner.nextLine();
		}
		meal.setLevelBefore(Integer.parseInt(levelBeforeStr));
		
		//Have user input all the carbs about to be eaten
		System.out.println( "\nEnter Meal Specifics: (Type 0 to stop)\n" );
		int count = 1;
		while(true){
			System.out.print(count +". Enter Short Food Description: " );
			String desc = scanner.nextLine();
			if(desc.equals("0")){
				if(meal.isRecordsEmpty()){
					System.out.println("**Are you eating nothing?!**");
					continue;
				}else{
					break;
				}
			}
			if(desc != null && !desc.equals("")){
				Record record = new Record(desc, 0, -1);
				if(!record.verifyRecord()){
					continue;
				}
				//TODO: Check if existing food
				Food[] foods = record.getSimilarFoods();
				int foodId= -1;
				boolean updateCarbCount = false;
				if(foods != null && foods.length > 0){
					int innerCount = 1;
					System.out.println("\tDoes this food match any of the following?");
					for(Food f : foods){
						System.out.print("\t\t" + innerCount + ". ");
						System.out.println(f.foodName());
						innerCount++;
					}
					System.out.println("\t\t" + innerCount + ". Nope. Create New Entry");
					while(foodId == -1){
						System.out.print("\tWhich one did you mean:");
						String foodIdStr = scanner.nextLine();
						if(isNumber(foodIdStr)){
							int temp = Integer.parseInt(foodIdStr);
							if(temp < 0 || temp > foods.length+1){
								continue;
							}else{
								if(temp == foods.length+1){
									foodId = 0;
									updateCarbCount = true;
								}else{
									foodId = foods[temp-1].getId();
									record.setCarbCount(foods[temp-1].getCarbCount() * record.getAmount());
								}
							}
						}
					}
				}
				if(foodId == -1 || updateCarbCount){
					foodId = 0;
					String carbCountStr = "";
					while(!isCarbCount(carbCountStr)){
						System.out.print(count + ". Enter Carb Count Prediction: " );
						carbCountStr = scanner.nextLine();
					}
					record.setCarbCount(Integer.parseInt(carbCountStr));
				}
				record.setFoodId(foodId);
				meal.addRecord(record);
				count++;
				System.out.println("");
			}else{
				//TODO: More specific error handling
				System.out.println("**Error with Input. Try Again.**");
			}
		}
		
		//Create/Display/Save insulin prediction
		System.out.println("\nMy Prediction");
		System.out.println(delimiter);
		Predictor predictor = new Predictor(meal);
		int unitPrediction = predictor.getPrediction();
		System.out.println("I believe you should take " + unitPrediction + " units.");
		meal.setUnitsPredicted(unitPrediction);
		
		//Get actual units taken:
		String unitsTakenStr = null;
		while(!isNumber(unitsTakenStr)){
			System.out.print( "Actual units taken: " );
			unitsTakenStr = scanner.nextLine();
		}
		meal.setUnitsTaken(Integer.parseInt(unitsTakenStr));
		
		//Save the meal, so you can eat
		meal.saveMeal();
		System.out.println("\n**ENJOY YOUR MEAL**");
	}
	
	private void finishMeal(){
		//Get level after:
		System.out.println("\n\nFinish Logging Meal");
		System.out.println(delimiter);
		Meal meal = Meal.getUnfinishedMeal();
		Predictor predictor = new Predictor(meal);
		String levelAfterStr = null;
		while(!isInsulinLevel(levelAfterStr)){
			System.out.print( "Insulin Level 1-2 Hours After Meal: " );
			levelAfterStr = scanner.nextLine();
		}
		meal.setLevelAfter(Integer.parseInt(levelAfterStr));
		meal.finishMeal();
		if(predictor.updatePredictor()){
			System.out.println("**Predictor Udpated**");
		}
		
		this.showMenu();
	}
	
	private void showMenu(){
		System.out.println("\n\nD-Logger Menu ");
		System.out.println(delimiter);
		System.out.println("     1. Enter New Meal");
		System.out.println("     2. View Past Results");
		System.out.println("     3. Lookup Food Item (Not working)");
		System.out.println("     4. Exit");
		String optionStr = null;
		while(!isNumber(optionStr)){
			System.out.print( "Select an Option: " );
			optionStr = scanner.nextLine();
		}
		int option = Integer.parseInt(optionStr);
		switch (option){
			case 1: this.startMeal();
					break;
			case 2: this.showHistory();
					break;
			case 3: System.out.println("Option not working yet! Try another.");
					this.showMenu();
					break;
			case 4: System.out.println("\nBye, Bye!");
					System.exit(0);
			case 5: testPredictor();
					
			default: System.out.println("Choose a valid option!");
					this.showMenu();
		}
		
	}
	
	private void showHistory(){
		String rowDelimiter = "-------------------------------------------------" +
				"----------------------------------------------------";
		System.out.println("\n\n\nMeal History ");
		System.out.println(delimiter);
		Meal[] meals= Meal.getMealHistory();
		if(meals != null && meals.length > 0){
			System.out.println("Date\t\tSug Before\tSug After\tCarb Count\tUnits Predicted\t\tUnits Taken");
			System.out.println(rowDelimiter);
			for(Meal meal : meals){
				System.out.print(meal.getMealTime() + "\t");
				System.out.print(meal.getLevelBefore() + "\t\t");
				System.out.print(meal.getLevelAfter() + "\t\t");
				System.out.print(meal.getTotalCarbs() + "\t\t");
				System.out.print(meal.getUnitsPredicted() + "\t\t\t");
				System.out.print(meal.getUnitsTaken() + "\n");
				System.out.println(rowDelimiter);
			}
		}else{
			System.out.println("**No records**");
		}
		
		this.showMenu();
	}
	
	private boolean isNumber(String input){
		if(input == null || input == ""){
			return false;
		}else{
			try{
				Integer.parseInt(input);
			}catch(NumberFormatException e){
				return false;
			}
			return true;
		}
	}
	
	private boolean isInsulinLevel(String level){
		if(isNumber(level)){
			int l = Integer.parseInt(level);
			if(l > 5 && l < 750){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	private boolean isCarbCount(String carbCount){
		if(isNumber(carbCount)){
			int count = Integer.parseInt(carbCount);
			if(count >= 0 && count < 500){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	public void testPredictor(){
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(" #####             #");                                                    
		System.out.println(" #    #            #");                                
		System.out.println(" #     #           #         #####    ######   ######   #####    # ###"); 
		System.out.println(" #     #  #######  #        #     #  #     #  #     #  #     #   ##");
		System.out.println(" #     #           #        #     #  #     #  #     #  #######   #");  
		System.out.println(" #    #            #        #     #  #     #  #     #  #         #");   
		System.out.println(" #####             ######    #####    ######   ######   #####    #");   
		System.out.println("                                           #        #");   
		System.out.println("                                      #####    #####");
		
		Logger logger = new Logger();
		if(Meal.getUnfinishedMeal() != null){
			System.out.println("\n\nPlease Complete Your Meal!");
			logger.finishMeal();
		}else{
			logger.showMenu();
		}
	}

}
