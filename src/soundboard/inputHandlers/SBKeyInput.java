package soundboard.inputHandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import engine.core.EventBus;
import engine.util.FileIO;
import engine.ux.GUI;
import main.HashBiMap;
import soundboard.SBController;

public class SBKeyInput {
	
	public HashBiMap<String, Integer> soundBinds, boardBinds, globalBinds;
	public HashMap<Integer, ArrayList<String>> randomBinds;
	public boolean soundBinding, boardBinding, globalBinding;
	
	public SBKeyInput() {
		soundBinds = new HashBiMap<>();
		randomBinds = new HashMap<>();
		boardBinds = new HashBiMap<>();
		globalBinds = new HashBiMap<>();
	}
	
	public void keyInput(int keyCode) {
		String eventName;
		
		//keybinding
		
		if (globalBinding) {
			eventName = GUI.getElement("SettingsMenu.keyBindList").getReturnValue();
			
			if (eventName != null) {
				globalBinds.put(eventName, keyCode);
				
				writeGlobal();
			}
			return;
		}
		if (soundBinding) {
			eventName = GUI.getElement("SoundBoard.sounds").getReturnValue();
			
			bindSound(eventName, keyCode);
			
			writeBoard(SBController.selectedBoard);
			
			return;
		}
		if (boardBinding) {
			eventName = SBController.selectedBoard;
			
			if (eventName != null) {
				boardBinds.put(eventName, keyCode);
				
				writeBoards();
			}
			return;
		}
		
		//activation
		eventName = globalBinds.getKey(keyCode);
		if (eventName != null) {
			EventBus.broadcast(eventName);
			return;
		}
		
		eventName = soundBinds.getKey(keyCode);
		if (eventName != null) {
			//picking random sound if needed
			if (eventName.equals("?random"))
				eventName = randomBinds.get(keyCode).get(
						(int) (Math.random()*randomBinds.get(keyCode).size()));
			
			EventBus.broadcast("SBPlaySound", eventName);
			return;
		}
		
		eventName = boardBinds.getKey(keyCode);
		if (eventName != null) {
			EventBus.broadcast("SBSelectBoard", eventName);
			return;
		}
	}
	
	public void bindSound(String eventName, int keyCode) {
		if (eventName != null) {
			String currentSoundName = soundBinds.getKey(keyCode);
			
			if (randomBinds.containsKey(soundBinds.getValue(eventName))) {
				randomBinds.get(soundBinds.getValue(eventName)).remove(eventName);
			}
			
			if (currentSoundName != null) {
//				if (currentSoundName.equals(eventName))
//					return;
				
				//code for random keybinds
				//removing existing random bind if keybind changed
				
				if (!currentSoundName.isEmpty()){
					//creating random list
					if (randomBinds.get(keyCode) == null) {
						randomBinds.put(keyCode, new ArrayList<String>());
						
						randomBinds.get(keyCode).add(currentSoundName);
					}
					
					//adding to random list
					randomBinds.get(keyCode).add(eventName);
					
					//cleaning up
					soundBinds.put(eventName, keyCode);
					soundBinds.put("?random", keyCode);
				}
			}else{
				soundBinds.put(eventName, keyCode);
			}
		}
	}
	
	public void loadBoard(String boardName) {
		File bindings = FileIO.getFile("soundboards/" + boardName + "/SBBindings.txt");
		
		loadKeybindings(bindings, soundBinds);
	}
	public void loadBoards() {
		File bindings = FileIO.getFile("soundboards/SBind.txt");
		
		loadKeybindings(bindings, boardBinds);
	}
	public void loadGlobal() {
		File bindings = FileIO.getFile("config/keybindings.txt");
		
		loadKeybindings(bindings, globalBinds);
	}
	
	public void writeBoard(String boardName) {
		File bindings = FileIO.getFile("soundboards/" + boardName + "/SBBindings.txt", true);
		
		writeKeybindings(bindings, soundBinds);
	}
	public void writeBoards() {
		File bindings = FileIO.getFile("soundboards/SBind.txt", true);
		
		writeKeybindings(bindings, boardBinds);
	}
	public void writeGlobal() {
		File bindings = FileIO.getFile("config/keybindings.txt", true);
		
		writeKeybindings(bindings, globalBinds);
	}
	
	private void loadKeybindings(File file, HashBiMap<String, Integer> map) {
		map.clear();
		if (map == soundBinds)
			randomBinds.clear();
		
		Scanner in;
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			return;
		}
		
		String[] split;
		
		while (in.hasNext()) {
			split = in.nextLine().split(":");
			if (map == soundBinds)
				bindSound(split[0], Integer.parseInt(split[1]));
			else
				map.put(split[0], Integer.parseInt(split[1]));
		}
	}
	
	private void writeKeybindings(File file, HashBiMap<String, Integer> map) {
		try {
			FileWriter writer = new FileWriter(file);
			
			for (String key : map.getKeys()) {
				if (key.startsWith("?"))
					continue;
				
				writer.write(key + ":" + map.getValue(key));
				writer.write('\n');
			}
			
			if (map == soundBinds) {
				for (ArrayList<String> randList : randomBinds.values()) {
					for (String key : randList) {
						writer.write(key + ":" + map.getValue(key));
						writer.write('\n');
					}
				}
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
