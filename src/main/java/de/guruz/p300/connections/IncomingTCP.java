/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.guruz.p300.connections;

import java.nio.channels.SocketChannel;

import de.guruz.p300.Configuration;

/**
 * This class represents an incoming TCP connection
 * @author guruz
 *
 */
public class IncomingTCP extends TCP {
	protected IncomingTCP(SocketChannel s) throws Exception {
		super ();
		this.socketChannel = s;
		this.socketChannel.configureBlocking(false);
		this.socketChannel.socket().setSendBufferSize(Configuration.getServerSendBufferSize());
		
		this.socketChannel.socket().setKeepAlive(true);
		selector = this.socketChannel.provider().openSelector();
		
		//this.socketChannel.socket().setSoTimeout(30*1000);
		m_key = "FIXME";
		
		//D.out (this + " receive buffer size = " + this.socketChannel.socket().getReceiveBufferSize());
		//D.out (this + "    send buffer size = " + this.socketChannel.socket().getSendBufferSize());	
	}


}
