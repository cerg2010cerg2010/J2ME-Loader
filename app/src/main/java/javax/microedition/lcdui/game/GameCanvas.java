/*
 * J2ME Loader
 * Copyright (C) 2017 Nikita Shakarun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package javax.microedition.lcdui.game;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class GameCanvas extends Canvas {

	private Image image;
	private Graphics graphics;
	private int key;
	private int repeatedKey;
	public static final int UP_PRESSED = 1 << Canvas.UP;
	public static final int DOWN_PRESSED = 1 << Canvas.DOWN;
	public static final int LEFT_PRESSED = 1 << Canvas.LEFT;
	public static final int RIGHT_PRESSED = 1 << Canvas.RIGHT;
	public static final int FIRE_PRESSED = 1 << Canvas.FIRE;
	public static final int GAME_A_PRESSED = 1 << Canvas.GAME_A;
	public static final int GAME_B_PRESSED = 1 << Canvas.GAME_B;
	public static final int GAME_C_PRESSED = 1 << Canvas.GAME_C;
	public static final int GAME_D_PRESSED = 1 << Canvas.GAME_D;

	public GameCanvas(boolean suppressCommands) {
		super();
		image = Image.createImage(super.getWidth(), super.getHeight());
		graphics = image.getGraphics();
	}

	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, Graphics.LEFT | Graphics.TOP);
	}

	private int convertGameKeyCode(int keyCode) {
		switch (keyCode) {
			case KEY_LEFT:
			case KEY_NUM4:
				return LEFT_PRESSED;
			case KEY_UP:
			case KEY_NUM2:
				return UP_PRESSED;
			case KEY_RIGHT:
			case KEY_NUM6:
				return RIGHT_PRESSED;
			case KEY_DOWN:
			case KEY_NUM8:
				return DOWN_PRESSED;
			case KEY_FIRE:
			case KEY_NUM5:
				return FIRE_PRESSED;
			default:
				return 0;
		}
	}

	public void gameKeyPressed(int keyCode) {
		key |= convertGameKeyCode(keyCode);
	}

	public void gameKeyReleased(int keyCode) {
		repeatedKey &= ~convertGameKeyCode(keyCode);
	}

	public void gameKeyRepeated(int keyCode) {
		repeatedKey |= convertGameKeyCode(keyCode);
	}

	public int getKeyStates() {
		int temp = key;
		temp |= repeatedKey;
		key = repeatedKey;
		return temp;
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public void flushGraphics() {
		repaint();
		if (clearBuffer) {
			graphics.setColor(0);
			graphics.setClip(0, 0, width, height);
			graphics.fillRect(0, 0, width, height);
		}
	}

	public void flushGraphics(int x, int y, int width, int height) {
		repaint(x, y, width, height);
		if (clearBuffer) {
			graphics.setColor(0);
			graphics.setClip(0, 0, width, height);
			graphics.fillRect(0, 0, width, height);
		}
	}
}
