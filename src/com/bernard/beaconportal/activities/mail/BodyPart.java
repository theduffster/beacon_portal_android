package com.bernard.beaconportal.activities.mail;

public abstract class BodyPart implements Part {
	private Multipart mParent;

	public Multipart getParent() {
		return mParent;
	}

	public void setParent(Multipart parent) {
		mParent = parent;
	}

	public abstract void setEncoding(String encoding) throws MessagingException;

	@Override
	public abstract void setUsing7bitTransport() throws MessagingException;
}
