package brain;


public class Food {
	
	private int id;
	private double amount;
	private String quantifier;
	private String item;
	private double carbCount;
	private double units;
	private int score;
	
	//creating from user input
	public Food(double amount, String quantifier, String item, double carbCount){
		this.amount = amount;
		this.quantifier = quantifier;
		this.item = item;
		this.carbCount = carbCount;
		this.units = carbCount / DatabaseConnector.getCarbsPerUnit();
		this.id = -1;
		this.score = 1;
		this.generalizeAmounts();
	}
	
	//creating from DB
	public Food(int id, double amount, String quantifier, String item, double carbCount, double units, int score){
		this.id = id;
		this.amount = amount;
		this.quantifier = quantifier;
		this.item = item;
		this.carbCount = carbCount;
		this.units = units;
		this.score = score;
	}
	
	public int saveNewFood(){
		DatabaseConnector.saveFood(amount, quantifier, item, carbCount, units);
		return DatabaseConnector.getFoodId(amount, quantifier, item);
	}
	
	public void updateFood(){
		if(id > 0){
			DatabaseConnector.updateFood(id, carbCount, units, score);
		}
	}
	
	private void generalizeAmounts(){
		if(quantifier.equals("g") || quantifier.equals("gs") || quantifier.equals("grams") || quantifier.equals("gram")){
			quantifier = "gram";
			carbCount = carbCount / amount;
			units = units / amount;
			amount = 1;
		}
		else if(quantifier.equals("milliliters") || quantifier.equals("milliliter") || quantifier.equals("ml")){
			quantifier = "milliliter";
			carbCount = carbCount / amount;
			units = units / amount;
			amount = 1;
		}
		else if(quantifier.equals("slices") || quantifier.equals("slice") || quantifier.equals("toast") || quantifier.equals("bread") 
				|| quantifier.equals("bread")){
			quantifier = "slice";
			carbCount = carbCount / amount;
			units = units / amount;
			amount = 1;
		}
		else if(quantifier.equals("hampfle")){
			quantifier = "hampfle";
			carbCount = carbCount / amount;
			units = units / amount;
			amount = 1;
		}
		else if(quantifier.equals("tasse")){
			quantifier = "tasse";
			carbCount = carbCount / amount;
			units = units / amount;
			amount = 1;
		}
	}
	
	public Food[] getSimilarFoods(){
		return DatabaseConnector.getSimilarFood(item, quantifier);
	}
	
	public String foodName(){
		return item;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getQuantifier() {
		return quantifier;
	}

	public void setQuantifier(String quantifier) {
		this.quantifier = quantifier;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public double getCarbCount() {
		return carbCount;
	}

	public void setCarbCount(double carbCount) {
		double carbsPerUnit = DatabaseConnector.getCarbsPerUnit();
		this.carbCount = carbCount;
		this.units = carbCount / carbsPerUnit;
	}

	public double getUnits() {
		return units;
	}

	public void setUnits(double units) {
		this.units = units;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
