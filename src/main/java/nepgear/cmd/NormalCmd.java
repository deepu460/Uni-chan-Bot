package nepgear.cmd;

import nepgear.Nepgear;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class NormalCmd extends Command {

	/**
	 * This is just a simple response
	 */
	private String message;

	public NormalCmd(String regexMatch, String message) {
		super(regexMatch);
		this.message = message;
	}

	@Override
	public void onAction(IMessage msg) {
		String author = msg.getAuthor().getDisplayName(msg.getGuild());
		try {
			Nepgear.say(msg.getChannel(), String.format(message, author));
		} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
			e.printStackTrace();
		}
	}

}