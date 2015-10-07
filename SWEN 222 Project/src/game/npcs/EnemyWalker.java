package game.npcs;

import java.awt.Point;
import java.io.Serializable;

import game.DestinysWild;
import game.Player;
import game.Room;
import game.Tile;
import renderer.GameImagePanel;

public class EnemyWalker implements NPC,Serializable {
	private String type;
	private Point roomCoords; //room coords
	private Point realCoords; // real coords in respect to the window
	private int speed;
	private int damage;
	private String strategy;
	private Room currentRoom;
	private Tile currentTile;
	private int loopStep = 0;
	private int loopMaxY = 5; //Dimensions	  loop
	private int loopMaxX = 3; //		   of

	public EnemyWalker(String type, Point roomCoords, int speed, int damage, Room currentRoom){
		this.type = type;
		this.roomCoords = roomCoords;
		this.realCoords = GameImagePanel.calcRealCoords(roomCoords);
		this.currentRoom = currentRoom;
		this.currentTile = currentRoom.calcTile(realCoords);
		this.damage = damage;
		this.speed = speed;
		switch(type){
			case "bats":
				strategy = "follow";
				break;
			case "snail":
				strategy = "loop";
				break;
			default:
				System.out.println("Couldn't define strategy: Must be 'bats' or 'snail' type");
		}
	}

	/**
	 * tries to move this EnemyWalker
	 * @return boolean if move occurs
	 */
	public boolean tryMove(){
		switch(strategy){
			case "follow":
				return tryFollow();
			case "loop":
				return tryLoop();
			default:
				System.out.println("No strategy defined..");
				return false;
		}
	}

	/**
	 * Tries to follow the nearest player
	 * @return boolean if can move
	 */
	public boolean tryFollow(){
		Player nearestPlayer = null;
		for(Player player : DestinysWild.getBoard().getPlayers()){
			if(nearestPlayer == null || player.getCoords().distance(realCoords) < nearestPlayer.getCoords().distance(realCoords)){
				nearestPlayer = player;
			}
		}
		if(nearestPlayer.getCoords().x > realCoords.x){
			if(nearestPlayer.getCoords().y > realCoords.y){
				realCoords.translate(speed, speed);
			}
			else{
				realCoords.translate(speed, -speed);
			}
			return true;
		}
		else if(nearestPlayer.getCoords().y > realCoords.y){
			realCoords.translate(-speed, speed);
			return true;
		}
		else if(!(nearestPlayer.getCoords().equals(realCoords))){
			realCoords.translate(-speed, -speed);
			return true;
		}
		return false; //Doesn't need to move if on top of player
	}

	/**
	 * Tries to move this walker in a loop
	 * @return boolean if can move
	 */
	public boolean tryLoop(){
		if(loopStep < loopMaxY-1){
			//move north
			realCoords.translate(-speed, -speed);
		}
		else if(loopStep >= loopMaxY-1 && loopStep < (loopMaxY-1)+(loopMaxX-1)){
			//move east
			realCoords.translate(speed, -speed);
		}
		else if(loopStep >= (loopMaxY-1)+(loopMaxX-1) && loopStep < ((loopMaxY-1)*2) + (loopMaxX-1)){
			//move south
			realCoords.translate(speed, speed);
		}
		else{
			//move west
			realCoords.translate(-speed, speed);
		}
		
		if(!currentRoom.calcTile(realCoords).equals(currentTile)){
			currentTile = currentRoom.calcTile(realCoords);
			if(loopStep == ((loopMaxY*2-2) + (loopMaxX*2-2))){
				loopStep = 0;
			}
			else{
				loopStep++;
			}
		}
		return true;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}


	/**
	 * @return the strategy
	 */
	public String getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy the strategy to set
	 */
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	/**
	 * @return the currentRoom
	 */
	public Room getCurrentRoom() {
		return currentRoom;
	}

	/**
	 * @param currentRoom the currentRoom to set
	 */
	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}

	/**
	 * @return the currentTile
	 */
	public Tile getCurrentTile() {
		return currentTile;
	}

	/**
	 * @param currentTile the currentTile to set
	 */
	public void setCurrentTile(Tile currentTile) {
		this.currentTile = currentTile;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Point getRealCoords() {
		return realCoords;
	}

	public void setRealCoords(Point coords) {
		this.realCoords = coords;
	}

	/**
	 * returns this npc's room coords
	 */
	public Point getRoomCoords() {
		return roomCoords;
	}

	/**
	 * sets this npc's room coords
	 * @param coords to be set
	 */
	public void setRoomCoords(Point coords) {
		this.roomCoords = coords;
	}

	public String toString(){
		return "enemywalker";
	}
}
