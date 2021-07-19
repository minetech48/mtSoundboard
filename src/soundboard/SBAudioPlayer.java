package soundboard;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import engine.core.Logger;
import engine.util.FileIO;
import javazoom.spi.mpeg.sampled.file.MpegEncoding;
import javazoom.spi.vorbis.sampled.file.VorbisEncoding;

public class SBAudioPlayer {
	
	private static Player player1, player2;
	
	protected static int running = 0, playingSounds = 0;
	
	//initialization
	public static void initialize() {
		
	}
	
	public static void setAudio1(String deviceName) {
		player1 = null;
		player1 = new Player(getInfo(deviceName));
	}
	public static void setAudio2(String deviceName) {
		player2 = null;
		player2 = new Player(getInfo(deviceName));
	}
	
	private static Mixer.Info getInfo(String deviceName) {
		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			if (info.getName().equals(deviceName))
				return info;
		}
		
		return null;
	}
	
	//playing sound
	public static void playSound(String soundName) {
		File soundFile = FileIO.getFile("soundboards/" + SBController.selectedBoard + "/" + soundName);
		
		if (player1 != null)
			playSoundFile(soundFile, player1);
		if (player2 != null)
			playSoundFile(soundFile, player2);
	}
	
	public static void playSoundFile(File soundFile) {
		if (player1 != null)
			playSoundFile(soundFile, player1);
		if (player2 != null)
			playSoundFile(soundFile, player2);
	}
	public static void playSoundFile(File soundFile, Player player) {
		int id = SBAudioPlayer.running;
		
		if (SBAudioPlayer.playingSounds == 0)
			SBController.PTTKey(true);
		SBAudioPlayer.playingSounds++;
		
		Thread playerThread = new Thread() {
			public void run() {
				AudioInputStream stream;
				
				AudioFormat outFormat;
				
				try {
					stream = AudioSystem.getAudioInputStream(soundFile);
					outFormat = getOutFormat(stream.getFormat());
					
					stream = AudioSystem.getAudioInputStream(outFormat, stream);
				} catch (UnsupportedAudioFileException | IOException e1) {Logger.log(e1); return;}
				
				SourceDataLine dataLine = player.getDataLine(outFormat);
				
				byte[] buffer = new byte[2048];
				int bytesRead = 0;
				
				try {
					while ((bytesRead = stream.read(buffer)) != -1 && SBAudioPlayer.running == id) {
						dataLine.write(buffer, 0, bytesRead);
					}
				} catch (IOException | NullPointerException e) {}
				
				//end of file
				closeLine(dataLine, id);
				
				try {
					stream.close();
				} catch (IOException e) {
					Logger.logException(e);
				}
				
				SBAudioPlayer.playingSounds--;
				if (SBAudioPlayer.playingSounds == 0)
					SBController.PTTKey(false);
			}
		};
		
		playerThread.start();
	}
	
	private static AudioFormat getOutFormat(AudioFormat inFormat) {
		final int ch = inFormat.getChannels();
		final float rate = inFormat.getSampleRate();
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch*2, rate, false);
	}
	////
	
	public static void closeLine(SourceDataLine line, int id) {
		if (SBAudioPlayer.running != id) {
			line.stop();
			line.flush();
		}
		line.drain();
		
		line.close();
	}
	
	public static int getPlayingSounds() {
		return playingSounds;
	}
	public static void stopAllSounds() {
		running++;
		
		SBController.PTTKey(false);
	}
}


class Player {
	
	Mixer.Info mixerInfo;
	
	public Player(Mixer.Info info) {
		mixerInfo = info;
	}
	
	public SourceDataLine getDataLine(AudioFormat format) {
		SourceDataLine dataLine = null;
		
		try {
			dataLine = AudioSystem.getSourceDataLine(format, mixerInfo);
			
			dataLine.open(format);
			dataLine.start();
			
			return dataLine;
		} catch (LineUnavailableException e) {dataLine.close(); return null;}
	}
}