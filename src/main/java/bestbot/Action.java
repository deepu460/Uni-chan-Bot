package bestbot;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

@FunctionalInterface
public interface Action {
	
	public void act(IDiscordClient client, IMessage msg);
	
}