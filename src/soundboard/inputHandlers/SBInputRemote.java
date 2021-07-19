package soundboard.inputHandlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import engine.core.Logger;

public class SBInputRemote extends SBKeyInput {
	
	public static Runnable listenerThread;
	
	public boolean running = true;
	
	static ServerSocket ss;
	InputStream in;
	
	
	public void start() {
		listenerThread = new Runnable() {
			public void run() {
				try {
					ss = new ServerSocket(4800);
					Logger.log("listening for remote server on port 4800");
					
					Socket s = ss.accept();
					in = s.getInputStream();
					Logger.log("client connected");
					
					int buffer = 0;
					while (running) {
						buffer =
								(in.read() << 24)	+
								(in.read() << 16)	+
								(in.read() << 8)	+
								(in.read());
						
						keyInput(buffer);
						
						buffer = 0;
					}
				} catch (IOException e) {
					if (running)
						Logger.logException(e);
					
					try {
						ss.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if (running)
						new Thread(listenerThread).start();
				}
			}
		};
		
		new Thread(listenerThread).start();
	}
	
	public void stop() {
		running = false;
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
