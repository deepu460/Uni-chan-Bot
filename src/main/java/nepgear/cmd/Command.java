package nepgear.cmd;

import sx.blah.discord.handle.obj.IMessage;

/**
 * This defines any action to take given an input message and possibly some
 * other contextual clues.
 */
public abstract class Command {

	/**
	 * This is the "trigger" that will enable action
	 */
	protected String regexMatch;

	protected Command(String regexMatch) {
		this.regexMatch = regexMatch;
	}

	/**
	 * Returns true if the message contains a trigger.
	 */
	public boolean isTriggered(IMessage msg) {
		return msg.getContent().matches(regexMatch);
	}

	/**
	 * Define an action of what to say or do...
	 */
	public abstract void onAction(IMessage msg);

}