
public class Message {
	private int pid;
	private MessageType type;
	private int hops;
	
	public Message(int pid, MessageType type, int hops) {
		this.pid = pid;
		this.type = type;
		this.hops = hops;
	}

	public int getHops() {
		return hops;
	}

	public void decrementHops() {
		this.hops--;
	}

	public int getPid() {
		return pid;
	}

	public MessageType getType() {
		return type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
	
	
}
