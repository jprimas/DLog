package brain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseConnector {

	private static Connection connection;

	public static boolean connectDB(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			String Url = "jdbc:MySql://localhost:3306/d_logger_test";
			connection = DriverManager.getConnection(Url, "root", "");
			return true;
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Connection Error");
			return false;
		}
	}

	public static boolean saveMeal(int totalCarbs, int levelBefore, int unitsTaken, int unitsPredicted){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String insertMealString = "INSERT INTO meals" +
					"(total_carbs, level_before, units_taken, units_predicted)" +
					" VALUES (?,?,?,?)";
			PreparedStatement updateMeal = connection.prepareStatement(insertMealString);
			updateMeal.setInt(1, totalCarbs);
			updateMeal.setInt(2, levelBefore);
			updateMeal.setInt(3, unitsTaken);
			updateMeal.setInt(4, unitsPredicted);
			updateMeal.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Meal getMeal(int mealId){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * from meals where id = ?");
			statement.setInt(1, mealId);
			ResultSet rs = statement.executeQuery();
			Meal meal = null;
			while(rs.next()){
				meal = new Meal(rs.getInt("id"), rs.getInt("total_carbs"), rs.getInt("level_before"),
						rs.getInt("level_after"), rs.getInt("units_taken"),
						rs.getInt("units_predicted"), rs.getDate("meal_time"));
			}
			if(meal != null){
				return addAllRecords(meal);
			}else{
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Meal addAllRecords(Meal meal){
		if(meal == null){
			return null;
		}
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM records where meal_id = ?");
			statement.setInt(1, meal.getMealId());
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				System.out.println(rs.getString("description"));
				meal.addRecord(new Record(rs.getString("description"), rs.getInt("carb_count"), rs.getInt("food_id")));
			}
			return meal;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Meal[] getRecentMeals(){
		if(connection == null){
			connectDB();
		}
		ArrayList<Meal> meals = new ArrayList<Meal>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * from meals order by id desc limit 10;");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				meals.add(new Meal(rs.getInt("id"), rs.getInt("total_carbs"), rs.getInt("level_before"),
						rs.getInt("level_after"), rs.getInt("units_taken"),
						rs.getInt("units_predicted"), rs.getDate("meal_time")));
			}
			return meals.toArray(new Meal[]{});
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getUnfinishedMealId(){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT id from meals where level_after = -1");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getInt("id");
			}
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static boolean finishMeal(int levelAfter){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		int mealId = getUnfinishedMealId();
		if(mealId > 0){
			try {
				String updateMealString = "UPDATE meals SET level_after = ? where id = ?";
				PreparedStatement updateMeal = connection.prepareStatement(updateMealString);
				updateMeal.setInt(1, levelAfter);
				updateMeal.setInt(2, mealId);
				updateMeal.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}else{
			//no meals need to be updated
			return false;
		}
	}

	public static boolean saveFoodRecord(String desc, double carbCount, int mealId, int foodId){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String insertFoodString = "INSERT INTO records" +
					"(description, carb_count, meal_id, food_id)" +
					" VALUES (?,?,?,?)";
			PreparedStatement foodItem = connection.prepareStatement(insertFoodString);
			foodItem.setString(1, desc);
			foodItem.setDouble(2, carbCount);
			foodItem.setInt(3, mealId);
			foodItem.setInt(4, foodId);
			foodItem.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean saveFood(double amount, String quantifier, String item, double carbs, double units){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String insertFoodString = "INSERT INTO foods" +
					"(amount, quantifier, item, carbs, units)" +
					" VALUES (?,?,?,?,?)";
			PreparedStatement foodItem = connection.prepareStatement(insertFoodString);
			foodItem.setDouble(1, amount);
			foodItem.setString(2, quantifier);
			foodItem.setString(3, item);
			foodItem.setDouble(4, carbs);
			foodItem.setDouble(5, units);
			foodItem.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean updateFood(int id, double carbs, double units, int score){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String udpateFoodString = "UPDATE foods SET" +
					" carbs = ?, units = ?, score = ?" +
					" WHERE id = ?";
			PreparedStatement foodItem = connection.prepareStatement(udpateFoodString);
			foodItem.setDouble(1, carbs);
			foodItem.setDouble(2, units);
			foodItem.setInt(3, score);
			foodItem.setDouble(4, id);
			foodItem.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static int getFoodId(double amount, String quantifier, String item){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT id FROM foods WHERE amount = ? AND quantifier = ? AND item = ?");
			statement.setDouble(1, amount);
			statement.setString(2, quantifier);
			statement.setString(3, item);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getInt("id");
			}
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static Food[] getSimilarFood(String item, String quantifier){
		if(connection == null){
			connectDB();
		}
		ArrayList<Food> food = new ArrayList<Food>();
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM foods WHERE match(item) against(?) and quantifier = ?");
			statement.setString(1, item);
			statement.setString(2, quantifier);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				food.add(new Food(rs.getInt("id"), rs.getDouble("amount"), rs.getString("quantifier"), rs.getString("item"),
						rs.getDouble("carbs"), rs.getDouble("units"), rs.getInt("score")));
			}
			return food.toArray(new Food[]{});
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Food getFoodItem(int foodId){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM foods WHERE id = ?");
			statement.setInt(1, foodId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return new Food(rs.getInt("id"), rs.getDouble("amount"), rs.getString("quantifier"), rs.getString("item"),
						rs.getDouble("carbs"), rs.getDouble("units"), rs.getInt("score"));
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static double getSugarPerUnit(){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT sugar_per_unit FROM data WHERE id = 1");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getDouble("sugar_per_unit");
			}
			return 0.0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public static int getSugarPerUnitScore(){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT sugar_per_unit_score FROM data WHERE id = 1");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getInt("sugar_per_unit_score");
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static boolean updateSugarPerUnit(double newSugarPerUnit){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String updateString = "UPDATE data SET sugar_per_unit = ? where id = 1";
			PreparedStatement updateMeal = connection.prepareStatement(updateString);
			updateMeal.setDouble(1, newSugarPerUnit);
			updateMeal.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean updateSugarPerUnitScore(int newSugarPerUnitScore){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String updateString = "UPDATE data SET sugar_per_unit_score = ? where id = 1";
			PreparedStatement updateMeal = connection.prepareStatement(updateString);
			updateMeal.setInt(1, newSugarPerUnitScore);
			updateMeal.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static double getCarbsPerUnit(){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT carbs_per_unit FROM data WHERE id = 1");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getDouble("carbs_per_unit");
			}
			return 0.0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public static int getCarbsPerUnitScore(){
		if(connection == null){
			connectDB();
		}
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT carbs_per_unit_score FROM data WHERE id = 1");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				return rs.getInt("carbs_per_unit_score");
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static boolean updateCarbsPerUnit(double newCarbsPerUnit){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String updateMealString = "UPDATE data SET carbs_per_unit = ? where id = 1";
			PreparedStatement updateMeal = connection.prepareStatement(updateMealString);
			updateMeal.setDouble(1, newCarbsPerUnit);
			updateMeal.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean updateCarbsPerUnitScore(int newCarbsPerUnitScore){
		if(connection == null){
			if(!connectDB()){
				return false;
			}
		}
		try {
			String updateMealString = "UPDATE data SET carbs_per_unit_score = ? where id = 1";
			PreparedStatement updateMeal = connection.prepareStatement(updateMealString);
			updateMeal.setInt(1, newCarbsPerUnitScore);
			updateMeal.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
