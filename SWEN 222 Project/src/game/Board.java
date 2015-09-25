package game;

import java.awt.Canvas;

import javax.swing.JOptionPane;

import clientServer.GameClient;
import clientServer.GameServer;

public class Board extends Canvas implements Runnable {

	private Room[][] board = new Room[5][5];
	private GameClient client;
	private GameServer server;
	private boolean running = false;
	public int tickCount = 0;

	public Board() {

	}

	/**
	 * @return the board
	 */
	public Room[][] getBoard() {
		return board;
	}

	public void addRoom(Room room, int x, int y) {
		board[x][y] = room;
	}

	/**
	 * This method should provide a basic, text based look at the board in its
	 * current state.
	 */
	public void printBoard() {
		for (int i = 0; i < board.length; i++) {
			System.out.print("| ");
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] == null) {
					System.out.print("null | ");
				} else {
					System.out.print(board[i][j].getID() + " | ");
				}
			}
			System.out.println();
		}
	}

	

}
