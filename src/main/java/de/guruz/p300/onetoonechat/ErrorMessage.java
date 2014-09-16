package de.guruz.p300.onetoonechat;

public class ErrorMessage extends Message {

	public ErrorMessage(String em, Message m) {
		super (m.getFrom(), m.getTo(),em + ": " + m.getText() + "\n");
	}

}
