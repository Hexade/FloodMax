/* Group Members
 * 
 * Upendra Govindagowda (uxg140230)
 * Ankur Gupta (axg156130)
 * Sarvotam Pal Singh (sxs155032) 
 * 
 * */


public class Message {
	private int pid;
	private MessageType type;
	private int timeStamp;
	
	public Message(int pid, MessageType type, int timeStamp) {
		this.pid = pid;
		this.type = type;
		this.timeStamp = timeStamp;
	}

	public int getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
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
