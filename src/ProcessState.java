
public class ProcessState {
	private volatile boolean canStartRound;
	private Status status;
	private int pid;

	public ProcessState(int pid) {
		this.canStartRound = false;
		this.status = Status.UNKNOWN;
		this.pid = pid;
	}

	public int getPid() {
		return pid;
	}

	public boolean isCanStartRound() {
		return (status == Status.UNKNOWN) && canStartRound;
	}

	public void setCanStartRound(boolean canStartRound) {
		this.canStartRound = canStartRound;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
