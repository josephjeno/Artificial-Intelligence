import java.util.ArrayList;
import java.util.Scanner;

public class OthelloSolver {

	static final int NUM_COLUMNS = 8;
	// We want to keep these enum values so that flipping ownership is just a sign change
	static final int WHITE = 1;
	static final int NOBODY = 0;
	static final int BLACK = -1;
	static final int TIE = 2;

	static final boolean WHITE_TO_PLAY = true;

	public static void main(String[] args) {
		Scanner myScanner = new Scanner(System.in);
		int searchDepth = readDepth(myScanner); // search depth
		int[][] board = readBoard(myScanner); // board with 1 for white, 0 for nobody, -1 for black
		System.out.println(minimax_value(board, WHITE_TO_PLAY, searchDepth, Float.NEGATIVE_INFINITY,
						   Float.POSITIVE_INFINITY));
	}

	// returns given search depth
	static int readDepth(Scanner s) {
		try {
			return Integer.parseInt(s.nextLine());
		} catch (Exception e) {
			System.err.println("Recall that first line of the input must be the search depth.");
			System.exit(0);
		}
		// satisfy compiler
		return 0;
	}

	// returns the given board state in int form
	static int[][] readBoard(Scanner s) {
		int [][] board = new int[NUM_COLUMNS][NUM_COLUMNS];
		for (int r = 0; r < NUM_COLUMNS; r++) {
			String line = s.nextLine();
			for (int c = 0; c < NUM_COLUMNS; c++) {
				if (line.charAt(c) == 'W') {
					board[r][c] = WHITE;
				} else if (line.charAt(c) == 'B') {
					board [r][c] = BLACK;
				} else if (line.charAt(c) == '-') {
					board [r][c] = NOBODY;
				} else {
					System.err.println("Badly formatted board; unrecognized token, " + line.charAt(c));
					System.exit(0);
				}
			}
		}
		return board;
	}

	// findWinner assumes the game is over
	static int findWinner(int[][] board) {
	  int whiteCount = 0;
	  int blackCount = 0;
	  for (int row = 0; row < NUM_COLUMNS; row++) {
		for (int col = 0; col < NUM_COLUMNS; col++) {
		  if (board[row][col] == WHITE) whiteCount++;
		  if (board[row][col] == BLACK) blackCount++;
		}
	  }
	  if (whiteCount > blackCount) {
		return WHITE;
	  } else if (whiteCount < blackCount) {
		return BLACK;
	  } else {
		return TIE;
	  }
	}

	static class Move {
	  int row;
	  int col;
  
	  Move(int r, int c) {
		row = r;
		col = c;
	  }
  
	  public boolean equals(Object o) {
		if (o == this) {
		  return true;
		}
	
		if (!(o instanceof Move)) {
		  return false;
		}
		Move m = (Move) o;
		return (m.row == row && m.col == col);
	  }
	}

	static ArrayList<Move> generateLegalMoves(int[][] board, boolean whiteTurn) {
	  ArrayList<Move> legalMoves = new ArrayList<Move>();
	  for (int row = 0; row < NUM_COLUMNS; row++) {
		for (int col = 0; col < NUM_COLUMNS; col++) {
		  if (board[row][col] != NOBODY) {
			continue;  // can't play in occupied space
		  }
		  // Starting from the upper left ...short-circuit eval makes this not terrible
		  if (capturesInDir(board,row,-1,col,-1, whiteTurn) ||
			  capturesInDir(board,row,-1,col,0,whiteTurn) ||    // up
			  capturesInDir(board,row,-1,col,+1,whiteTurn) ||   // up-right
			  capturesInDir(board,row,0,col,+1,whiteTurn) ||    // right
			  capturesInDir(board,row,+1,col,+1,whiteTurn) ||   // down-right
			  capturesInDir(board,row,+1,col,0,whiteTurn) ||    // down
			  capturesInDir(board,row,+1,col,-1,whiteTurn) ||   // down-left
			  capturesInDir(board,row,0,col,-1,whiteTurn)) {    // left
				legalMoves.add(new Move(row,col));
		  }
		}
	  }
	  return legalMoves;
	}

	// row_delta and col_delta are the direction of movement of the scan for capture
	static boolean capturesInDir(int[][] board, int row, int row_delta, int col, int col_delta, boolean whiteTurn) {
	  // Nothing to capture if we're headed off the board
	  if ((row+row_delta < 0) || (row + row_delta >= NUM_COLUMNS)) {
		return false;
	  }
	  if ((col+col_delta < 0) || (col + col_delta >= NUM_COLUMNS)) {
		return false;
	  }
	  // Nothing to capture if the neighbor in the right direction isn't of the opposite color
	  int enemyColor = (whiteTurn ? BLACK : WHITE);
	  if (board[row+row_delta][col+col_delta] != enemyColor) {
		return false;
	  }
	  // Scan for a friendly piece that could capture -- hitting end of the board
	  // or an empty space results in no capture
	  int friendlyColor = (whiteTurn ? WHITE : BLACK);
	  int scanRow = row + 2*row_delta;
	  int scanCol = col + 2*col_delta;
	  while ((scanRow >= 0) && (scanRow < NUM_COLUMNS) &&
			  (scanCol >= 0) && (scanCol < NUM_COLUMNS) && (board[scanRow][scanCol] != NOBODY)) {
		  if (board[scanRow][scanCol] == friendlyColor) {
			  return true;
		  }
		  scanRow += row_delta;
		  scanCol += col_delta;
	  }
	  return false;
	}

