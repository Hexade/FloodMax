import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Simulates a channel/link with input buffer
 */
public class Channel {
	private Process process;
	private Queue<Message> messageQ;
	
	public Channel(Process proc) {
		this.process = proc;
		messageQ = new ConcurrentLinkedQueue<Message>();
	}

	public Process getProcess() {
		return process;
	}

	public void add(Message message) {
		this.messageQ.add(message);
	}
	
	// reads messages in FIFO order 
	// with time-stamps less than or equal to current round
	public ArrayList<Message> read(int clockValue) {		
		ArrayList<Message> deliverableMessages = new ArrayList<Message>();
		while (this.messageQ.size() > 0 &&
				this.messageQ.peek().getTimeStamp() <= clockValue) {
			deliverableMessages.add(this.messageQ.poll());
		}
		return deliverableMessages;
	}
}
