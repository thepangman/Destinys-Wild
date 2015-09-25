package clientServer;

import game.Board;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class Slave extends Thread implements KeyListener  {

	private final Socket socket;
	private Board game;
	private DataOutputStream output;
	private DataInputStream input;
	private int uid;
	private int totalSent;


	/**
	 * Similar to Pacman. Slave doesn't actually process any computation, other
	 * than updating the display and refreshing it when it receives data
	 * from the master connection.
	 */
	public Slave(Socket socket) {
		this.socket = socket;
	}

	public void run(){
		try{
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		}
		catch(IOException ex){

		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//UNUSED, left due to implementation requirements
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		//UNUSED, left due to implementation requirements
	}

}
