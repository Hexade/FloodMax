/* Group Members
 * 
 * Upendra Govindagowda (uxg140230)
 * Ankur Gupta (axg156130)
 * Sarvotam Pal Singh (sxs155032) 
 * 
 * */

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Simulates a distributed node
 */
public class Process implements Runnable {

	private static final int DELAY_MIN = 1;
	private static final int DELAY_MAX = 20;
	private volatile boolean canStartRound;
	private Status status;
	private int pid;
	private int currentRound;
	private volatile boolean terminated;

	private ArrayList<Channel> channels;

	public Process(int pid) {
		this.canStartRound = false;
		this.status = Status.UNKNOWN;
		this.pid = pid;		
		this.currentRound = 1;
		this.channels = new ArrayList<Channel>();
		this.terminated = false;
	}
	
	public int getPid() {
		return pid;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void addNeighbor(Process proc) {
		channels.add(new Channel(proc));
	}

	public boolean isCanStartRound() {
		return this.canStartRound;
	}

	public void setCanStartRound(boolean canStartRound) {
		this.canStartRound = canStartRound;
	}

	@Override
	public void run() {

		while (true) {
			
			// wait for confirmation from master
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// incoming messages delivered in the current round
			ArrayList<Message> deliveredMessages = new ArrayList<Message>();
			for (Channel channel : channels) {
				deliveredMessages.addAll(channel.read(currentRound));
			}

			//TODO: remove
			for (Message message : deliveredMessages) {
				//TODO: remove
				System.out.println("READ: [" + this.pid + " (" + currentRound + ")]" +
						"-> From: " + message.getPid() +
						", Time stamp: " + message.getTimeStamp());
			}
			
		    setCanStartRound(false);		    
		    		    
			// Wait for all threads to read their current buffer values
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// TODO: generate new message only in round 1
			broadcast(new Message(this.pid, MessageType.EXPLORE, 0));
			
			//------------------------------------------------------------------------
			
			/*
			 * Floodmax algorithm should be implemented here
			 * Current round incoming messages are available in 'deliveredMessages'
			 * To send a message, please use one of the methods defined below
			 * 
			 * NOTE: Address TODOs if applicable
			 */
			
			  
			//------------------------------------------------------------------------	
			
			currentRound++;
			
			// TODO: This is a work around for termination
			// TODO: Must be removed once the termination logic is in place
			if (currentRound == 10) {
				this.terminated = true;
				break;
			} 
			
			// notify master that current round has ended
			setCanStartRound(false);
		}
	}
	
	/*
	 * Broadcasts a message to all neighbors 
	 */
	private void broadcast(Message message) {
		for (Channel channel : channels) {
			sendMessage(channel.getProcess(), message);
		}
	}

	/*
	 * Sends a message to specified neighboring process
	 */
	private void sendMessage(Process toProcess, Message message) {
		int rand_delay = ThreadLocalRandom.current().nextInt(DELAY_MIN, DELAY_MAX);
		int timeStamp = currentRound + rand_delay;
		// set new delay
		message.setTimeStamp(timeStamp);
		//TODO: remove
		System.out.println("SEND: [" + this.pid + " (" + currentRound + ")]" +
				"-> To: " + toProcess.getPid() +
				", Time stamp: " + message.getTimeStamp());
		toProcess.putMessage(message);
	}

	/*
	 * Sends to message to specified process id
	 */
	@SuppressWarnings("unused")
	private void sendMessage(int toPid, Message message) {
		sendMessage(getChannel(toPid).getProcess(), message);
	}

	/*
	 * Gets channel based on process id
	 */
	private Channel getChannel(int toPid) {
		for (Channel ch: channels) {
			if (toPid == ch.getProcess().getPid())
				return ch;
		}
		return null;
	}
	
	/*
	 * Puts the message in Channel buffer
	 */
	public void putMessage(Message message) {
		getChannel(message.getPid()).add(message);
	}
}
