// Name: Akshay Deepak Hegde
// USC NetID: hegdeaks
// CS 455 PA3
// Fall 2018


/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield), Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including  
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to moves the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.
  
  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from 
  outside this class via the getMineField accessor.  
 */
public class VisibleField {
   // ----------------------------------------------------------   
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method 
   // getStatus(row, col)).
   
   // Covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // Uncovered states (all non-negative values):
   
   // values in the range [0,8] corresponds to number of mines adjacent to this square
   
   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------   
  
   private MineField presentState;
   private int[][] closed;
   private boolean isFailed;
   private int guessed;
   private int explored;
   

   /**
      Create a visible field that has the given underlying mineField.
      The initial state will have all the mines covered up, no mines guessed, and the game
      not over.
      @param mineField  the minefield to use for for this VisibleField
    */
   public VisibleField(MineField mineField) {
	  presentState = mineField;
	  isFailed = false;
	  guessed = 0;
	  explored = 0;
      closed = new int[mineField.numRows()][mineField.numCols()];
      for(int i = 0; i < mineField.numRows(); i++) {
        for(int j = 0; j < mineField.numCols(); j++) {
          closed[i][j] = COVERED;
        }
      }
   }
   
   
   /**
      Reset the object to its initial state (see constructor comments), using the same underlying MineField. 
   */     
   public void resetGameDisplay() {
	  resetTheGame();
   }
  
   
   /**
      Returns a reference to the mineField that this VisibleField "covers"
      @return the minefield
    */
   public MineField getMineField() {
      return presentState;       
   }
   
   
   /**
      get the visible status of the square indicated.
      @param row  row of the square
      @param col  col of the square
      @return the status of the square at location (row, col).  See the public constants at the beginning of the class
      for the possible values that may be returned, and their meanings.
      PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {
      assert getMineField().inRange(row, col);
      return closed[row][col];
   }

   
   /**
      Return the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
      or not.  Just gives the user an indication of how many more mines the user might want to guess.  So the value can
      be negative, if they have guessed more than the number of mines in the minefield.     
      @return the number of mines left to guess.
    */
   public int numMinesLeft() {
	  int temp = getMineField().numMines();
      return temp - guessed;
   }
 
   
   /**
      Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
      changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
      changes it to COVERED again; call on an uncovered square has no effect.  
      @param row  row of the square
      @param col  col of the square
      PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {
      assert getMineField().inRange(row, col);
      if(getStatus(row, col) == QUESTION) {
        closed[row][col] = COVERED;
        }
      else if(getStatus(row, col) == COVERED) {
        closed[row][col] = MINE_GUESS;
        guessed++;
      }
      else if(getStatus(row, col) == MINE_GUESS) {
        closed[row][col] = QUESTION;
        guessed--;
      }      
   }

   
   /**
      Uncovers this square and returns false iff you uncover a mine here.
      If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in 
      the neighboring area that are also not next to any mines, possibly uncovering a large region.
      Any mine-adjacent squares you reach will also be uncovered, and form 
      (possibly along with parts of the edge of the whole field) the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS. 
      @param row  of the square
      @param col  of the square
      @return false   iff you uncover a mine at (row, col)
      PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {
      assert getMineField().inRange(row, col);
      if(getMineField().hasMine(row, col)) {
        explored++;
        closed[row][col] = EXPLODED_MINE;
        isFailed = true;
        return false;
      }
      else {
        recursiveDFS(row, col);
      }
      return true;
   }
 
   
   /**
      Returns whether the game is over.
      @return whether game over
    */
   public boolean isGameOver() {
	  return isGameDone();
   }
 
   
   /**
      Return whether this square has been uncovered.  (i.e., is in any one of the uncovered states, 
      vs. any one of the covered states).
      @param row of the square
      @param col of the square
      @return whether the square is uncovered
      PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      assert getMineField().inRange(row, col);
      int state = getStatus(row, col);
      if(state == COVERED || state == MINE_GUESS || state == QUESTION) {
        return false;
      }
      return true;
   }
   
   private void resetTheGame() {
	   getMineField().resetEmpty();
	   isFailed = false;
	   guessed = 0;
	   explored = 0;
	   for(int i = 0; i < closed.length; i++) {
		   for(int j = 0; j < closed[0].length; j++) {
			   closed[i][j] = COVERED;
	       }
	   }
   }
   
   private boolean isGameDone() {
	   int numMines = getMineField().numMines();
	   int rows = getMineField().numRows();
	   int cols = getMineField().numCols();
	   if(isFailed) {
		   for(int i = 0; i < rows; i++) {
			   for(int j = 0; j < cols; j++) {
				   if(getStatus(i, j) == COVERED && getMineField().hasMine(i, j))
					   closed[i][j] = MINE;
				   else if(getStatus(i, j) == MINE_GUESS && !getMineField().hasMine(i, j)) 
					   closed[i][j] = INCORRECT_GUESS;
	           }
	       }
		   return true;
	   }
	      
	   if(!isFailed && explored == rows * cols - numMines ) {
		   for(int i = 0; i < rows; i++) {
			   for(int j = 0; j < cols; j++) {
				   if(getStatus(i, j) == COVERED)
					   closed[i][j] = MINE_GUESS;
	           }
	       }
		   return true;
	   }
	   return false;
   }
   
   private void recursiveDFS (int row, int col) {
	  if(!getMineField().inRange(row, col))
	    return;
      if((getStatus(row, col) == MINE_GUESS) ||  getMineField().hasMine(row, col))
        return;
      if(isUncovered(row, col))
        return;
      
      closed[row][col] = getMineField().numAdjacentMines(row, col);
      explored++;
      
      if(getStatus(row, col) == 0) {
        recursiveDFS(row + 1, col + 1);
        recursiveDFS(row + 1, col - 1);
        recursiveDFS(row + 1, col);
        recursiveDFS(row - 1, col + 1);
        recursiveDFS(row - 1, col - 1);
        recursiveDFS(row - 1, col);
        recursiveDFS(row, col + 1);
        recursiveDFS(row, col - 1);
      }
    }
 
   
}
