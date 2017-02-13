package bestbot;

/**
 * The command class links a command keyword to its appropriate action.
 */
public class Command {

	/**
	 * The name of the command
	 */
	private String name;

	/**
	 * The action to be taken whenever this command is invoked
	 */
	private Action action;

	public Command(String name, Action action) {
		super();
		this.action = action;
		this.name = name;
	}

	public Command(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Command other = (Command) obj;
		if (name == null)
			if (other.name != null)
				return false;
			else if (!name.equals(other.name))
				return false;
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

}