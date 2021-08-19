package soundboard;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer.Info;

import engine.core.Event;
import engine.core.EventBus;
import engine.util.FileIO;
import engine.ux.GUI;
import engine.ux.StringHandler;
import soundboard.inputHandlers.SBInputHost;
import soundboard.inputHandlers.SBInputRemote;
import soundboard.inputHandlers.SBKeyInput;

public class SBController {
	
	public static ArrayList<String> audioDeviceNames, soundboards, sounds;
	
	public static String audioDeviceName1, audioDeviceName2, inputMode;
	
	public static String selectedBoard;
	
	private static SBKeyInput input;
	private static SBInputHost inputHost;
	private static SBInputRemote inputRemote;
	
	public static Robot bot;
	public static boolean pttActive;
	
	//init
	public static void initialize() {
		eventGUICleared(null);
		
		try {
			bot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static void initGUI() {
		audioDeviceNames = new ArrayList<>();
		
		GUI.stringLists.put("audioDeviceNames", audioDeviceNames);
		
		Info[] mixers = AudioSystem.getMixerInfo();
		
		for (Info info : mixers) {
			if (!info.getDescription().endsWith("Playback"))
				continue;
			audioDeviceNames.add(info.getName());
		}
		
		switch (inputMode) {
		case "Remote":
			if (inputRemote != null) {
				inputRemote.stop();
			}
			inputRemote = new SBInputRemote();
			inputRemote.start();
			
			input = inputRemote;
			break;
			
		case "Host":
		default:
			if (inputHost != null) {
				inputHost.stop();
			}
			inputHost = new SBInputHost();
			inputHost.start();
			
			input = inputHost;
			break;
		}
		
		//binding string handlers
		GUI.addStringHandler("keybindings", new StringHandler() {
			public String getString(String key) {
				Integer toReturn = input.globalBinds.getValue(key);
				
				if (toReturn == null) return "";
				return GUI.getKeyText(toReturn);
			}
		});
		GUI.addStringHandler("boardBinds", new StringHandler() {
			public String getString(String key) {
				Integer toReturn = input.boardBinds.getValue(key);
				
				if (toReturn == null) return "";
				return GUI.getKeyText(toReturn);
			}
		});
		GUI.addStringHandler("soundBinds", new StringHandler() {
			public String getString(String key) {
				Integer toReturn = input.soundBinds.getValue(key);
				
				if (toReturn == null) return "";
				return GUI.getKeyText(toReturn);
			}
		});
	}
	
	//updating
	public static void update() {
		if (SBAudioPlayer.getPlayingSounds() < 1 && pttActive)
			PTTKey(false);
	}
	
	public static void PTTKey(boolean activate) {
		if (activate) {
			bot.keyPress(input.globalBinds.getValue("PushToTalk") & 65535);
		}else{
			bot.keyRelease(input.globalBinds.getValue("PushToTalk") & 65535);
		}
		
		pttActive = activate;
	}
	
	//events
	public static void eventGUICleared(Event event) {
		readConfig();
		
		initGUI();
		
		loadSoundboards();
		input.loadBoards();
		input.loadGlobal();
	}
	
	public static void eventSBSelectBoard(Event event) {
		sounds = new ArrayList<>();
		
		if (event.getArgument(0).contains("/")) {
			sounds.add("?Back");
		}
		
		loadSounds(event.getArgument(0));
		
		input.loadBoard(event.getArgument(0));
		
		if (event.getArgument(0).contains("/")) {
			input.soundBinds.put("?Back", input.globalBinds.getValue("SBBack"));
		}
	}
	
	//sounds
	public static void eventSBBack(Event event) {
		if (selectedBoard != null && selectedBoard.contains("/")) {
			EventBus.broadcast("SBSelectBoard", SBController.selectedBoard.substring(0, SBController.selectedBoard.lastIndexOf('/')));
		}
	}
	public static void eventSBPlaySound(Event event) {
		File soundFile = FileIO.getFile("soundboards/" + SBController.selectedBoard + "/" + event.getArgument(0));
		
		if (event.getArgument(0).equals("?Back"))
			EventBus.broadcast("SBSelectBoard", SBController.selectedBoard.substring(0, SBController.selectedBoard.lastIndexOf('/')));
		else
		if (soundFile.isDirectory()) 
			EventBus.broadcast("SBSelectBoard", SBController.selectedBoard + "/" + event.getArgument(0));
		else
			SBAudioPlayer.playSoundFile(soundFile);
		
	}
	public static void eventSBPlayCurrent(Event event) {
		EventBus.broadcast("SBPlaySound", GUI.getElement("SoundBoard.sounds").getReturnValue());
	}
	
	public static void eventSBStopSounds(Event event) {
		SBAudioPlayer.stopAllSounds();
	}
	
	public static void eventSBClearBinding(Event event) {
		input.soundBinds.removeValue(GUI.getElement("SoundBoard.sounds").getReturnValue());
		input.writeBoard(selectedBoard);
	}
	
	//input events
	public static void eventSBFixPTTHold(Event event) {
		SBController.PTTKey(false);
		SBController.PTTKey(true);
	}
	
	public static void eventSetAudio1(Event event) {
		audioDeviceName1 = event.getArgument(0);
		GUI.setString("AudioDevice1", audioDeviceName1);
		SBAudioPlayer.setAudio1(audioDeviceName1);
		writeConfig();
	}
	public static void eventSetAudio2(Event event) {
		audioDeviceName2 = event.getArgument(0);
		GUI.setString("AudioDevice2", audioDeviceName2);
		SBAudioPlayer.setAudio2(audioDeviceName2);
		writeConfig();
	}
	
	//keybinding
	public static void eventSBSoundBinding(Event event) {input.soundBinding = true;}
	public static void eventSBSoundBindingR(Event event) {input.soundBinding = false;}
	public static void eventSBBoardBinding(Event event) {input.boardBinding = true;}
	public static void eventSBBoardBindingR(Event event) {input.boardBinding = false;}
	public static void eventSBGlobalBinding(Event event) {input.globalBinding = true;}
	public static void eventSBGlobalBindingR(Event event) {input.globalBinding = false;}
	
	//methods
	public static void loadSoundboards() {
		soundboards = new ArrayList<>();
		
		GUI.stringLists.put("soundboards", soundboards);
		
		File SBDir = FileIO.getFile("soundboards");
		
		if (!SBDir.exists()) {
			try {
				SBDir.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		
		for (File file : SBDir.listFiles()) {
			if (file.isDirectory())
				soundboards.add(file.getName());
		}
	}
	
	public static void loadSounds(String boardName) {
		selectedBoard = boardName;
		
		GUI.stringLists.put("sounds", sounds);
		
		File soundDir = FileIO.getFile("soundboards/" + boardName);
		
		if (!soundDir.exists()) {
			return;
		}
		
		for (File file : soundDir.listFiles()) {
			if (!file.getName().endsWith(".txt"))
				sounds.add(file.getName());
		}
	}
	
	public static void readConfig() {
		File config = FileIO.getFile("config/SBConfig.txt");
		
		Scanner in;
		try {
			in = new Scanner(config);
		} catch (FileNotFoundException e) {
			return;
		}
		
		String[] split;
		
		//audio devices
		split = in.nextLine().split(":");
		audioDeviceName1 = split[1];
		GUI.setString("AudioDevice1", audioDeviceName1);
		SBAudioPlayer.setAudio1(audioDeviceName1);
		split = in.nextLine().split(":");
		audioDeviceName2 = split[1];
		GUI.setString("AudioDevice2", audioDeviceName2);
		SBAudioPlayer.setAudio2(audioDeviceName2);
		
		//key input method
		split = in.nextLine().split(":");
		inputMode = split[1];
		
		//
		
	}
	
	public static void writeConfig() {
		try {
			FileWriter writer = new FileWriter(FileIO.getFile("config/SBConfig", true));
			
			writer.write("AudioDevice1:" + audioDeviceName1);
			writer.write('\n');
			writer.write("AudioDevice2:" + audioDeviceName2);
			writer.write('\n');
			writer.write("InputMode:" + inputMode);
			writer.write('\n');
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
