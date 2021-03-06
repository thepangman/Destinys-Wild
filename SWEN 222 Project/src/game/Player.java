package game;

import java.awt.Point;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import clientServer.packets.TorchPacket;
import game.items.Health;
import game.items.Item;
import game.items.Key;
import game.items.Score;
import game.items.Tool;
import game.npcs.EnemyStill;
import game.npcs.EnemyWalker;
import game.npcs.NPC;

/**
 * A Player is the character controlled by the user.
 * @author Rob
 *
 */
public class Player implements Serializable {
	private String name;
	private Point coords; //Coords relative to the game window
	private int health = 100;
	private Room currentRoom;
	private List<Room> visitedRooms = new ArrayList<>();
	private List<Item> inventory = new ArrayList<>();
	private int score = 0;
	private int speed = 4;
	private String orientation = "north";
	private Tile currentTile;
	private Tile prevTile;
	private boolean isMoving;
	private boolean north;
	private boolean south;
	private boolean east;
	private boolean west;
	private int numKeyPieces = 0;
	private boolean allowGate = false;

	private int walkState = 0;
	private int walkDelay = 0;
	private boolean invincible;
	private boolean hasTorch = false;
	private int invincibleCount = 60;

	public Player(){

	}

	/**
	 * Constructor for new players
	 * @param name name of player
	 * @param coords coords of player
	 * @param currentRoom starting room for the player
	 */
	public Player(String name, Point coords, Room currentRoom){
		this.name = name;
		this.coords = coords;
		this.currentRoom = currentRoom;
		this.currentTile = currentRoom.calcTile(coords);
		addCurrentRoom();
	}

	/**
	 * Constructor for loading existing player. This will be necessary
	 * because players may load in previous games.
	 * @param name name of the player
	 * @param coords location of the player in terms of the game window
	 * @param health player's health
	 * @param currentRoom room player is currently in
	 * @param visitedRooms rooms player has visited
	 * @param inventory list of items the player currently has
	 * @param score current score of the player
	 */
	public Player(String name, Point coords, int health,
			Room currentRoom, List<Room> visitedRooms, List<Item> inventory, int score){
		this.name = name;
		this.coords = coords;
		this.health = health;
		this.currentRoom = currentRoom;
		this.visitedRooms = visitedRooms;
		this.inventory = inventory;
		this.score = score;
		this.currentTile = currentRoom.calcTile(coords);
		this.port = -1;
		this.ipAddress = null;
	}

