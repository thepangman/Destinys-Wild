//Author: Matt Meyer

package Renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class LevelEditorPanel extends JPanel implements MouseListener, MouseMotionListener{
	
	//Where to start drawing the board
	private int drawX = 50;
	private int drawY = 50;
	
	//The size of a tile
	private int size = 50;
	
	//Set the hover x and y to -1 to indicate that it is neither on the board nor the select list
	private int hoverX = -1;
	private int hoverY = -1;
	private Color hoverColor = new Color(255, 255, 255, 128); //The last int is the transparency value
	
	//The x and y values for the select list
	private int selectX = 650;
	private int selectY = -1;
	
	//The default tile and colours
	private String type = "stone";
	private Color color = new Color(194, 194, 194);
	private Color color2 = new Color(143, 143, 143);
	
	//The tiles on the current room
	private EditorTile[][] tiles = new EditorTile[10][10];
	
	//The list of tiles to select from
	private List<EditorTileSelect> selects = new ArrayList<EditorTileSelect>();
	
	private boolean onBoard = false; //Mouse is on the board
	private boolean onSelect = false; //Mouse if on the Select list
	
	//Booleans for each wall to say if there is a door there or not
	private boolean north = false;
	private boolean south = false;
	private boolean east = true;
	private boolean west = true;
	
	public LevelEditorPanel(){
		//Make the editor listen for mouse inputs
		addMouseListener(this);
		//Make the editor listen for mouse motion inputs
		addMouseMotionListener(this);
		//Add all the different tile types to the select list
		selects.add(new EditorTileSelect(new EditorTile(99, 99, "stone", new Color(194, 194, 194), new Color(143, 143, 143))));
		selects.add(new EditorTileSelect(new EditorTile(99, 99, "Bstone", new Color(194, 194, 194), new Color(143, 143, 143))));
		selects.add(new EditorTileSelect(new EditorTile(99, 99, "Mstone", new Color(194, 194, 194), new Color(25, 123, 48))));
		selects.add(new EditorTileSelect(new EditorTile(99, 99, "Coin", new Color(255, 215, 0), new Color(205, 173, 0))));
		selects.add(new EditorTileSelect(new EditorTile(99, 99, "Water", new Color(105, 232, 255), new Color(25, 152, 255))));
	}
	
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		drawSelects(g);
		drawGround(g);
		drawDoors(g);
		drawTiles(g);
		drawHover(g);
	}
	
	/*
	 * Draws the doors to the panel, if the corresponding booleans are true
	 */
	public void drawDoors(Graphics g){
		if(west){
			g.setColor(new Color(194, 194, 194));
			g.fillRect(0, 250, size, size);
			g.fillRect(0, 300, size, size);
			g.setColor(new Color(143, 143, 143));
			g.drawRect(0, 250, size, size);
			g.drawRect(0, 300, size, size);
		}
		if(east){
			g.setColor(new Color(194, 194, 194));
			g.fillRect(550, 250, size, size);
			g.fillRect(550, 300, size, size);
			g.setColor(new Color(143, 143, 143));
			g.drawRect(550, 250, size, size);
			g.drawRect(550, 300, size, size);
		}
		if(north){
			g.setColor(new Color(194, 194, 194));
			g.fillRect(250, 0, size, size);
			g.fillRect(300, 0, size, size);
			g.setColor(new Color(143, 143, 143));
			g.drawRect(300, 0, size, size);
			g.drawRect(250, 0, size, size);
		}
		if(south){
			g.setColor(new Color(194, 194, 194));
			g.fillRect(250, 550, size, size);
			g.fillRect(300, 550, size, size);
			g.setColor(new Color(143, 143, 143));
			g.drawRect(300, 550, size, size);
			g.drawRect(250, 550, size, size);
		}
	}
	
	/*
	 * Draw all the tiles in the select list
	 */
	public void drawSelects(Graphics g){
		int y = 0;
		int x = 649;
		for(EditorTileSelect e : selects){
			e.draw(g, x, y, size);
			y += size;
		}
	}
	
	/*
	 * Draw all the default grass tiles, a 10*10 grid
	 */
	public void drawGround(Graphics g){
		int x = 0;
		int y = 0;
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 10; j++){
				g.setColor(new Color(145, 255, 149));
				g.fillRect(x+drawX, y+drawY, size, size);
				g.setColor(new Color(91, 188, 95));
				g.drawRect(x+drawX, y+drawY, size, size);
				x += size;
			}
			x = 0;
			y += size;
		}
	}
	
	/*
	 * Draw all the tiles that have been placed
	 */
	public void drawTiles(Graphics g){
		int x = 0;
		int y = 0;
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 10; j++){
				if(tiles[j][i] != null){
					tiles[j][i].draw(g, x+drawX, y+drawY, size);
				}
				x += size;
			}
			x = 0;
			y += size;
		}
	}
	
	/*
	 * Highlight the tile that the mouse is hovering over on the board or the select list
	 */
	public void drawHover(Graphics g){
		if(onBoard){
			g.setColor(hoverColor);
			g.fillRect((hoverX*size)+drawX+1, (hoverY*size)+drawY+1, size-1, size-1);
		}
		else if (onSelect){
			g.setColor(hoverColor);
			g.fillRect(selectX, (selectY*size)+1, size-1, size-1);
		}
	}
	
	/*
	 * An algorithm to figure out which tile the mouse is hovering over
	 */
	public void checkTile(int x, int y){
		int oldX = hoverX;
		int oldY = hoverY;
		
		hoverX = (x - drawX)/size;
		hoverY = (y - drawY)/size;
		
		if(x < drawX || y < drawY){
			hoverX = -1;
			hoverY = -1;
			onBoard = false;
		}
		else if(hoverX > 9 || hoverY > 9){
			hoverX = -1;
			hoverY = -1;
			onBoard = false;
		}
		else{
			onBoard = true;
		}
		
		if(oldX != hoverX || oldY != hoverY){
			this.repaint();
		}
	}
	
	/*
	 * Check which tile the mouse is hovering over on the select list
	 */
	public void checkSelect(int x, int y){
		if(x > 650){
			int oldY = selectY;
			selectY = y/size;
			onSelect = true;
			if(oldY != selectY){
				this.repaint();
			}
		}
		else{
			onSelect = false;
			this.repaint();
		}
	}
	
	/*
	 * Add a tile to the tiles array, with the location of the mouse
	 */
	public void createTile(){
		tiles[hoverX][hoverY] = new EditorTile(hoverX, hoverY, type, color, color2);
		this.repaint();
	}
	
	/*
	 * Change the current tile to the tile clicked on the select list
	 */
	public void selectTile(){
		type = selects.get(selectY).getType();
		color = selects.get(selectY).getColor();
		color2 = selects.get(selectY).getColor2();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(onBoard){
			createTile();
		}
		if(onSelect){
			selectTile();
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseMoved(MouseEvent e) {
		checkTile(e.getX(), e.getY());
		checkSelect(e.getX(), e.getY());
	}

}
