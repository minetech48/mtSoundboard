package main;

import engine.core.Console;
import engine.core.EventBus;
import engine.ux.GUI;
import soundboard.SBController;

public class Main {
	
	public static void main(String[] args) {
		EventBus.initializeHandler(GUI.class);
		
		EventBus.initializeHandler(SBController.class);
		
		EventBus.initializeHandler(Console.class);
		
		EventBus.start();
	}
}
