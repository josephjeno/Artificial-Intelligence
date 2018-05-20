import java.util.*;

// name : Joseph Jeno

// time using tiles-out-of-place heuristic : 20.672852 milliseconds
// time using manhattan-distance heuristic : 10.250746 milliseconds

// What would the likely effect be of using Euclidean distance
// (straight-line distance, allowing diagonals) instead of Manhattan distance
// or tiles displaced as the heuristic?
// Since diagonal movement is not possible, it would be a poor heuristic, as the
// cost for a diagonal distance heuristic would be understated, since it
// would require 2 tile moves/state changes.

// Would this work?
// Yes, diagonal distance is essentially manhattan distance / 2, so a heuristic
// might be 1 instead of 2.  The PQ might select the wrong nodes to expand but
// eventually it will find a solution.

// How is it likely to rank in speed, and why?
// Definitely worse than manhattan distance. Probably worse than tiles-out-of-place

// Solving the 16-puzzle with A* using two heuristics:
// tiles-out-of-place and total-distance-to-move

// Comparator for tiles-out-of-place heuristic
class TilesOutOfPlaceComparator implements Comparator<NumberPuzzleNode> {

    public int compare(NumberPuzzleNode v1, NumberPuzzleNode v2) {
        int v1Key = v1.getCost() + v1.getTilesOutOfPlace();
        int v2Key = v2.getCost() + v2.getTilesOutOfPlace();

        if (v1Key < v2Key) {
            return -1; }
        else if (v1Key > v2Key) {
            return 1;}
        return 0;
    }
}

class ManhattanDistanceComparator implements Comparator<NumberPuzzleNode> {

    public int compare(NumberPuzzleNode v1, NumberPuzzleNode v2) {
        int v1Key = v1.getCost() + v1.getManhattanDistance();
        int v2Key = v2.getCost() + v2.getManhattanDistance();

        if (v1Key < v2Key) {
            return -1; }
        else if (v1Key > v2Key) {
            return 1;}
        return 0;
    }
}

// Object for NumberPuzzle nodes containing parent and cost
class NumberPuzzleNode {

    private NumberPuzzle vertex;
    private NumberPuzzleNode parent;
    private int cost;

    public NumberPuzzleNode(NumberPuzzle givenVertex,
                            NumberPuzzleNode givenParent, int givenCost) {
        vertex = givenVertex;
        parent = givenParent;
        cost = givenCost;
    }

    // returns this vertex's state
    public NumberPuzzle getState() {
        return this.vertex;
    }

    // returns parent node
    public NumberPuzzleNode getParent() {
        return this.parent;
    }

    // returns this vertex's cost
    public int getCost() {
        return this.cost;
    }

    // returns this vertex's manhattan distance
    public int getManhattanDistance() {
        return vertex.manhattanDistance();
    }

    // returns # tiles out of place heuristic
    public int getTilesOutOfPlace() {
        return vertex.tilesOutOfPlace();
    }

    // Route to this node from start node
    public LinkedList<NumberPuzzle> routeToStart() {
        LinkedList<NumberPuzzle> route = new LinkedList<>();

        NumberPuzzleNode n = this;

        while (n.getParent() != null) {
            route.addFirst(n.getState());
            n = n.getParent();
        }

        route.addFirst(n.getState());

        return route;
    }

    @Override
    public String toString() {
        return "\n" + "Vertex is " + "\n" + vertex + "\n" +
                "Parent is " + "\n" + parent + "\n" +
                "Cost is " + this.cost + "\n" +
                //"Tiles out of place is " + this.getTilesOutOfPlace() + "\n"
                "Manhattan distance is " + this.getManhattanDistance() + "\n";
    }
}

public class NumberPuzzle {
    public static final int PUZZLE_WIDTH = 4;
    public static final int BLANK = 0;
    // BETTER:  false for tiles-displaced heuristic, true for Manhattan distance
    public static boolean BETTER = true;

    // Array of Array of Integers
    private int[][] tiles;  // [row][column]

    private int blank_r, blank_c;   // blank row and column

    public static void main(String[] args) {
        NumberPuzzle myPuzzle = readPuzzle(); // reads input from console
        System.out.println(System.nanoTime());
        LinkedList<NumberPuzzle> solutionSteps = myPuzzle.solve(BETTER);
        printSteps(solutionSteps);
        System.out.println(System.nanoTime());
    }

    NumberPuzzle() {
        tiles = new int[PUZZLE_WIDTH][PUZZLE_WIDTH];
    }

    // Returns array of array of ints for puzzle input on console
    static NumberPuzzle readPuzzle() {
        NumberPuzzle newPuzzle = new NumberPuzzle();

        Scanner myScanner = new Scanner(System.in); // input from console
        int row = 0;
        while (myScanner.hasNextLine() && row < PUZZLE_WIDTH) {
            String line = myScanner.nextLine();
            String[] numStrings = line.split(" ");
            for (int i = 0; i < PUZZLE_WIDTH; i++) {
                if (numStrings[i].equals("-")) {
                    newPuzzle.tiles[row][i] = BLANK;
                    newPuzzle.blank_r = row;
                    newPuzzle.blank_c = i;
                } else {
                    newPuzzle.tiles[row][i] = new Integer(numStrings[i]);
                }
            }
            row++;
        }
        return newPuzzle;
    }

