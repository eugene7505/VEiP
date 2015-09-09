package veip.fsm;

public class Event {

	String name;
	private boolean observable = true;
	private boolean inserted = false;
	private boolean replacement = false;

	public Event(String eventName) {
		name = eventName;
	}

	public Event(String eventName, boolean isObservable) {
		name = eventName;
		setObservable(isObservable);
	}

	public String getName() {
		return name;
	}

	public boolean isObservable() {
		return observable;
	}

	public boolean isInserted() {
		return inserted;
	}

	public boolean isReplacement() {
		return replacement;
	}

	public void setObservable(boolean observable) {
		this.observable = observable;
	}

	public void setInserted(boolean inserted) {
		this.inserted = inserted;
	}

	public void setReplacement(boolean replacement) {
		this.replacement = replacement;
	}

}
