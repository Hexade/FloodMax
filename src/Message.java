/* Group Members
 * 
 * Upendra Govindagowda (uxg140230)
 * Ankur Gupta (axg156130)
 * Sarvotam Pal Singh (sxs155032) 
 * 
 * */


public class Message implements Cloneable {
	
	private MessageType type;
	private int timeStamp;
	private int message;
	private int latestExploreSenderParentId;
	private int senderId;
	
	public Message(int message, int senderId, int latestExploreSenderParentId, MessageType type, int timeStamp) {
		this.type = type;
		this.timeStamp = timeStamp;
		this.latestExploreSenderParentId = latestExploreSenderParentId;
		this.message = message;
		this.senderId = senderId;
	}

	public int getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	public MessageType getType() {
		return type;
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}

	public int getMessage() {
		return message;
	}

	public void setMessage(int message) {
		this.message = message;
	}

	public int getLatestExploreSenderParentId() {
		return latestExploreSenderParentId;
	}

	public void setLatestExploreSenderParentId(int latestExploreSenderParentId) {
		this.latestExploreSenderParentId = latestExploreSenderParentId;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public Message(Message another) {
		   this.type = another.type;
		   this.timeStamp = another.timeStamp;
		   this.message = another.message;
		   this.latestExploreSenderParentId = another.latestExploreSenderParentId;
		   this.senderId = another.senderId;
	}
}
