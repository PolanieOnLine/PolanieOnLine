package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import org.junit.Test;

public class MouseHandlerTest {
	@Test
	public void releasingADragDoesNotAlsoClickTheSource() {
		Handler handler = new Handler();
		JPanel panel = new JPanel();
		handler.mousePressed(event(panel, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1));
		handler.mouseDragged(event(panel, MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON));
		handler.mouseReleased(event(panel, MouseEvent.MOUSE_RELEASED, MouseEvent.BUTTON1));
		assertEquals(1, handler.drags);
		assertEquals(0, handler.clicks);
		handler.mousePressed(event(panel, MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1));
		handler.mouseReleased(event(panel, MouseEvent.MOUSE_RELEASED, MouseEvent.BUTTON1));
		assertEquals(1, handler.clicks);
	}

	private MouseEvent event(JPanel panel, int id, int button) {
		return new MouseEvent(panel, id, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK,
				8, 8, 1, false, button);
	}

	private static class Handler extends MouseHandler {
		int clicks, drags;
		@Override protected boolean onMouseClick(Point p) { clicks++; return true; }
		@Override protected boolean onMouseDoubleClick(Point p) { clicks++; return true; }
		@Override protected void onMouseRightClick(Point p) { }
		@Override protected void onDragStart(Point p) { drags++; }
	}
}
