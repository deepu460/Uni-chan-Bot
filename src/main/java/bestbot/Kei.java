package bestbot;

import java.util.HashMap;
import java.util.Map;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

/**
 * Kei handles events. She's basically the event handler...
 */
public class Kei {

	/**
	 * The list of all citizens managed by Uni. It first maps by server then
	 * user.
	 */
	public static Map<IGuild, Map<IUser, Citizen>> citizens = new HashMap<>();

	@EventSubscriber
	public void onReadyEvent(ReadyEvent e) {
		System.out.println("Ready!");
		// Let the fun begin
		Uni.msgr = new MessageBuilder(Uni.client);
	}

	@EventSubscriber
	public void onMessageEvent(MessageReceivedEvent e) {
		IMessage msg = e.getMessage();
		if (!msg.getAuthor().isBot())
			if (Basilicon.detectCmd(msg.getContent()))
				Basilicon.handleCmd(msg);
			else if (Basilicon.hasUserActions(msg.getAuthor()))
				Basilicon.userActions.get(msg.getAuthor()).forEach(a -> a.getAction().act(Uni.client, msg));
	}

	/**
	 * Finds the citizen representation of a user within a server. If there is
	 * none, then one will be made for the user.
	 * 
	 * @param user
	 *            - The user to search for.
	 * @param guild
	 *            - The server the user is located in.
	 * @return A citizen representation of the user within the server.
	 */
	public static Citizen retrieveCitizen(IUser user, IGuild guild) {
		if (!citizens.containsKey(guild))
			citizens.put(guild, new HashMap<>());
		Map<IUser, Citizen> map = citizens.get(guild);
		if (!map.containsKey(user))
			map.put(user, new Citizen(user, guild));
		return map.get(user);
	}
	
}