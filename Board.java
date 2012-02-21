/*
 * Noah Alonso-Torres
 * Othello Board Games (Reversi)
 * Board Class
 */

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Board {

	private static int BLACK = -1, WHITE = 1, EMPTY = 0;
	private int difficulty = 1, win = 0, turn = 0, value = 0, whiteCorners = 0, blackCorners = 0;
	private LinkedList<Point> moves = new LinkedList<Point>();
	private int blackPieces = 0, whitePieces = 0;
	
	public LinkedList<Board> children = new LinkedList<Board>();
	
	private int[][] grid = new int[8][8];
	
	public Board() {}
	
	public void start(int diff) {
		grid[3][3] = WHITE;
		grid[4][3] = BLACK;
		grid[3][4] = BLACK;
		grid[4][4] = WHITE;
		turn = BLACK;
		this.difficulty = diff;
		scanMoves(); //corners are empty
		boardValue();
	}
	
	private void scanMoves() {
		moves = new LinkedList<Point>(); 
		blackPieces = 0;
		whitePieces = 0;
		for (int col = 0; col < 8; col++)
            for (int row = 0; row < 8; row++) {
            	if (grid[col][row] == BLACK) blackPieces += 1; 
            	if (grid[col][row] == WHITE) whitePieces += 1; 
            	
            	if (grid[col][row] != EMPTY) ; //2.a cant make a move
            	if (grid[col][row] == EMPTY) { //2.b
            		for (int i = 1; i <= 8; i++) {
            			Point current = new Point(col,row);
            			Point direction = directions(i,current);
            			int newCol = direction.x;
            			int newRow = direction.y;
            			if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) ; //2bi
            			else if (grid[newCol][newRow] == EMPTY) ; //2bi
            			else if (grid[newCol][newRow] == turn) ; //2bii
            			else { //its opposite
            				while (grid[newCol][newRow] == turn*-1) {
            					Point step = new Point(newCol,newRow);
            					Point steptaken = directions(i,step);
            					newCol = steptaken.x;
            					newRow = steptaken.y;
            					if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) break;
            				}
        					if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) ;
        					else if (grid[newCol][newRow] == EMPTY) ;
        					else if (!checkMove(col,row)) moves.add(new Point(col,row)); //its the same as current
            			}
            		}
            	}
            }
	}
	
	private void convert(Point current) { //changes in-betweens
		LinkedList<Point> conversions = new LinkedList();
		LinkedList<Point> temp = new LinkedList();
		
		for (int i = 1; i <= 8; i++) { //direction loop 
			Point direction = directions(i,current);
			int newCol = direction.x;
			int newRow = direction.y;
			if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7 
					|| grid[newCol][newRow] == EMPTY || grid[newCol][newRow] == turn) ;
			else {
				while (grid[newCol][newRow] == turn*-1) {
					Point step = new Point(newCol,newRow);
					Point stepTaken = directions(i,step);
					temp.add(step);
					newCol = stepTaken.x;
					newRow = stepTaken.y;
					if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7) break;
				}
				if (newRow < 0 || newRow > 7 || newCol < 0 || newCol > 7 || grid[newCol][newRow] == EMPTY)
					temp = new LinkedList();
				else { //its the same as current
					Iterator<Point> it = temp.iterator();
					while(it.hasNext()) conversions.add(it.next());
				}
			}
		}
		Iterator<Point> it = conversions.iterator();
		while(it.hasNext()) {
			Point point = it.next();
			grid[point.x][point.y] = turn;
		}
	}
	
	private Point directions(int direction, Point coord) {
		switch (direction) {
		case 1: return new Point(coord.x-1,coord.y-1); //top left
		case 2: return new Point(coord.x,coord.y-1);   //mid left
		case 3: return new Point(coord.x+1,coord.y-1); //bottom left
		case 4: return new Point(coord.x+1,coord.y);   //bottom center
		case 5: return new Point(coord.x+1,coord.y+1); //bottom right
		case 6: return new Point(coord.x,coord.y+1);   //mid right
		case 7: return new Point(coord.x-1,coord.y+1); //top right
		case 8: return new Point(coord.x-1,coord.y);   //mid top
		}
		return new Point(0,0);
	}
	
	public void processMove(int[][] grid, Point move) { 
		this.grid = grid;
		convert(move); //changes in-betweens
		turn *= -1; //change turn
		scanMoves(); //new set of moves
		checkCorners();
		boardValue();
		if (moves.isEmpty()) { //other player passes, change to your turn
			turn *= -1; //change turn
			scanMoves(); //new set of moves
			checkCorners();
			boardValue();
			if (moves.isEmpty()) { //
				win = 1; //cant move, because no moves available. newsfeed updates.
			}
		}
	}
	
	public void clone(int[][] grid, int turn, int difficulty) {
		this.grid = grid;
		this.turn = turn;
		this.difficulty = difficulty;
		children = new LinkedList<Board>();
		scanMoves(); // also counts pieces
		checkCorners();
		boardValue();
	}
	
	private Point[] corners = { new Point(0,0), new Point(7,0), new Point(0,7), new Point(7,7) };
	
	public void boardValue() {
		if (difficulty == 1)
			value = blackPieces - whitePieces + blackCorners*2 - whiteCorners*2;
		if (difficulty == 2) ;
			value = blackPieces - whitePieces + blackCorners*2 - whiteCorners*2;
		if (difficulty == 3) {
			int blackValue = 0, whiteValue = 0;
			int [][] strat = strategy(difficulty);
			
			for (int col = 0; col < grid.length; col++)
				for (int row = 0; row < grid.length; row++) {
					if (grid[col][row] == BLACK) blackValue += strat[col][row];
					if (grid[col][row] == WHITE) whiteValue += whiteValue*strat[col][row];
				}
			value = blackValue - whiteValue;
		}
		if (turn == WHITE) value*=-1;
	}
	
	public void checkCorners() {
		for (int i = 0; i < corners.length; i++) {
			if (grid[corners[i].x][corners[i].y] == WHITE) whiteCorners++;
			if (grid[corners[i].x][corners[i].y] == BLACK) blackCorners++;
		}
	}
	
	public boolean checkMove(int col, int row) {
		Point tempMove = new Point(col,row);
		Iterator<Point> it = moves.iterator();
		while(it.hasNext()) if (it.next().equals(tempMove)) return true;
		return false;
	}
	
	public int[][] strategy(int index) {
		int[][] strat = new int[8][8];
		switch (index) {
		case 3: {
			strat = new int[][] {
				{100, -25, 10, 5, 5, 10, -25, 100,},
				{-25, -25,  1, 1, 1,  1, -25, -25,},
				{ 10,   1,  5, 2, 2,  5,   1,  10,},
				{  5,   1,  2, 1, 1,  2,   1,   5,},
				{  5,   1,  2, 1, 1,  2,   1,   5,},
				{ 10,   1,  5, 2, 2,  5,   1,  10,},
				{-25, -25,  1, 1, 1,  1, -25, -25,},
				{100, -25, 10, 5, 5, 10, -25, 100,},
			};
			return strat;
		}
		}
		int[][] fake = {
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
				{0,0,0,0,0,0,0,0},
		};
		return fake;
	}
	
	//Getter Get info
	public int[][] getGrid() { return grid; }
	public int getTurn() { return turn; }
	public LinkedList<Point> getMoves() { return moves; }
	public int getBlack() { return blackPieces; }
	public int getWhite() { return whitePieces; }
	public int getWin() { return win; }
	public int getValue() { return value; }
	public int getDifficulty() { return difficulty; }
	
	public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
}

