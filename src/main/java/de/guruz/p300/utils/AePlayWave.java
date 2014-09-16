package de.guruz.p300.utils;

/*
 * From http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * 
 * Adapted by Markus Goetz
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
 
public class AePlayWave extends Thread {
 
	protected static AePlayWave m_one;
	
	public static void playOneAtATime (URL u)
	{
		synchronized (AePlayWave.class)
		{
		if (m_one == null)
		{
			m_one = new AePlayWave (u);
			m_one.start();
		}
		}
	}
	
	protected static void playOneAtATimeStopped ()
	{
		synchronized (AePlayWave.class)
		{
			m_one = null;
		}
	}
	
	private URL m_url;
 
	private Position curPosition;
 
	private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
 
	enum Position {
		LEFT, RIGHT, NORMAL
	};
 
	protected AePlayWave(URL u) {
		m_url = u;
		curPosition = Position.NORMAL;
	}
	

 
 
	public void run() {
		try {

		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(m_url);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
 
		AudioFormat format = audioInputStream.getFormat();
		SourceDataLine auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
 
		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
 
		if (auline.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
			if (curPosition == Position.RIGHT)
				pan.setValue(1.0f);
			else if (curPosition == Position.LEFT)
				pan.setValue(-1.0f);
		} 
 
		auline.start();
		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
 
		try {
			while (nBytesRead != -1) {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
				if (nBytesRead >= 0)
					auline.write(abData, 0, nBytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} finally {
			auline.drain();
			auline.close();
		}
		
		} finally {
			playOneAtATimeStopped ();
		}
 
	}
}