	// won't return the board to make clear it's not a copy
	static void capture(int[][] board, int row, int col, boolean whiteTurn) {
	  for (int row_delta = -1; row_delta <= 1; row_delta++) {
		for (int col_delta = -1; col_delta <= 1; col_delta++) {
		  if ((row_delta == 0) && (col_delta == 0)) {
			// the only combination that isn't a real direction
			continue;
		  }
		  if (capturesInDir(board, row, row_delta, col, col_delta, whiteTurn)) {
			// All our logic for this being valid just happened -- start flipping
			int flipRow = row + row_delta;
			int flipCol = col + col_delta;
			int enemyColor = (whiteTurn ? BLACK : WHITE);
			// No need to check for board bounds - capturesInDir tells us there's a friendly piece
			while(board[flipRow][flipCol] == enemyColor) {
			  // Take advantage of enum values and flip the owner
			  board[flipRow][flipCol] = -board[flipRow][flipCol];
			  flipRow += row_delta;
			  flipCol += col_delta;
			}
		  }
		}
	  }
	}

	//---------------

	// returns board state after one round
	static int[][] play(int[][] board, Move move, boolean whiteTurn) {
	  int[][] newBoard = copyBoard(board);
	  newBoard[move.row][move.col] = (whiteTurn ? WHITE : BLACK);
	  capture(newBoard, move.row, move.col, whiteTurn);
	  return newBoard;
	}

	// copies board (used by play(board, move, whiteTurn)
	static int[][] copyBoard(int[][] board) {
	  int[][] newBoard = new int[NUM_COLUMNS][NUM_COLUMNS];
	  for (int i = 0; i < NUM_COLUMNS; i++) {
		for (int j = 0; j < NUM_COLUMNS; j++) {
		  newBoard[i][j] = board[i][j];
		}
	  }
	  return newBoard;
	}

	// returns difference in piece count between white and black
	static float evaluationFunction(int[][] board) {
		int whiteCount = 0;
		int blackCount = 0;
		for (int row = 0; row < NUM_COLUMNS; row++) {
			for (int col = 0; col < NUM_COLUMNS; col++) {
				if (board[row][col] == WHITE) whiteCount++;
				if (board[row][col] == BLACK) blackCount++;
			}
		}

        return (float) whiteCount - blackCount;
	}


	// checks if game is over and finds winner
	static int checkGameOver(int[][] board) {
	  ArrayList<Move> whiteLegalMoves = generateLegalMoves(board, true);
	  if (!whiteLegalMoves.isEmpty()) {
		return NOBODY;
	  }
	  ArrayList<Move> blackLegalMoves = generateLegalMoves(board, false);
		if (!blackLegalMoves.isEmpty()) {
		return NOBODY;
	  }
	  // No legal moves, so the game is over
	  return findWinner(board);
	}
	//-------


	// minimax algorithm
	static float minimax_value(int board[][], boolean whiteTurn, int searchDepth, float alpha, float beta) {

		int isGameOver = checkGameOver(board);

		// if terminal, return winner
		if (isGameOver != 0) {
			// Black wins
			if (isGameOver == -1) {
				return -100;
			}
			// White wins
			if (isGameOver == 1) {
				return 100;
			}
			// Tie game
			if (isGameOver == 2) {
				return 0;
			}
		}

		// if search depth terminal
		if (searchDepth == 0) {
			return evaluationFunction(board);
		}

		float bestOutcome = Float.NEGATIVE_INFINITY;
		float worstOutcome = Float.POSITIVE_INFINITY;
		float Outcome = Float.POSITIVE_INFINITY;

		ArrayList<Move> moves = generateLegalMoves(board, whiteTurn); //generates list of moves

		if (moves.isEmpty()) {
			return minimax_value(board, !whiteTurn, searchDepth, alpha, beta);
		} else {
			for (Move node : moves) {
				int[][] nodeState = play(board, node, whiteTurn);
				float nodeOutcome = minimax_value(nodeState, !(whiteTurn), searchDepth - 1, alpha, beta);

				if (whiteTurn == true) {
					bestOutcome = Math.max(bestOutcome, nodeOutcome);
					if (bestOutcome >= beta) {
						return bestOutcome;
					}
					Outcome = bestOutcome;
					alpha = Math.max(alpha, bestOutcome);
				} else {
					worstOutcome = Math.min(worstOutcome, nodeOutcome);
					if (worstOutcome <= alpha) {
						return worstOutcome;
					}
					Outcome = worstOutcome;
					beta = Math.min(beta, worstOutcome);
				}
			}
		}
		return Outcome;
	}
}