    // Returns string output of array of array of ints in 15-puzzle format
    public String toString() {
        String out = "";
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_WIDTH; j++) {
                if (j > 0) {
                    out += " ";
                }
                if (tiles[i][j] == BLANK) {
                    out += "-";
                } else {
                    out += tiles[i][j];
                }
            }
            out += "\n";
        }
        return out;
    }

    // Creates new NumberPuzzle
    public NumberPuzzle copy() {
        NumberPuzzle clone = new NumberPuzzle();
        clone.blank_r = blank_r;
        clone.blank_c = blank_c;
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_WIDTH; j++) {
                clone.tiles[i][j] = this.tiles[i][j];
            }
        }
        return clone;
    }

    // betterH:  if false, use tiles-out-of-place heuristic
    //           if true, use total-manhattan-distance heuristic
    LinkedList<NumberPuzzle> solve(boolean betterH) {

        // Priority Queue
        PriorityQueue<NumberPuzzleNode> PQ;

        if (betterH) {
            PQ = new PriorityQueue(new ManhattanDistanceComparator());
        } else {
            PQ = new PriorityQueue(new TilesOutOfPlaceComparator());
        }

        // Init priority queue to start node
        PQ.add(new NumberPuzzleNode(this, null, 0));

        // while priority queue isn't empty
        while (!PQ.isEmpty()) {

            // remove lowest cost node "n"
            NumberPuzzleNode n = PQ.poll();

            // if n is the goal, return route to n
            if (n.getState().solved()) {
                return n.routeToStart();
            }

            // for each neighbor of n, add node to pq
            for (NumberPuzzleNode neighbor : n.getState().neighbors(n)) {
                PQ.add(neighbor);
            }
        }
        throw new RuntimeException("No valid solution");
    }

    // generates a list of neighbors for a particular puzzle configuration
    private LinkedList<NumberPuzzleNode> neighbors(NumberPuzzleNode n) {
        LinkedList<NumberPuzzleNode> neighborList = new LinkedList<> ();

        if (blank_r != 0) {
            NumberPuzzle topNeighbor = this.copy();
            topNeighbor.blank_r = blank_r - 1;
            topNeighbor.tiles[blank_r][blank_c] = tiles[blank_r - 1][blank_c];
            topNeighbor.tiles[topNeighbor.blank_r][blank_c] = 0;
            neighborList.add(new NumberPuzzleNode(topNeighbor, n, n.getCost() + 1));
        }

        if (blank_r != 3) {
            NumberPuzzle bottomNeighbor = this.copy();
            bottomNeighbor.blank_r = blank_r + 1;
            bottomNeighbor.tiles[blank_r][blank_c] = tiles[blank_r + 1][blank_c];
            bottomNeighbor.tiles[bottomNeighbor.blank_r][blank_c] = 0;
            neighborList.add(new NumberPuzzleNode(bottomNeighbor, n, n.getCost() + 1));
        }

        if (blank_c != 0) {
            NumberPuzzle leftNeighbor = this.copy();
            leftNeighbor.blank_c = blank_c - 1;
            leftNeighbor.tiles[blank_r][blank_c] = tiles[blank_r][blank_c - 1];
            leftNeighbor.tiles[blank_r][leftNeighbor.blank_c] = 0;
            neighborList.add(new NumberPuzzleNode(leftNeighbor, n, n.getCost() + 1));
        }

        if (blank_c != 3) {
            NumberPuzzle rightNeighbor = this.copy();
            rightNeighbor.blank_c = blank_c + 1;
            rightNeighbor.tiles[blank_r][blank_c] = tiles[blank_r][blank_c + 1];
            rightNeighbor.tiles[blank_r][rightNeighbor.blank_c] = 0;
            neighborList.add(new NumberPuzzleNode(rightNeighbor, n, n.getCost() + 1));
        }

        return neighborList;
    }

    // returns number of tiles out of place
    public int tilesOutOfPlace() {
        int heuristic = 0;
        int shouldBe = 1;
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_WIDTH; j++) {
                if (tiles[i][j] != shouldBe) {
                    heuristic += 1;
                }
                shouldBe = (shouldBe + 1) % (PUZZLE_WIDTH*PUZZLE_WIDTH);
            }
        }
        return heuristic;
    }

    // returns manhattan distance to goal position
    public int manhattanDistance() {
        int heuristic = 0;
        int shouldBe = 1;
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_WIDTH; j++) {
                if (tiles[i][j] != shouldBe) {
                    heuristic += manhattanDistanceTile(i,j);
                }
                shouldBe = (shouldBe + 1) % (PUZZLE_WIDTH*PUZZLE_WIDTH);
            }
        }
        return heuristic;
    }

    // returns manhattan distance to goal position for specific tile
    public int manhattanDistanceTile(int row, int column) {
        int actualRow = row;
        int actualColumn = column;

        int number = tiles[row][column];

        int goalRow;
        int goalColumn;

        if (number % PUZZLE_WIDTH == 0)
            goalRow = (number / PUZZLE_WIDTH) - 1;
        else
            goalRow = number / PUZZLE_WIDTH;

        if (number % PUZZLE_WIDTH == 0) {
            goalColumn = (PUZZLE_WIDTH - 1);
        } else {
            goalColumn = (number % PUZZLE_WIDTH) - 1;
        }

        return (Math.abs(actualRow - goalRow) + Math.abs(actualColumn - goalColumn));
    }

    // returns true if solved, false if not solved
    public boolean solved() {
        int shouldBe = 1;
        for (int i = 0; i < PUZZLE_WIDTH; i++) {
            for (int j = 0; j < PUZZLE_WIDTH; j++) {
                if (tiles[i][j] != shouldBe) {
                    return false;
                } else {
                    // Take advantage of BLANK == 0
                    shouldBe = (shouldBe + 1) % (PUZZLE_WIDTH*PUZZLE_WIDTH);
                }
            }
        }
        return true;
    }

    // prints states from start node to end node
    static void printSteps(LinkedList<NumberPuzzle> steps) {
        for (NumberPuzzle s : steps) {
            System.out.println(s);
        }
    }

}
