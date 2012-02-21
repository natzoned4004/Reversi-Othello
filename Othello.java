/*
 * Noah Alonso-Torres
 * Othello Board Game (Reversi)
 * Game Class
 */


import javax.swing.*; 

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Othello extends JComponent implements MouseListener, ActionListener {

	private JFrame frame; //
	private JLabel info = new JLabel("");
	private JMenuItem newGame = new JMenuItem("New Game");
	//private JMenuItem switchSides = new JMenuItem("Switch");
	private JMenuItem oneP  = new JMenuItem("Player vs. Comp");
	private JMenuItem twoP = new JMenuItem("Player vs. Player");
	private JMenuItem noP = new JMenuItem("Comp vs. Comp");
	private JMenuItem exit = new JMenuItem("Exit");
	private JMenuItem easy = new JMenuItem("Easy");
	private JMenuItem moderate = new JMenuItem("Moderate");
	private JMenuItem hard = new JMenuItem("Hard");
	
	private int players = 1, difficulty = 1;
	
	private static int CELL_SIZE = 65;
	private static int BOARD_SIZE = CELL_SIZE*8;
	private Board board = new Board();
	private static int BLACK = -1, WHITE = 1, EMPTY = 0;
	
	public Othello() {
		board.start(difficulty);
		newsfeed();
		frame = new JFrame("Othello");
		frame.getContentPane().setLayout(new BorderLayout());
		setPreferredSize(new Dimension(BOARD_SIZE,BOARD_SIZE));
		frame.add(this, BorderLayout.CENTER); //with the extends JComponent in place, this paints
		frame.add(info, BorderLayout.SOUTH);
		
		
		JMenuBar menuBar = new JMenuBar();
	    frame.setJMenuBar(menuBar);
	    
	    JMenu file = new JMenu("File");
	    file.add(newGame);
	    //file.add(switchSides);
	    file.add(exit);
	    
	    JMenu difficulty = new JMenu("Player vs. Comp");
	    difficulty.add(easy);
	    difficulty.add(moderate);
	    difficulty.add(hard);
	    
	    JMenu gameType = new JMenu("Game Type");
	    gameType.add(difficulty);
	    gameType.add(twoP);
	    gameType.add(noP);
	    
        menuBar.add(file);
        menuBar.add(gameType);
        
		newGame.addActionListener(this);
		//switchSides.addActionListener(this);
		exit.addActionListener(this);
		
		oneP.addActionListener(this);
		twoP.addActionListener(this);
		noP.addActionListener(this);
		
		easy.addActionListener(this);
		moderate.addActionListener(this);
		hard.addActionListener(this);
		
		addMouseListener(this);
		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == newGame) newGame();
		//if (event.getSource() == switchSides) ; // EMPTY
		if (event.getSource() == exit) System.exit(0);
		
		if (event.getSource() == twoP) twoP();
		if (event.getSource() == noP) noP();
		
		if (event.getSource() == easy) oneP(1);
		if (event.getSource() == moderate) oneP(2);
		if (event.getSource() == hard) oneP(3);
	}
	public void newGame() {
		board = new Board();
		board.start(difficulty);
		repaint();
		newsfeed();
	}
	public void oneP(int diff) {
		players = 1;
		difficulty = diff;
		newGame();
	}
	public void twoP() {
		players = 2;
		difficulty = 0;
		newGame();
	}
	public void noP() {
		players = 3;
		difficulty = 3;
		newGame(); // 3 is difficulty
		auto();
	}
	
	public void mousePressed(MouseEvent event) {
		Point move = new Point(event.getX()/CELL_SIZE,event.getY()/CELL_SIZE);
		
		if (move.y < 8 && move.x < 8) { //resolves out-of-bounds
			int[][] temp = new int[8][8];
			for (int gCol = 0; gCol < 8; gCol++)
	            for (int gRow = 0; gRow < 8; gRow++)
	            	temp[gCol][gRow] = board.getGrid()[gCol][gRow];
			temp[move.x][move.y] = board.getTurn();  //made turn on temp grid
			
			if (players == 2) {
				if(!board.checkMove(move.x,move.y)) return;
				else {
					board.processMove(temp,move);
					repaint();
					newsfeed();
				}
			}
			if (players == 1) {
				if(!board.checkMove(move.x,move.y)) return;
				else {
					System.out.println("---------------------------BLACK MOVED--------------------------");
					int turn = board.getTurn(); //this will be human's
					board.processMove(temp, move);
					repaint();
					newsfeed();

					if (board.getTurn() == turn) return; //if its human again, return
					while (board.getTurn() == -1*turn && board.getWin() == 0) { //if its comp turn, or it is comp again comp goes
						brainBlast();
						newsfeed();
						repaint();
					}	
				}
			}
			if (players == 3) auto();
		}
		else return;
	}
	
	public void auto() {
			int turn = board.getTurn(); //this will be human's
			while (board.getTurn() == turn && board.getWin() == 0) { //if its comp turn, or it is comp again comp goes
				System.out.println("---------------------------BLACK MOVED--------------------------");
				brainBlast();
				newsfeed();
				repaint();
			}
			if (board.getTurn() == turn) return; //if its human again, return
			while (board.getTurn() == -1*turn && board.getWin() == 0) { //if its comp turn, or it is comp again comp goes
				System.out.println("---------------------------WHITE MOVED--------------------------");
				brainBlast();
				newsfeed();
				repaint();
			}
			if (board.getWin() == 0) auto();
	}
	
	public void brainBlast() {
		int queueCount = 1, MAX = 80*1000;
		
		Board copy = new Board();
		copy.clone(board.getGrid(), board.getTurn(), difficulty);
		Board root = copy;
		
		Queue<Board> queue = new LinkedList<Board>();
		queue.add(copy);
		
		while (!queue.isEmpty() && (queueCount < MAX)) {
			Board tempBoard = queue.remove(); //the point of board-temp is to remove once
			Iterator<Point> moveSet = tempBoard.getMoves().iterator(); //temp's Moves (first time its white's turn)
			
			while (moveSet.hasNext()) { //while there are moves
				Point move = moveSet.next(); //current iteration (a move on the list)
				Board clone = new Board();
				clone.clone(tempBoard.getGrid(), tempBoard.getTurn(), difficulty); //successfully created clone of queue board
				
				int[][] quick = new int[8][8]; //set up for "process move" (grid required)
				for (int gCol = 0; gCol < 8; gCol++)
		            for (int gRow = 0; gRow < 8; gRow++)
		            	quick[gCol][gRow] = clone.getGrid()[gCol][gRow];
				quick[move.x][move.y] = clone.getTurn();  //made turn on "quick" grid
				
				clone.processMove(quick, move); //made turn on "clone" (no longer resembles temp)
				
				tempBoard.children.add(clone);
				queue.add(clone);
				queueCount++;
			}
			if (queueCount >= MAX) System.out.println("Tree Depth:" + queueCount); //testing
		} //end of tree building, seems fine. DEBUGGED
		
		Iterator<Board> level1 = root.children.iterator(); //level 1 iterator (roots children)
		int rootCV = choiceValue(root);                    //so that we only call cV() once
		System.out.println("Root CV: " + rootCV);
		
		//find the move and execute
		              //while (level1.hasNext()) System.out.println(choiceValue(level1.next()));
		
		
		int moveIndex = 0; //this will count the boards (which will determine index of move in root)
		while (level1.hasNext()) {
			Board next = level1.next(); //board with move made on it (FIRST PARAMETER)
			
			int cVN = choiceValue(next);
			if (next.getTurn() != root.getTurn()) cVN*=-1;
			
			System.out.println("Move " + moveIndex + ": " + cVN); //Test line
			
			if (cVN == rootCV) {
				Iterator<Point> moves = root.getMoves().iterator(); //go back to the root and get list
				int locator = 0; //counts moves
				Point move = new Point(0,0); //filler
				
				while (moves.hasNext()) {
					Point temp = moves.next();
					if (locator == moveIndex) {
						move = temp;
						break;
					}
					locator++;
				}
				board.processMove(next.getGrid(), move); //make the move
				System.out.println("Move Made! at: " + move);
				return;
			}
			moveIndex += 1;
		}
		System.out.println("FAILURE--------------------------------------------");
	}

	public int choiceValue (Board root) {
		int choiceValue = 0;
		if (root.children.isEmpty()) { //then its a leaf
			choiceValue = root.getValue(); //it will return the adjusted choiceValue
		}
		else {
			Iterator<Board> boards = root.children.iterator();
			int greatestChildCV = -999;
			
			while (boards.hasNext()) {
				Board current = boards.next(); //next child in sequence
				int cCV = choiceValue(current); //currentChoiceValue
				if (root.getTurn() != current.getTurn()) cCV *= -1;

				if (cCV > greatestChildCV) {
					greatestChildCV = cCV;
					//cVhelper = current;
				}
			}
			choiceValue = greatestChildCV;
		}
		return choiceValue;
	}
	
	public void newsfeed() {
		// Turn: Human, Human = 2, Computer = 1;
		String turn;
		if (board.getTurn() == BLACK) turn = "Black";
		else turn = "White";
		String usual = "     Black: " + board.getBlack() + "     White: " + board.getWhite() + 
			"      Moves: " + board.getMoves().size();
		String first = "Difficulty: " + board.getDifficulty() + "     Turn: " + turn;
		if (board.getWin() == 1) {
			if (board.getBlack() > board.getWhite())
				info.setText("Black Wins!" + usual);
			else if (board.getBlack() < board.getWhite())
				info.setText("White Wins!" + usual);
			else info.setText("Draw." + usual);
		}
		else info.setText(first + usual);
	}
	
	public void drawPiece(Graphics graphics, int col, int row) {
        if (board.getGrid()[col][row] == BLACK) graphics.setColor(Color.BLACK);
        if (board.getGrid()[col][row] == WHITE) graphics.setColor(Color.WHITE);
        graphics.fillOval(col*CELL_SIZE, row*CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }
	
	public void drawMove(Graphics graphics, int col, int row) {
		graphics.setColor(Color.GREEN);
		graphics.fillRect(col*CELL_SIZE + CELL_SIZE/2 - 3, row*CELL_SIZE + CELL_SIZE/2 - 3, 10, 10);
	}
	
	public void paintComponent(Graphics graphics) {
    	graphics.setColor(Color.DARK_GRAY); 
    	graphics.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE); //Board Background
        graphics.setColor(Color.GRAY);
        for (int i = 0; i < 8; ++i) // vertical grid lines
            graphics.drawLine(i*CELL_SIZE, 0, i*CELL_SIZE, 8*CELL_SIZE);
        for (int i=0; i < 8; ++i) { // horizontal grid lines
            graphics.drawLine(0, i*CELL_SIZE, 8*CELL_SIZE, i*CELL_SIZE);
        }
        
        for (int col = 0; col < 8; col++)
            for (int row = 0; row < 8; row++)
                if (board.getGrid()[col][row] != EMPTY) drawPiece(graphics, col, row);
        
        Iterator<Point> it = board.getMoves().iterator();
		while(it.hasNext()) {
			Point coord = it.next();
			drawMove(graphics, coord.x, coord.y);
		}
    }
	
	public void mouseEntered (MouseEvent event) { /* ignore */ }
	public void mouseExited  (MouseEvent event) { /* ignore */ }
	public void mouseClicked (MouseEvent event) { /* ignore */ }
	public void mouseReleased(MouseEvent event) { /* ignore */ }
	
	public static void main(String[] args) {
		new Othello();
	}
}
