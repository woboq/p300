package de.guruz.p300;

import de.guruz.p300.threads.ShutdownThread;

public class P300UncaughtExceptionHandler implements
		Thread.UncaughtExceptionHandler {
	public void uncaughtException(Thread t, Throwable e) {
		if (!ShutdownThread.shuttingDown && !(e instanceof java.lang.ThreadDeath)) {
			System.err.println("Uncaught exception by " + t + ":");
			e.printStackTrace();
		}
	}
}