	/**
	 * Checks whether this player has the corresponding tool for a given breakable obstacle
	 * @param breakable obstacle
	 * @return boolean has tool or not
	 */
	public boolean hasTool(String breakable){
		for(Item item : inventory){
			if(item instanceof Tool && (((Tool)item).getBreakable().equals(breakable)) || breakable.equals("steelbeams")){
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the player's current room
	 * @param room to be set
	 */
	public void setRoom(Room room){
		this.currentRoom = room;
	}

	/**
	 * Gets whether the player has a torch or not
	 * @return boolean has torch or not
	 */
	public boolean getHasTorch(){
		return this.hasTorch;
	}

	/**
	 * sets whether the player has a torch or not
	 * @param torch to be set
	 */
	public void setHasTorch(boolean torch){
		this.hasTorch = torch;
	}

	/**
	 * Tries to interact with anything within the surround 4 tiles (N E S or W)
	 * @return boolean whether interaction is successful or not
	 */
	public boolean tryInteract(){
		for(Object occupant : getInteractables()){
			if(occupant != null){
				((Interactable)occupant).interact();
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the player's walk cycle. Used for rendering the player movement animations
	 */
	public void updateWalkCycle(){
		walkState++;
		if(walkState == 8){
			walkState = 0;
		}
	}

	/**
	 * gets the player's walk state
	 * @return int walk state
	 */
	public int getWalkState(){
		return walkState;
	}

	/**
	 * finds any interactables in the surrounding squares of the player
	 * @return List<Object> of interactables
	 */
	public List<Object> getInteractables(){
		int currTileRow = currentTile.getRoomCoords().x;
		int currTileCol = currentTile.getRoomCoords().y;

		List<Object> occupants = new ArrayList<>();

		Tile north = currentRoom.getTileFromRoomCoords(new Point(currTileRow-1, currTileCol));
		Tile east = currentRoom.getTileFromRoomCoords(new Point(currTileRow, currTileCol+1));
		Tile south = currentRoom.getTileFromRoomCoords(new Point(currTileRow+1, currTileCol));
		Tile west = currentRoom.getTileFromRoomCoords(new Point(currTileRow, currTileCol-1));
		Tile current = currentTile;

		Object occupant;

		//Special case for locked gate at home
		if(currentRoom.getId() == 0 && (currentTile.getRoomCoords().equals(new Point(0, 4)) || currentTile.getRoomCoords().equals(new Point(0, 5)))){
			if(hasKey()){
				DestinysWild.startTalking("You've unlocked the gate!!!");
				for(Player player : DestinysWild.getBoard().getPlayers()){
					player.setAllowGate(true);

				}
			}
			else{
				DestinysWild.startTalking("Come back when you have the complete key!");
			}
		}
		//------------------------------------

		if(north != null && north.isOccupied()){
			occupant = currentRoom.getTileOccupant(north);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(east != null && east.isOccupied()){
			occupant = currentRoom.getTileOccupant(east);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(south != null && south.isOccupied()){
			occupant = currentRoom.getTileOccupant(south);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(west != null && west.isOccupied()){
			occupant = currentRoom.getTileOccupant(west);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		if(current != null && current.isOccupied()){
			occupant = currentRoom.getTileOccupant(current);
			if(occupant instanceof Interactable){
				occupants.add(occupant);
			}
		}
		return occupants;
	}

	/**
	 * Checks whether the player has a complete key or not
	 * @return boolean has key or not
	 */
	public boolean hasKey(){
		for(Item item : inventory){
			if(item instanceof Key && item.getId() == 5){
				return true;
			}
		}
		return false;
	}

	/**
	 * sets whether this player is allowed through the end gate or not
	 */
	public void setAllowGate(boolean allow){
		allowGate = allow;
	}

	/**
	 * gets the state of allowGate for this player
	 * @return boolean allowGate
	 */
	public boolean getAllowGate(){
		return allowGate;
	}

	/**
	 * Updates the Player. Including inviniciblity, animation state and movement
	 */
	public void updatePlayer(){
		updateInvincibility();
		int count = 0;
		if(north){
			tryMove("north");
			count++;
		}
		if(south){
			tryMove("south");
			count++;
		}
		if(east){
			tryMove("east");
			count++;
		}
		if(west){
			tryMove("west");
			count++;
		}
		if(count > 0){
			if(walkDelay == 0){
				updateWalkCycle();
				walkDelay = 2;
			}
			else{
				walkDelay--;
			}
		}
		else{
			walkDelay = 2;
			walkState = 0;
		}
	}

	/**
	 * The Player becomes invincible for a certain time after taking damage. This method updates the timer
	 */
	public void updateInvincibility(){
		if(invincible){
			invincibleCount--;
			if(invincibleCount == 0){
				invincible = false;
				invincibleCount = 60;
			}
		}
	}

	/**
	 * Makes the player take the given amount of damage and handles death if they are to be so unfortunate
	 * @param damage to be taken
	 */
	public void takeDamage(int damage){
		if(!invincible){
			setHealth(getHealth()-damage);
			invincible = true;
			if(!checkPulse()){
				partThisCruelWorldForAnother();
			}
		}
	}

	/**
	 * After a player has stepped onto a tile, this method calculates whether to push the player
	 * back or whether to pick up an item or whether to do nothing.
	 * @return whether the player can move onto the next tile or not. False = cannot change tile
	 */
	public boolean canChangeTile(){
		if(!currentTile.isOccupied()){
			return true;
		}
		//tile is occupied
		Object occupant = currentRoom.getTileOccupant(currentTile);
		if(occupant instanceof Item){
			if(addInventoryItem((Item)occupant)){
				currentRoom.removeItems((Item)occupant);
				if(occupant instanceof Score){
					setScore(getScore() + ((Item)occupant).getScore());
				}
				else{
					DestinysWild.getBoard().addOffBoardItem((Item)occupant);
				}
			}
			return true;
		}
		else if(occupant instanceof EnemyWalker || occupant instanceof EnemyStill){
			takeDamage(((NPC)occupant).getDamage());
			return (occupant instanceof EnemyWalker);
		}
		return false;
	}

	/**
	 * Checks whether the player is still alive
	 * @return boolean true if pulse is found
	 */
	public boolean checkPulse(){
		if(getHealth() <= 0){
			return false;
		}
		return true;
	}

	/**
	 * The player has died. Reinitialises everything appropriately.
	 */
	public void partThisCruelWorldForAnother(){
		resetInventory();
		setCurrentRoom(DestinysWild.getBoard().getRoomFromCoords(2, 2));
		setCoords(540, 325);
		setScore((int)(getScore()*0.6));
		setHealth(100);
		DestinysWild.startTalking("Oh dear, you have died! Your medical bill was $" + ((int)((getScore()/0.6)-getScore())));
	}

	/**
	 * resets the player's inventory accordingly upon death
	 */
	public void resetInventory(){
		List<Item> toRemove = new ArrayList<>();
		for(Item item : inventory){
//			if(item instanceof Key && item.getId() == 5){
//				getCurrentRoom().addItem(item, prevTile.getRoomCoords().x, prevTile.getRoomCoords().y);
//				((Key)item).setCoords(new Point(prevTile.getRoomCoords()));
//				toRemove.add(item);
//			}
			if(item instanceof Health){
				toRemove.add(item);
			}
		}
		for(Item item : toRemove){
			inventory.remove(item);
		}
	}

	/**
	 * Attempts to heal the player upon selection of a health item from the inventory
	 * @param itemId item to eat
	 * @return boolean success
	 */
	public boolean tryEat(int itemId){
		Item healthItem = null;
		for (Item item : inventory){
			if (item.getId() == itemId){
				healthItem = item;
			}
		}
		if (healthItem != null){
			if (getHealth() == 100){
				return false;
			}
			else{
				if (getHealth() + healthItem.getHealth() < 100){
					setHealth(getHealth() + healthItem.getHealth());
				}
				else {
					//don't want player to have over 100 health
					setHealth(100);
				}
				inventory.remove(healthItem);
				return true;

			}
		}
		return false;
	}

	/**
	 * Where the game logic player movement is done. The player will be moved onto a tile,
	 * then that tile is tested for an occupant. If occupied, the movement is reversed.
	 * @param direction the direction the player is trying to move
	 * @return boolean whether the player move is successful or not
	 */
	public boolean tryMove(String direction){
		orientation = direction;
		prevTile = currentTile;
		switch(direction){
			case "north":
				setCoords(getCoords().x, getCoords().y - speed/2);
				currentTile = currentRoom.calcTile(coords);
				if(!currentRoom.currTileIsInRoom(currentTile) && prevTile.isDoorMat().equals("north")){
					if(currentRoom.getId() == 0 && !allowGate){
						setCoords(getCoords().x, getCoords().y + speed/2);
						currentTile = prevTile;
						DestinysWild.startTalking("You must unlock this door first!");
					}
					else{
						currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getNorth());
						changeRoom(prevTile);
					}
				}
				else if(!currentRoom.currTileIsInRoom(currentTile) || !canChangeTile()){
					setCoords(getCoords().x, getCoords().y + speed/2);
					currentTile = prevTile;
				}
				break;
			case "east":
				setCoords(getCoords().x + speed, getCoords().y);
				currentTile = currentRoom.calcTile(coords);
				if(!currentRoom.currTileIsInRoom(currentTile) && prevTile.isDoorMat().equals("east")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getEast());
					changeRoom(prevTile);
				}
				else if(!currentRoom.currTileIsInRoom(currentTile) || !canChangeTile()){
					setCoords(getCoords().x - speed, getCoords().y);
					currentTile = prevTile;
				}
				break;
			case "south":
				setCoords(getCoords().x, getCoords().y + speed/2);
				currentTile = currentRoom.calcTile(coords);
				if(!currentRoom.currTileIsInRoom(currentTile) && prevTile.isDoorMat().equals("south")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getSouth());
					changeRoom(prevTile);
				}
				else if(!currentRoom.currTileIsInRoom(currentTile) || !canChangeTile()){
					setCoords(getCoords().x, getCoords().y - speed/2);
					currentTile = prevTile;
				}
				break;
			case "west":
				setCoords(getCoords().x - speed, getCoords().y);
				currentTile = currentRoom.calcTile(coords);
				if(!currentRoom.currTileIsInRoom(currentTile) && prevTile.isDoorMat().equals("west")){
					currentRoom = DestinysWild.getBoard().getRoomFromId(currentRoom.getWest());
					changeRoom(prevTile);
				}
				else if(!currentRoom.currTileIsInRoom(currentTile) || !canChangeTile()){
					setCoords(getCoords().x + speed, getCoords().y);
					currentTile = prevTile;
				}
				break;
			default:
				throw new Error("Invalid Direction");
		}
		return true;
	}

	/**
	 * Updates everything required upon changing room
	 * @param previousTile the player's previous Tile
	 */
	public void changeRoom(Tile previousTile){
		if(!visitedRooms.contains(currentRoom)){
			addCurrentRoom();
		}

		if(currentRoom.hasNoOtherPlayers(getName())){
			currentRoom.resetNpcs();
		}

		int prevX = previousTile.getRoomCoords().x;
		int prevY = previousTile.getRoomCoords().y;

		Point newPoint;

		if(prevX == 0){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(9, previousTile.getRoomCoords().y)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevX == 9){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(0, previousTile.getRoomCoords().y)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevY == 0){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(previousTile.getRoomCoords().x, 9)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		else if(prevY == 9){
			newPoint = currentRoom.getTileFromRoomCoords(new Point(previousTile.getRoomCoords().x, 0)).getRealCoords();
			setCoords(newPoint.x, newPoint.y);
		}
		currentTile = currentRoom.calcTile(coords);
	}


	/**
	 * adds any room object to the visited Room list
	 * @param room room to add
	 */
	public void addRoom(Room room){
		visitedRooms.add(room);
	}

	/**
	 * adds the current room to the list of visited rooms for the player
	 */
	public void addCurrentRoom(){
		visitedRooms.add(currentRoom);
	}

	/**
	 * adds an item object to the player's inventory if there is room for one more of that Item type
	 * @param item item to add
	 * @return boolean successful
	 */
	public boolean addInventoryItem(Item item){
		if((item instanceof Health && numHealthItems()<5) || (item instanceof Key && numKeyItems() < 5) || item instanceof Tool) {
			if (item.getType() != null && item.getType().equals("torch")) {
				hasTorch = true;
				TorchPacket torchPacket = new TorchPacket(this.getName(),true);
				if(DestinysWild.getMultiplayer() != null){
					torchPacket.writeData(DestinysWild.getMultiplayer().getClient());
				}
			}
			if(item instanceof Key){
				numKeyPieces++;
			}
			return inventory.add(item);
		}
		else if(item instanceof Score){
			return true;
		}
		else{
			DestinysWild.startTalking("Too many " + item.toString() + " items in your inventory!");
			return false;
		}
	}

	/**
	 * Tries to make the complete key for the Player. This is called after interacting with
	 * fladnag. To make the key, the players in the room must collectively have the 4 pieces
	 * scattered around the map.
	 * @return boolean success
	 */
	public boolean tryMakeKey(){
		int keyCount = 0;
		List<Player> inRoom = new ArrayList<>();
		for(Player player : DestinysWild.getBoard().getPlayers()){
			if(player.getCurrentRoom().equals(this.getCurrentRoom())){
				inRoom.add(player);
			}
		}
		for(Player player : inRoom){
			keyCount += player.numKeyItems();
		}
		if(keyCount == 4){
			for(int i = 5; i<5 + (5-this.numKeyPieces+1); i++){
				this.addInventoryItem(new Key(i, null));
			}
			for(Player player : inRoom){
				if(player != this){
					player.removeKeys();
				}
			}
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Removes key pieces from inventory. Intended for use when another player
	 * creates the key using some or all of your pieces
	 */
	public void removeKeys(){
		List<Item> toRemove = new ArrayList<>();
		for(Item item : inventory){
			if(item instanceof Key){
				toRemove.add(item);
			}
		}
		for(Item item : toRemove){
			if(item instanceof Key){
				removeInventoryItem(item);
				numKeyPieces--;
			}
		}
	}

	/**
	 * counts the number of health items in the player's inventory
	 * @return int number of health items
	 */
	public int numHealthItems(){
		int count = 0;
		for(Item item : inventory){
			if(item instanceof Health){
				count++;
			}
		}
		return count;
	}

	/**
	 * counts the number of tools in the player's inventory
	 * @return int number of tools
	 */
	public int numToolItems(){
		int count = 0;
		for(Item item : inventory){
			if(item instanceof Tool){
				count++;
			}
		}
		return count;
	}

	/**
	 * gets the number of key items a player has in their inventory
	 * @return int num key items
	 */
	public int numKeyItems(){
		int count = 0;
		for(Item item : inventory){
			if(item instanceof Key){
				count++;
			}
		}
		return count;
	}
	/**
	 * removes an item at 'index' from the player's inventory
	 * @param index index of item to be removed
	 */
	public void removeInventoryItem(int index){
		inventory.remove(index);
	}

	/**
	 * removes an item by Item object from player's inventory
	 * @param item item to be removed
	 */
	public void removeInventoryItem(Item item){
		inventory.remove(item);
	}

	/**
	 * gets the player's name
	 * @return the player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the current player's name
	 * @param name current player's new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * gets the coordinates of this player
	 * @return coords of this player
	 */
	public Point getCoords() {
		return coords;
	}

	/**
	 * sets the coordinates of this player
	 * @param x coord of player to be set
	 * @param y coord of player to be set
	 */
	public void setCoords(int x, int y) {
		if(coords == null){
			coords = new Point(x, y);
		}
		else{
			coords.setLocation(x, y);
		}
	}

	/**
	 * gets the players current health level
	 * @return int health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * sets the player's current health level
	 * @param health to be set
	 */
	public void setHealth(int health) {
		this.health = health;
	}

	/**
	 * gets the current room of the player
	 * @return current room of player
	 */
	public Room getCurrentRoom() {
		return currentRoom;
	}

	/**
	 * sets the current room of the player
	 * @param currentRoom to be set
	 */
	public void setCurrentRoom(Room currentRoom) {
		this.currentRoom = currentRoom;
	}

	/**
	 * gets the list of rooms that the player has visited
	 * @return List<Room> list of rooms
	 */
	public List<Room> getVisitedRooms() {
		return visitedRooms;
	}

	/**
	 * Because of the way various methods in GameInterface
	 * are set up, it is convenient if the items in the
	 * inventory are returned with all health items first,
	 * then tool items, then key items.
	 * @return sorted inventory
	 */
	public List<Item> getInventory() {
		List<Item> sortedInventory = new ArrayList<Item>();

		//add health items
		for (int i = 0; i < inventory.size(); ++i) {
			if (inventory.get(i) instanceof Health) {
				sortedInventory.add(inventory.get(i));
			}
		}

		//add tools
		for (int i = 0; i < inventory.size(); ++i) {
			if (inventory.get(i) instanceof Tool) {
				sortedInventory.add(inventory.get(i));
			}
		}

		//finally, add key
		for (int i = 0; i < inventory.size(); ++i) {
			if (inventory.get(i) instanceof Key) {
				sortedInventory.add(inventory.get(i));
			}
		}

		return sortedInventory;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setCurrentTile(Tile currentTile){
		this.currentTile = currentTile;
	}

	public Tile getCurrentTile(){
		return currentTile;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getOrientation() {
		return orientation;
	}

	public boolean isInvincible(){
		return invincible;
	}

	public boolean isMoving() {
		return isMoving;
	}

	public void updateWalkState(int i){
		walkState = i;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public boolean isNorth() {
		return north;
	}

	public boolean isSouth() {
		return south;
	}

	public boolean isEast() {
		return east;
	}

	public boolean isWest() {
		return west;
	}

	public void setNorth(boolean north) {
		this.north = north;
	}

	public void setSouth(boolean south) {
		this.south = south;
	}

	public void setEast(boolean east) {
		this.east = east;
	}

	public void setWest(boolean west) {
		this.west = west;
	}

	public int getNumKeyPieces() {
		return numKeyPieces;
	}

	public void setKeyPieces(int keyPieces) {
		this.numKeyPieces = keyPieces;
	}

	/////////////////////////////////ADDING IN PLAYER MULTI THINGS BELOW//////////////////////////////////
	private InetAddress ipAddress;
	private int port;

	/**
	 * Player constructor for multi player
	 * @param name
	 * @param coords
	 * @param currentRoom
	 * @param ipAddress
	 * @param port
	 */
	public Player(String name, Point coords, Room currentRoom, InetAddress ipAddress, int port){
		this.name = name;
		this.coords = coords;
		this.currentRoom = currentRoom;
		this.ipAddress = ipAddress;
		this.port = port;
		this.currentTile = currentRoom.calcTile(coords);
		addCurrentRoom();
	}

	public InetAddress getIP(){
		return ipAddress;
	}

	public int getPort(){
		return port;
	}

	public void setPort(int newPort) {
		port = newPort;
	}

	public void setIP(InetAddress ip) {
		ipAddress = ip;

	}

}
