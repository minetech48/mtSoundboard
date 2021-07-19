package soundboard.inputHandlers;

import java.awt.event.KeyEvent;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.keyboard.event.GlobalKeyListener;

public class SBInputHost extends SBKeyInput implements GlobalKeyListener {
	
	GlobalKeyboardHook keyboardHook;
	
	public void start() {
		keyboardHook = new GlobalKeyboardHook(true);
		
		keyboardHook.addKeyListener(this);
	}
	
	public void stop() {
		keyboardHook.removeKeyListener(this);
		keyboardHook.shutdownHook();
	}
	
	public void keyPressed(GlobalKeyEvent event) {
		int modifiers = 0;
		
		if (event.isControlPressed())
			modifiers+= KeyEvent.CTRL_MASK;
		if (event.isShiftPressed())
			modifiers+= KeyEvent.SHIFT_MASK;
		if (event.isMenuPressed())
			modifiers+= KeyEvent.ALT_MASK;
		
		int keyCode = event.getVirtualKeyCode() + (modifiers<<16);
		
		keyInput(keyCode);
	}
	
	public void keyReleased(GlobalKeyEvent event) {}
}
