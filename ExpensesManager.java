import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ExpensesManager {
    static class Expense {
    private double amount;
    private String category;
    private String date;
    private String notes;

    public Expense(double amount, String category, String date, String notes) {
    this.amount = amount;
    this.category = category;
    this.date = date;
    this.notes = notes;
    }

    public double getAmount() {
    return amount;
    }

    public String getCategory() {
    return category;
    }

    public String getDate() {
    return date;
    }

    public String getNotes() {
    return notes;
    }

        public String toCSV() {
        return amount + "," + category + "," + date + "," + notes;
        }

    @Override
    public String toString() {
    return String.format("[%s] %s: $%.2f (%s)", date, category, amount, notes);
    }
    }

private static ArrayList<Expense> expenses = new ArrayList<>();
private static HashMap<String, Double> budgets = new HashMap<>();
private static final String FILE_NAME = "expenses.txt";
private static Scanner scanner = new Scanner(System.in);

public static void main(String[] args) {
System.out.println("=================================");
System.out.println("   Welcome to Expenses Manager   ");
System.out.println("=================================");

    loadExpenses();

    boolean running = true;
    while (running) {
        System.out.println("\nMain Menu:");
        System.out.println("1. Add Expense");
        System.out.println("2. View Expenses");
        System.out.println("3. Daily Report");
        System.out.println("4. Set Budget");
        System.out.println("5. Save & Exit");
        System.out.print("Choose an option (1-5): ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            addExpense();
        } else if (choice.equals("2")) {
            viewExpenses();
        } else if (choice.equals("3")) {
            showDailyReport();
        } else if (choice.equals("4")) {
            setBudget();
        } else if (choice.equals("5")) {
            saveExpenses();
            System.out.println("Goodbye!");
            running = false;
        } else {
            System.out.println("Invalid choice. Please enter a number between 1 and 5.");
        }
    }
}

    private static void addExpense() {
    System.out.println("\n--- Add New Expense ---");

    double amount = -1;
    while (amount < 0) {
    System.out.print("Enter amount: $");
    String input = scanner.nextLine();
    try {
    amount = Double.parseDouble(input);
    if (amount < 0) {
    System.out.println("Error: Amount cannot be negative.");
    }
    } catch (NumberFormatException e) {
    System.out.println("Error: Please enter a valid number (e.g., 15.50).");
    }
    }

    System.out.print("Enter category (e.g., Food, Transport): ");
    String category = scanner.nextLine().trim();

    String dateStr = "";
    boolean validDate = false;
    while (!validDate) {
    System.out.print("Enter date (YYYY-MM-DD) or press Enter for today: ");
    dateStr = scanner.nextLine().trim();
    if (dateStr.isEmpty()) {
    dateStr = LocalDate.now().toString();
    validDate = true;
    } else {
    try {
    LocalDate.parse(dateStr);
    validDate = true;
    } catch (DateTimeParseException e) {
    System.out.println("Error: Invalid date format. Please use YYYY-MM-DD.");
    }
    }
    }

    System.out.print("Enter notes: ");
    String notes = scanner.nextLine().trim();
    if (notes.isEmpty()) {
    notes = "No notes";
    }

    Expense newExpense = new Expense(amount, category, dateStr, notes);
    expenses.add(newExpense);
    System.out.println("Expense added successfully!");

    checkBudget(category, dateStr);
    }

    private static void viewExpenses() {
    System.out.println("\\n--- All Expenses ---");
    if (expenses.isEmpty()) {
    System.out.println("No expenses recorded yet.");
    return;
    }

    for (int i = 0; i < expenses.size(); i++) {
    System.out.println((i + 1) + ". " + expenses.get(i).toString());
    }
    }

    private static void showDailyReport() {
    String today = LocalDate.now().toString();
    System.out.println("\n--- Daily Report (" + today + ") ---");

    double totalToday = 0;
    HashMap<String, Double> categoryTotals = new HashMap<>();

    for (int i = 0; i < expenses.size(); i++) {
    Expense e = expenses.get(i);
    if (e.getDate().equals(today)) {
    totalToday += e.getAmount();
            String cat = e.getCategory().toLowerCase();
            double currentCatTotal = 0;
            if (categoryTotals.containsKey(cat)) {
                currentCatTotal = categoryTotals.get(cat);
            }
            categoryTotals.put(cat, currentCatTotal + e.getAmount());
        }        }

    if (totalToday == 0) {
    System.out.println("No expenses recorded for today.");
    return;
    }

    System.out.printf("Total spent today: $%.2f\\n", totalToday);
    System.out.println("Breakdown by category:");

    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
    String categoryName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
    System.out.printf(" - %s: $%.2f\n", categoryName, entry.getValue());
    }
    }

    private static void setBudget() {
    System.out.println("\n--- Set Category Budget ---");
    System.out.print("Enter category: ");
    String category = scanner.nextLine().trim().toLowerCase();

    double limit = -1;
    while (limit < 0) {
    System.out.print("Enter daily budget limit for " + category + ": $");
    String input = scanner.nextLine();
    try {
    limit = Double.parseDouble(input);
    if (limit < 0) {
    System.out.println("Error: Budget cannot be negative.");
    }
    } catch (NumberFormatException e) {
    System.out.println("Error: Please enter a valid number.");
    }
    }

    budgets.put(category, limit);
    System.out.printf("Daily budget for '%s' set to $%.2f\n", category, limit);
    }

    private static void checkBudget(String category, String dateStr) {
    String catKey = category.toLowerCase();
    if (!budgets.containsKey(catKey)) {
    return;
    }

    double limit = budgets.get(catKey);
    double spent = 0;

    for (int i = 0; i < expenses.size(); i++) {
    Expense e = expenses.get(i);
    if (e.getCategory().equalsIgnoreCase(category) && e.getDate().equals(dateStr)) {
    spent += e.getAmount();
    }
    }

    if (spent > limit) {
    System.out.println("\n************************************************");
    System.out.println("   ALERT: You have exceeded your daily budget!    ");
    System.out.printf("   Category: %s\n", category);
    System.out.printf("   Budget Limit: $%.2f\n", limit);
    System.out.printf("   Spent Today:  $%.2f\n", spent);
    System.out.println("************************************************\n");
    }
    }

    private static void loadExpenses() {
    File file = new File(FILE_NAME);
    if (!file.exists()) {
    System.out.println("No existing data found. Starting fresh.");
    return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    String line;
    int count = 0;
    while ((line = reader.readLine()) != null) {
    String[] parts = line.split(",", 4);
    if (parts.length == 4) {
    try {
    double amount = Double.parseDouble(parts[0]);
    expenses.add(new Expense(amount, parts[1], parts[2], parts[3]));
    count++;
    } catch (NumberFormatException e) {
    System.out.println("Warning: Skipping malformed record -> " + line);
    }
    }
    }
    System.out.println("Successfully loaded " + count + " expenses.");
    } catch (IOException e) {
    System.out.println("Error reading file. Starting with an empty list.");
    }
    }

    private static void saveExpenses() {
    try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
    for (int i = 0; i < expenses.size(); i++) {
    writer.println(expenses.get(i).toCSV());
    }
    System.out.println("\nExpenses saved successfully to " + FILE_NAME);
    } catch (IOException e) {
    System.out.println("\nError: Could not save data to file.");
    }
    }
    }