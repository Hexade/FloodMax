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
	private static final int DELAY_MAX = 5;
	private volatile boolean canStartRound;
	private Status status;
	private int pid;
	private int currentRound;
	private volatile boolean terminated;
	private ArrayList<Channel> channels;
	
	private Process parent;
	private int max_seen_so_far;
	private int pendingAcks;
	private boolean leaderElected = false;
	private boolean newinfo = true;
	private boolean ackReturned = false;
	private int grandparentid;
	
	public Process(int pid) {
		this.canStartRound = false;
		this.status = Status.UNKNOWN;
		this.pid = pid;	
		this.currentRound = 1;
		this.channels = new ArrayList<Channel>();
		this.terminated = false;
		this.parent = null;
		this.max_seen_so_far = pid;
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
			//String debugMessage = this.pid +" : "+ currentRound;
			//System.out.println(debugMessage);
			// wait for confirmation from master
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			ArrayList<Message> deliveredMessages = new ArrayList<Message>();
			
			for (Channel channel : channels) {
				deliveredMessages.addAll(channel.read(currentRound));
			}

			// Wait for all threads to read their current buffer values				
		    setCanStartRound(false);	
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}		
			//------------------------------------------------------------------------
			
			/*
			 * Floodmax algorithm should be implemented here
			 * Current round incoming messages are available in 'deliveredMessages'
			 * To send a message, please use one of the methods defined below
			 * 
			 * NOTE: Address TODOs if applicable
			 */
			newinfo = false;
			
			if(currentRound == 1) {
				
				Message message = new Message(this.pid, this.pid, 0, MessageType.EXPLORE, 0);
				pendingAcks = channels.size();
				newinfo = true;
				broadcast(message);
			
			} else {
				
				for (Message _message : deliveredMessages) {
					if(_message.getType().equals(MessageType.LEADER_ANNOUNCEMENT)) {
						this.status = Status.NON_LEADER;
						this.parent = getChannel(_message.getSenderId()).getProcess();

						_message.setSenderId(this.pid);
						
						broadcast(_message);
						
						leaderElected = true;
					}			
				}
				
				if(!leaderElected) {
					for (Message _message : deliveredMessages) {

						if(_message.getType().equals(MessageType.EXPLORE)) {
							if(_message.getMessage() > this.max_seen_so_far) {
								this.newinfo = true;
								this.max_seen_so_far = _message.getMessage();
								this.parent = getChannel(_message.getSenderId()).getProcess();
								this.ackReturned = false;
								this.grandparentid = _message.getLatestExploreSenderParentId();
							}
						}
					}

					if (newinfo) {
						Message message = new Message(this.max_seen_so_far, this.pid,
														this.parent.getPid(), MessageType.EXPLORE, 0);

						if(this.parent != null) {
							this.pendingAcks = channels.size() - 1;

							if(this.pendingAcks == 0) {
								this.ackReturned = true;
							}
							
						} else {
							this.pendingAcks = channels.size();
						}
						
						if(ackReturned && pendingAcks == 0) {
						
							Message ack_message = new Message(this.max_seen_so_far, this.pid,
									grandparentid, MessageType.ACK, 0);
							sendMessage(this.parent, ack_message);
						
						} else{
							broadcast(message);
						}
					}

					for (Message _message : deliveredMessages) {
						
						if(_message.getType().equals(MessageType.EXPLORE) ) {

							if(!newinfo) {
								if(_message.getMessage() <= this.max_seen_so_far) {

									Message reject_message = new Message(this.max_seen_so_far, this.pid,
											_message.getLatestExploreSenderParentId(), MessageType.REJECT, 0);

									sendMessage(getChannel(_message.getSenderId()).getProcess(), reject_message);
								}
							} else {
								if(_message.getMessage() <= this.max_seen_so_far
										&& _message.getSenderId() != this.parent.getPid()) {

									Message reject_message = new Message(this.max_seen_so_far, this.pid,
											_message.getLatestExploreSenderParentId(), MessageType.REJECT, 0);

									sendMessage(getChannel(_message.getSenderId()).getProcess(), reject_message);
								}
							}
															
						}
						
						if(_message.getType().equals(MessageType.REJECT)
								|| _message.getType().equals(MessageType.ACK)) {

							if(this.parent != null 
									&& this.parent.getPid() == _message.getLatestExploreSenderParentId()) {
								
								this.pendingAcks--;
							} else if (this.parent == null && _message.getLatestExploreSenderParentId() == 0){
								this.pendingAcks--;
							}
							
							if  (pendingAcks == 0 
									&& status.equals(Status.UNKNOWN) 
									&& this.parent !=null ) {				

								Message ack_message = new Message(this.max_seen_so_far, this.pid,
										grandparentid, MessageType.ACK, 0);
								sendMessage(this.parent, ack_message);

								this.ackReturned = true;
							}
						}
					}
					
					if (pendingAcks == 0 && this.parent == null && this.status.equals(Status.UNKNOWN)) {
						this.status = Status.LEADER;
						Message announcement = new Message(this.max_seen_so_far, this.pid, 0, MessageType.LEADER_ANNOUNCEMENT, 0);
						System.out.println("\n\n\n"+this.pid+"----------------------------------> I am the Leader \n\n\n");
						broadcast(announcement);
						leaderElected = true;
					}
				}	    		    		    
				
			}
			
			 
			//------------------------------------------------------------------------	
			
			currentRound++;
			
			if (leaderElected) {
				this.terminated = true;				
				setCanStartRound(false);
				break;
			}
			
			// notify master that current round has ended
			setCanStartRound(false);
		}
	}
	
	/*
	 * Broadcasts a message to all non parent neighbors 
	 */
	private void broadcast(Message message) {
		for (Channel channel : channels) {
			if(!channel.getProcess().equals(this.parent)) {
				Message newmessage = new Message(message);
				sendMessage(channel.getProcess(), newmessage);
			}
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

		System.out.println("SEND: ["+message.getType().toString() +"] [" + this.pid + " (" + currentRound + ")]" +
				"-> To: " + toProcess.getPid() + ",  Message: "+message.getMessage()+
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
		if(getChannel(message.getSenderId()) != null) {
			getChannel(message.getSenderId()).add(message);
		}
	}
}
