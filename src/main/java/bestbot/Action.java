package bestbot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

/**
 * A lazy way of incorporating java 8 lambda functions within this program as
 * modules.
 */
@FunctionalInterface
public interface Action {

	/**
	 * Does an action based on a given message string.
	 * 
	 * @param client
	 *            - the Discord client used.
	 * @param msg
	 *            - the message string.
	 */
	public void act(IDiscordClient client, IMessage msg);

}