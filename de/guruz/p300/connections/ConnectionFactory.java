package de.guruz.p300.connections;

import java.nio.channels.SocketChannel;

public class ConnectionFactory {
	public static SynchronousLogicalStreamConnection createOutgoingTCP (String host, int port, int timeout) throws Exception
	{
		return new OutgoingTCP (host, port, timeout);
	}
	
	public static SynchronousLogicalStreamConnection createIncomingTCP (SocketChannel sc) throws Exception
	{
		return new IncomingTCP (sc);
	}
}
