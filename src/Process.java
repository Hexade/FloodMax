/* Group Members
 * 
 * Upendra Govindagowda (uxg140230)
 * Ankur Gupta (axg156130)
 * Sarvotam Pal Singh (sxs155032) 
 * 
 * */


public class Process implements Runnable {
	private ProcessState p_state;
	private Message msgFromLeft, msgFromRight;
	private Process leftProcess, rightProcess;
	private int round;
	private int phase;

	public Process(int pid) {
		this.p_state = new ProcessState(pid);
		this.msgFromLeft = null;
		this.msgFromRight = null;
		this.round = 1;
	}

	public void setLeftPort(Message leftPort) {
		this.msgFromLeft = leftPort;
	}

	public void setRightPort(Message rightPort) {
		this.msgFromRight = rightPort;
	}

	public void setLeftProcess(Process leftProcess) {
		this.leftProcess = leftProcess;
	}

	public void setRightProcess(Process rightProcess) {
		this.rightProcess = rightProcess;
	}

	public boolean isCanStartRound() {
		return p_state.isCanStartRound();
	}

	public void setCanStartRound(boolean canStartRound) {
		p_state.setCanStartRound(canStartRound);
	}

	@Override
	public void run() {
		while (true) {
			
			// wait for confirmation from master
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Message msgleftLocal = msgFromLeft;
			Message msgRightLocal = msgFromRight;
			msgFromLeft = null; // clear all buffers
			msgFromRight = null; // clear all buffers
		    p_state.setCanStartRound(false);
		    
			// Wait for all threads to read their current buffer values
			while (!isCanStartRound()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (round == 1) {
				sendExploreMessages();
			} else {				
				// process IN messages which are my own tokens
				if (msgleftLocal != null && msgRightLocal != null
						&& MessageType.IN == msgleftLocal.getType()
						&& MessageType.IN == msgRightLocal.getType()
						&& msgleftLocal.getPid() == p_state.getPid()
						&& msgRightLocal.getPid() == p_state.getPid()) {
					phase++;
					sendExploreMessages();
				}
				else {
					
					if (msgleftLocal != null) {
						// process LEADER_ANNOUNCEMENT message
						if (MessageType.LEADER_ANNOUNCEMENT 
								== msgleftLocal.getType()) {
							p_state.setStatus(Status.NON_LEADER);

							/*System.out.println("[Prcoess Info: "  + 
									p_state.getPid() + "]: I am not a Leader");*/
							sendRight(msgleftLocal);
							setCanStartRound(false);
							break;
						}
						
						// process OUT message from left neighbor
						if (MessageType.OUT == msgleftLocal.getType()) {
							if (msgleftLocal.getPid() > p_state.getPid()) {
								if (msgleftLocal.getHops() > 1) {
									msgleftLocal.decrementHops();
									sendRight(msgleftLocal);
								} else if (msgleftLocal.getHops() == 1) {
									msgleftLocal.setType(MessageType.IN);
									sendLeft(msgleftLocal);
								}
							} else if (msgleftLocal.getPid() == p_state.getPid()) {
								p_state.setStatus(Status.LEADER);
								System.out.println();
								System.out.println("[Prcoess Info: "  + 
										p_state.getPid() + "]: I am the LEADER!!!");
								System.out.println();
								sendLeft(new Message(p_state.getPid(), 
										MessageType.LEADER_ANNOUNCEMENT, -1));
								sendRight(new Message(p_state.getPid(), 
										MessageType.LEADER_ANNOUNCEMENT, -1));
								System.out.println("[Prcoess Info: "  + 
										p_state.getPid() + "]: Announcing that I am the leader." +
										" All the processes will terminate in sometime.");
								setCanStartRound(false);
								break;
							}
						}
						// process IN message from left neighbor
						else {
							if (msgleftLocal.getPid() != p_state.getPid()) {
								sendRight(msgleftLocal);
							}
						}
					}
					
					if (msgRightLocal != null) {
						// process LEADER_ANNOUNCEMENT message
						if (MessageType.LEADER_ANNOUNCEMENT 
								== msgRightLocal.getType()) {
							p_state.setStatus(Status.NON_LEADER);
							/*System.out.println("[Prcoess Info: "  + 
									p_state.getPid() + "]: I am not a Leader");*/
							sendLeft(msgRightLocal);
							setCanStartRound(false);
							break;
						}
						
						// process OUT message from right neighbor
						if (MessageType.OUT == msgRightLocal.getType()) {
							if (msgRightLocal.getPid() > p_state.getPid()) {
								if (msgRightLocal.getHops() > 1) {
									msgRightLocal.decrementHops();
									sendLeft(msgRightLocal);
								} else if (msgRightLocal.getHops() == 1) {
									msgRightLocal.setType(MessageType.IN);
									sendRight(msgRightLocal);
								}
							} else if (msgRightLocal.getPid() == p_state.getPid()) {
								p_state.setStatus(Status.LEADER);
								System.out.println();
								System.out.println("[Prcoess Info: "  + 
										p_state.getPid() + "]: I am the LEADER!!!");
								System.out.println();
								
								System.out.println("[Prcoess Info: "  + 
										p_state.getPid() + "]: Announcing that I am the leader." +
										" All the processes will terminate in sometime.");
								sendLeft(new Message(p_state.getPid(), 
										MessageType.LEADER_ANNOUNCEMENT, -1));
								sendRight(new Message(p_state.getPid(), 
										MessageType.LEADER_ANNOUNCEMENT, -1));
								setCanStartRound(false);
								break;
							}
						}					
						// process IN message from right neighbor
						else {
							if (msgRightLocal.getPid() != p_state.getPid()) {
								sendLeft(msgRightLocal);
							}
						}
					}
				}
			}
			round++;
			
			// notify master that current round has ended
			setCanStartRound(false);
		}
	}

	private void sendExploreMessages() {
		// send explore message if round = 1 and 
		// still a contender for leader
		if (Status.UNKNOWN == p_state.getStatus()) {
			System.out.println("[Prcoess Info: "
					+ p_state.getPid() + "]: Starting phase " + phase);
			sendLeft(new Message(p_state.getPid(), MessageType.OUT, 
					(int) Math.pow(2.0, phase)));
			sendRight(new Message(p_state.getPid(), MessageType.OUT, 
					(int) Math.pow(2.0, phase)));
		}
	}

	private void sendRight(Message message) {
		this.rightProcess.setLeftPort(message);
		/*System.out.println("[Message: " + message.getType().toString() + "] : " +
				"Origin: " + message.getPid() + "; " +
				p_state.getPid() +" -> " + rightProcess.p_state.getPid());*/
	}

	private void sendLeft(Message message) {
		this.leftProcess.setRightPort(message);
		/*System.out.println("[Message: " + message.getType().toString() + "]: " +
				"Origin: " + message.getPid() + "; " +
				p_state.getPid() +" -> " + leftProcess.p_state.getPid());*/
	}

}
