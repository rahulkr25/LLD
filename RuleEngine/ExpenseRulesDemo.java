import java.util.*;
/*
Design a Rules Engine for Corporate Card Expenses
You are building an in-memory rules engine for a corporate credit-card expense system.
Managers define expense policies (rules) that must be evaluated against a list of expense transactions.

Each expense is represented as a record with fields such as:
expenseId, tripId (optional), expenseType, sellerType, sellerName, amount (USD)

Functional Requirements
Implement: evaluateRules(List<Rule>, List<Expense>) → EvaluationResult

The engine must:
Validate expense-level rules
Validate aggregate rules (e.g., trip-level totals)
Flag violating expenses or trips
Return explainable, audit-friendly output
Support easy addition of new rules without changing engine logic

These clarifications score points:
Expenses are immutable events
Amounts are strings → parsed to numbers
Currency assumed USD (for now)
Rules are independent
Evaluation is exhaustive (no short-circuit)
Violations are flags, not blockers
Multi-tenant handled outside this engine
*/


enum ExpenseType {
    AIRFARE,
    ENTERTAINMENT,
    FOOD,
}

enum SellerType {
    RESTAURANT,
    AIRLINE,
    THEATER,
}

enum EntityType {
    EXPENSE,
    TRIP
}

class Expense {
     String expenseId;
     String tripId;
     ExpenseType expenseType;
     SellerType sellerType;
     String sellerName;
     double amount; // in USD
    
     Expense(String expenseId, String tripId, ExpenseType expenseType, 
                  SellerType sellerType, String sellerName, double amount) {
        this.expenseId = expenseId;
        this.tripId = tripId;
        this.expenseType = expenseType;
        this.sellerType = sellerType;
        this.sellerName = sellerName;
        this.amount = amount;
    }
}

class RuleViolation {
     String ruleId;
     EntityType entityType;
     String entityId; // expenseId or tripId
     String message;
    
     RuleViolation(String ruleId, EntityType entityType, String entityId, String message) {
        this.ruleId = ruleId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.message = message;
    }
}

interface Rule {
    String ruleId();
    String description();
    
    default boolean appliesToExpense(){return false;};
    default boolean appliesToTrip(){return false;};
}

abstract class ExpenseRule implements Rule{
    public boolean appliesToExpense(){
        return true;
    }
    abstract boolean validate(Expense expense);
}

abstract class TripRule implements Rule{
    public boolean appliesToTrip(){
        return true;
    }
    abstract boolean validate(List<Expense> tripExpenses);
}

class MaxExpenseRule extends ExpenseRule{

    @Override
    public String ruleId() {
        return "MAX_EXPENSE_RULE";
    }

    @Override
    public String description() {
        return "No expense over $250";
    }


    @Override
    boolean validate(Expense expense) {
      return expense.amount <= 250.0;
    }   
}

class RestaurantLimitRule extends ExpenseRule{

    @Override
    public String ruleId() {
        return "RESTAURANT_LIMIT_RULE";
    }

    @Override
    public String description() {
        return "Restaurant expense must be <= $75";
    }

    @Override
    boolean validate(Expense expense) {   
        if(expense.sellerType == SellerType.RESTAURANT){
            return expense.amount <= 75.0;
        }
        return true;
     }
}

class NoAirFareRule extends ExpenseRule{

    @Override
    public String ruleId() {
        return "NO_AIRFARE_RULE";
    }

    @Override
    public String description() {
        return "No Airfare expenses allowed";
    }

    @Override
    boolean validate(Expense expense) {   
        return expense.expenseType != ExpenseType.AIRFARE;
     }
}

class NoEntertainmentRule extends ExpenseRule {

    public String ruleId() {
        return "NO_ENTERTAINMENT";
    }

    public String description() {
        return "Entertainment expenses are not allowed";
    }

    boolean validate(Expense e) {
        return e.expenseType != ExpenseType.ENTERTAINMENT;
    }
}

class TripeTotalLimitRule extends TripRule {
    double limit;
    TripeTotalLimitRule(double limit){
        this.limit = limit;
    }

    @Override
    public String ruleId() {
        return "TRIP_TOTAL_LIMIT_RULE";
    }

    @Override
    public String description() {
        return "Total trip expenses must be <= $" + limit;
    }

    @Override
    boolean validate(List<Expense> tripExpenses) {
        double total = 0.0;
        for(Expense e : tripExpenses){
            total += e.amount;
        }
        return total <= limit;
    }   
}

class RuleEngine {
    List<RuleViolation> evaluateRules(List<Rule>rules, List<Expense> expenses){
    List<RuleViolation> violations = new ArrayList<>();
    // Expense-level rule evaluation
    for(Expense expense: expenses){
        for(Rule rule: rules){
            if(rule.appliesToExpense()){
                ExpenseRule expenseRule = (ExpenseRule) rule;
                if(!expenseRule.validate(expense)){
                    violations.add(new RuleViolation(rule.ruleId(), EntityType.EXPENSE, expense.expenseId, 
                        "Expense violation: " + rule.description()));
                }
            }
        }
    }
    // Trip-level rule evaluation; Group expenses by tripId
    Map<String, List<Expense>> tripMap = new HashMap<>();
    for(Expense expense: expenses){
        if(expense.tripId != null && !expense.tripId.isEmpty()){
         tripMap.putIfAbsent(expense.tripId, new ArrayList<>());
         tripMap.get(expense.tripId).add(expense);
        }
    }
    
    // Trip-level rule evaluation
    for(Map.Entry<String,List<Expense>> entry: tripMap.entrySet()){
        for(Rule rule: rules){
            if(rule.appliesToTrip()){
                TripRule tripRule= (TripRule) rule;
                if(!tripRule.validate(entry.getValue())){
                    violations.add(new RuleViolation(rule.ruleId(), EntityType.TRIP, entry.getKey(), 
                        "Trip violation: " + rule.description()));
                }
            }
        }
    } 

    return violations;
}
}

public class ExpenseRulesDemo{
    public static void main(String [] args){
         List<Expense> expenses = List.of(
            new Expense("1", "T1", ExpenseType.FOOD, SellerType.RESTAURANT, "ABC", 120),
            new Expense("2", "T1", ExpenseType.AIRFARE, SellerType.AIRLINE, "XYZ", 500),
            new Expense("3", "T1", ExpenseType.FOOD, SellerType.RESTAURANT, "DEF", 90),
            new Expense("4", "T2", ExpenseType.ENTERTAINMENT, SellerType.THEATER, "PQR", 50)
        );
         List<Rule> rules = List.of(
            new MaxExpenseRule(),
            new RestaurantLimitRule(),
            new NoAirFareRule(),
            new NoEntertainmentRule(),
            new TripeTotalLimitRule(200)
        );

        RuleEngine engine = new RuleEngine();
           List<RuleViolation> violations =
            engine.evaluateRules(rules, expenses);

        for (RuleViolation v : violations) {
            System.out.println(
                v.entityType + " " + v.entityId +
                " violated " + v.ruleId +
                " → " + v.message
            );
        }
    }
    
}
