package util;

import java.util.ArrayList;

import messages.Message;

public class UnboundedMessageBuffer {

	private final ArrayList<Message> buf;

	public UnboundedMessageBuffer() {
		buf = new ArrayList<Message>();
	}

	public synchronized void put(Message msg) throws InterruptedException {
		doPut(msg);
		notifyAll();
	}

	public synchronized void remove(Message msg) {
		buf.remove(msg);
	}

	public synchronized Message get() throws InterruptedException {
		while (isEmpty()) {
			wait();
		}
		Message msg = doGet();
		notifyAll();
		return msg;
	}

	private synchronized final void doPut(Message msg) {
		buf.add(msg);
	}

	private synchronized final Message doGet() {
		Message msg = buf.remove(0);
		return msg;
	}

	public synchronized final boolean isEmpty() {
		return buf.size() == 0;
	}
}
