import java.util.*;

/***
 * The problem is about creating a simplified Excel spreadsheet using a 2D array as the backend data structure. The height, H, and the width, W, of the Excel is given at the time of construction where H is a positive integer, and W is an uppercase letter denoting the maximum width of the Excel sheet. The user should be able to set a value to any cell in the spreadsheet by specifying the row and column. Along with that, the user should be able to get the value stored in any cell. Additionally, the program should support the capability to calculate the sum of a list of cells or a range of cells and store the sum in a particular cell.
 */
public class Excel {
   Formula[][] Formulas;
   class Formula {
       Formula(HashMap < String, Integer > c, int v) {
           val = v;
           cells = c;
       }
       HashMap < String, Integer > cells;
       int val;
   }
   Stack < int[] > stack = new Stack < > ();
   public Excel(int H, char W) {
       Formulas = new Formula[H][(W - 'A') + 1];
   }
   public int get(int r, char c) {
       if (Formulas[r - 1][c - 'A'] == null)
           return 0;
       return Formulas[r - 1][c - 'A'].val;
   }
   public void set(int r, char c, int v) {
       Formulas[r - 1][c - 'A'] = new Formula(new HashMap < String, Integer > (), v);
       topologicalSort(r - 1, c - 'A');
       execute_stack();
   }
   public int sum(int r, char c, String[] strs) {
       HashMap < String, Integer > cells = convert(strs);
       int summ = calculate_sum(r - 1, c - 'A', cells);
       set(r, c, summ);
       Formulas[r - 1][c - 'A'] = new Formula(cells, summ);
       return summ;
   }
   public void topologicalSort(int r, int c) {
       for (int i = 0; i < Formulas.length; i++)
           for (int j = 0; j < Formulas[0].length; j++)
               if (Formulas[i][j] != null && Formulas[i][j].cells.containsKey("" + (char)('A' + c) + (r + 1))) {
                   topologicalSort(i, j);
               }
       stack.push(new int[] {r,c});
   }
   public void execute_stack() {
       while (!stack.isEmpty()) {
           int[] top = stack.pop();
           if (Formulas[top[0]][top[1]].cells.size() > 0)
               calculate_sum(top[0], top[1], Formulas[top[0]][top[1]].cells);
       }
   }
   public HashMap < String, Integer > convert(String[] strs) {
       HashMap < String, Integer > res = new HashMap < > ();
       for (String st: strs) {
           if (st.indexOf(":") < 0)
               res.put(st, res.getOrDefault(st, 0) + 1);
           else {
               String[] cells = st.split(":");
               int si = Integer.parseInt(cells[0].substring(1)), ei = Integer.parseInt(cells[1].substring(1));
               char sj = cells[0].charAt(0), ej = cells[1].charAt(0);
               for (int i = si; i <= ei; i++) {
                   for (char j = sj; j <= ej; j++) {
                       res.put("" + j + i, res.getOrDefault("" + j + i, 0) + 1);
                   }
               }
           }
       }
       return res;
   }
   public int calculate_sum(int r, int c, HashMap < String, Integer > cells) {
       int sum = 0;
       for (String s: cells.keySet()) {
           int x = Integer.parseInt(s.substring(1)) - 1, y = s.charAt(0) - 'A';
           sum += (Formulas[x][y] != null ? Formulas[x][y].val : 0) * cells.get(s);
       }
       Formulas[r][c] = new Formula(cells, sum);
       return sum;
   }
    public static void main(String[] args) {
        // Create Excel with 3 rows and columns A to C
        Excel excel = new Excel(3, 'C');

        // Test 1: Basic set and get
        excel.set(1, 'A', 5); // Set cell A1 = 5
        System.out.println("A1 = " + excel.get(1, 'A')); // Expected: 5

        // Test 2: Another set + get
        excel.set(2, 'B', 10); // Set cell B2 = 10
        System.out.println("B2 = " + excel.get(2, 'B')); // Expected: 10

        // Test 3: Sum from two individual cells
        String[] sumCells1 = {"A1", "B2"}; 
        int sum1 = excel.sum(3, 'A', sumCells1); // C3 = A1 + B2
        System.out.println("C3 (sum of A1,B2) = " + sum1); // Expected: 15

        // Test 4: Sum with repeated cells
        String[] sumCells2 = {"A1", "A1", "B2"}; 
        int sum2 = excel.sum(3, 'B', sumCells2); // B3 = A1 + A1 + B2
        System.out.println("B3 (A1 twice + B2) = " + sum2); // Expected: 20

        // Test 5: Sum using range
        excel.set(1, 'B', 2);   // B1 = 2
        excel.set(1, 'C', 3);   // C1 = 3
        String[] sumRange = {"A1:C1"}; // sum of A1, B1, C1 = 5 + 2 + 3
        int sum3 = excel.sum(2, 'C', sumRange);
        System.out.println("C2 (range A1:C1) = " + sum3); // Expected: 10

        // Test 6: Updating dependencies: When A1 changes, C3 should update
        excel.set(1, 'A', 7);  // A1 changed to 7
        System.out.println("Updated A1 = " + excel.get(1, 'A')); // Expected: 7
        System.out.println("Updated C3 = " + excel.get(3, 'A')); // Expected: recalc = 17

        // Test 7: Range dependencies update
        excel.set(1, 'B', 4); // Update B1
        System.out.println("Updated C2 (range) = " + excel.get(2, 'C')); // Expected: 7+4+3 = 14
    }
}

