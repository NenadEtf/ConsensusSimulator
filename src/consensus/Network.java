package consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.management.InstanceAlreadyExistsException;

import messages.Message;
import simulators.BasicConsensusSimulator;
import util.ConcurrentPrintWriter;
import util.UnboundedMessageBuffer;

public class Network extends Thread {

	public static HashMap<Integer, UnboundedMessageBuffer> messageBuffers = new HashMap<Integer, UnboundedMessageBuffer>();

	private ArrayList<Message> messages = new ArrayList<>();
	private boolean finished;

	public void run() {
		while (!finished) {
			try {
				redirectMessage();
			} catch (InterruptedException e) {
				finished = true;
			}
		}
	}

	
	public void stopNetwork() {
		finished = true;
		interrupt();
	}
	
	public void restartNetwork() {
		messageBuffers = new HashMap<Integer, UnboundedMessageBuffer>();
		messages = new ArrayList<>();
	}
	

	public synchronized void redirectMessage() throws InterruptedException {
		long time = messages.isEmpty() ? 0 : messages.get(0).getTimeStamp() - System.currentTimeMillis();
		if (time >= 0) {
			// wait(time);
			return;
		}
		Message msg = messages.get(0);
		if (msg.getTimeStamp() <= System.currentTimeMillis()) {
			messages.remove(0);
			messageBuffers.get(msg.getReceiverID()).put(msg);
			// ConcurrentPrintWriter.printOnSystemOut("PREBACENO[ " + messages.size() +
			// "]");
		}
	}

	public synchronized void addMessage(Message message) {
		if (messages.isEmpty()) {
			messages.add(message);
		} else {
			boolean messageAdded = false;
			for (int i = 0; i < messages.size(); i++) {
				if (message.getTimeStamp() < messages.get(i).getTimeStamp()) {
					messages.add(i, message);
					messageAdded = true;
					break;
				}
			}
			if (!messageAdded) {
				messages.add(message);
			}
		}
		notifyAll();
	}

	public static void addNodeBuffer(int nodeID) {
		messageBuffers.put(nodeID, new UnboundedMessageBuffer());
	}

}
