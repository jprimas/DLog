package brain;

public class Record implements Comparable<Record>{
	
	private String desc;
	private String quantifier;
	private double amount;
	private String item;
	private double carbCount;
	private Food food;
	private int foodId;
	
	public Record(String desc, double carbCount, int foodId){
		this.desc = desc;
		this.carbCount = carbCount;
		this.foodId = foodId;
		if(verifyDescription(desc)){
			this.extractFoodFromDescritpion(desc);
			this.standardizeAmount();
			food = DatabaseConnector.getFoodItem(foodId);
			if(food == null){
				food = new Food(amount, quantifier, item, carbCount);
			}
		}
	}
	
	private void extractFoodFromDescritpion(String desc){
		if(desc == null || desc.equals("")){
			return;
		}
		String cleanDesc = desc.toLowerCase();
		String[] parts = cleanDesc.split(":");
		String[] quantifiers = parts[0].split(" ");
		for(String word : quantifiers){
			if(word.equals("")){
				continue;
			}
			if(isDouble(word)){
				amount = Double.parseDouble(word);
			}else{
				quantifier = word;
			}
		}
		if(parts.length == 2){
			item = parts[1].trim();
		}
	}
	
	private boolean isDouble(String input){
		if(input == null || input == ""){
			return false;
		}else{
			try{
				Double.parseDouble(input);
			}catch(NumberFormatException e){
				return false;
			}
			return true;
		}
	}
	
	private boolean verifyDescription(String desc){
		if(desc == null || desc.equals("")){
			return false;
		}
		if(desc.indexOf(":") < 0){
			return false;
		}
		return true;
	}
	
	private void standardizeAmount(){
		if(quantifier.equals("mg") || quantifier.equals("mgs") || quantifier.equals("milligrams") || quantifier.equals("milligram")){
			quantifier = "gram";
			amount = amount * 1000;
		}
		else if(quantifier.equals("g") || quantifier.equals("gs") || quantifier.equals("grams") || quantifier.equals("gram")){
			quantifier = "gram";
		}
		else if(quantifier.equals("kg") || quantifier.equals("kgs") || quantifier.equals("kilograms") || quantifier.equals("kilogram")){
			quantifier = "gram";
			amount = amount / 1000;
		}
		else if(quantifier.equals("ounce") || quantifier.equals("ounces") || quantifier.equals("oz") || quantifier.equals("ozs")){
			quantifier = "gram";
			amount = amount * 28.3495;
		}
		else if(quantifier.equals("liter") || quantifier.equals("l") || quantifier.equals("liters")){
			quantifier = "milliliter";
			amount = amount / 1000;
		}
		else if(quantifier.equals("milliliters") || quantifier.equals("milliliter") || quantifier.equals("ml")){
			quantifier = "milliliter";
		}
		else if(quantifier.equals("teaspoon") || quantifier.equals("teaspoons")){
			quantifier = "milliliter";
			amount = amount * 4.92892;
		}
		else if(quantifier.equals("tablespoon") || quantifier.equals("tablespoons") || quantifier.equals("t") || quantifier.equals("tb") 
				|| quantifier.equals("tbs") || quantifier.equals("tbsp") || quantifier.equals("tblsp") || quantifier.equals("tblspn")){
			quantifier = "milliliter";
			amount = amount * 14.7868;
		}
		else if(quantifier.equals("cup") || quantifier.equals("cups")){
			quantifier = "milliliter";
			amount = amount * 236.588;
		}
		else if(quantifier.equals("pint") || quantifier.equals("pints") || quantifier.equals("pt") || quantifier.equals("p")){
			quantifier = "milliliter";
			amount = amount * 473.176;
		}
		else if(quantifier.equals("quart") || quantifier.equals("quarts")){
			quantifier = "milliliter";
			amount = amount * 946.353;
		}
		else if(quantifier.equals("gallon") || quantifier.equals("gallons") || quantifier.equals("gal")){
			quantifier = "milliliter";
			amount = amount * 3785.41;
		}
		else if(quantifier.equals("slices") || quantifier.equals("slice") || quantifier.equals("toast") || quantifier.equals("bread") 
				|| quantifier.equals("bread")){
			quantifier = "slice";
		}
		else if(quantifier.equals("humpfle") || quantifier.equals("humpfles") || quantifier.equals("hampfle") || quantifier.equals("hampfles")){
			quantifier = "hampfle";
		}
		else if(quantifier.equals("tasse") || quantifier.equals("tasses")){
			quantifier = "tasse";
		}
	}
	
	//if foodId == 0 then we need to add a new food row
	public void saveRecord(int mealId){
		if(foodId == 0){
			foodId = food.saveNewFood();
		}
		DatabaseConnector.saveFoodRecord(desc, carbCount, mealId, foodId);
	}
	
	public Food[] getSimilarFoods(){
		return food.getSimilarFoods();
	}
	
	public boolean verifyRecord(){
		if(desc == null || desc == "" || food == null){
			return false;
		}else{
			return true;
		}
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public double getCarbCount() {
		return carbCount;
	}

	public void setCarbCount(double carbCount) {
		this.carbCount = carbCount;
		standardizeAmount();
		this.carbCount = carbCount;
		if(foodId == -1){
			food = new Food(amount, quantifier, item, carbCount);
		}
	}

	public Food getFood() {
		return food;
	}

	public void setFood(Food food) {
		this.food = food;
	}

	public int getFoodId() {
		return foodId;
	}

	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}

	public String getQuantifier() {
		return quantifier;
	}

	public double getAmount() {
		return amount;
	}

	public String getItem() {
		return item;
	}

	@Override
	public int compareTo(Record r) {
		if(this.food.getScore() == r.getFood().getScore()){
			return 0;
		}else if(this.food.getScore() < r.getFood().getScore()){
			return -1;
		}else{
			return 1;
		}
	}
	

}
