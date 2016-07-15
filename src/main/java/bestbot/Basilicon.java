package bestbot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * This is where most of the static code goes.
 */
public class Basilicon {

	// Append this regex to ignore quotes
	public static final String IGNORE_QUOTES = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

	// Include this regex for the various "me" words
	public static final String ME = "(me|myself|I|moi|meh|m|em)";

	public static final String UNI = "uni( |\\.|\\?|\\!||-chan|-nee(|-chan))";

	public static Map<String, Action> actions;

	public static Map<IUser, List<Command>> userActions;

	public static String normalSpaces(String msg) {
		return msg.trim().replaceAll(" +", " ");
	}

	public static boolean detectCmd(String msg) {
		return msg.toLowerCase().matches(".*\\b(yo|hey)( |,).*\\b" + UNI + "\\b" + IGNORE_QUOTES + ".*")
				|| msg.toLowerCase().matches(".*\\balright(,| |then(|,)|y then(,|)|)\\b \\b" + UNI + "(|,|!|.|\\?)\\b"
						+ IGNORE_QUOTES + ".*");
	}

	public static String removeNoise(String msg) {
		return normalSpaces(msg.toLowerCase().replaceAll(
				"(\\b(hey|yo|alright)( |,|then(|,)|y then(,|))\\b|\\buni-nee-chan( |\\.|\\?|\\!|)\\b|\\buni-chan( |\\.|\\?|\\!|)\\b|"
						+ "\\buni-nee( |\\.|\\?|\\!|)\\b|\\buni( |\\.|\\?|\\!|)\\b|,|\\bcan you\\b|\\bplease\\b|\\!|\\?|\\.)"
						+ IGNORE_QUOTES,
				""));
	}

	public static boolean hasUserActions(IUser user) {
		return userActions.get(user) != null;
	}

	public static void enableUserActions(IUser user) {
		userActions.put(user, new LinkedList<>());
	}

	public static void disableUserActions(IUser user) {
		userActions.put(user, null);
	}

	public static boolean addAction(IUser user, Command command) {
		if (!hasUserActions(user))
			enableUserActions(user);
		if (!userActions.get(user).contains(command))
			return userActions.get(user).add(command);
		return false;
	}

	public static void handleCmd(IMessage msg) {
		String nice = removeNoise(msg.getContent());
		boolean inverted = nice.matches(".*\\b((do not)|don't|stop|quit|dnot|dont|dno't)\\b" + IGNORE_QUOTES + ".*");
		if (nice.matches(".*\\b(copy|copyeth|copy-eth|copying|copeh|cp)\\b \\b" + ME + "\\b" + IGNORE_QUOTES + ".*"))
			if (!inverted)
				actions.get("copy").act(Uni.client, msg);
			else
				delayedMsg(msg.getChannel(),
						disableUserAction(msg.getAuthor(), "copy") ? "Fine..." : "No can do, you have a bug!");
		else if (nice.matches("\\b(call)\\b \\b" + ME + "\\b.*")) {
			String nick = normalSpaces(nice.split("\\b(call)\\b \\b" + Basilicon.ME + "\\b")[1]);
			Kei.retrieveCitizen(msg.getAuthor(), msg.getGuild()).setNick(nick);
			delayedMsg(msg.getChannel(), "Alright, so " + nick + "?");
		} else
			delayedMsg(msg.getChannel(),
					"Hm? Hey there " + Kei.retrieveCitizen(msg.getAuthor(), msg.getGuild()).getNick() + "...");
	}

	public static boolean disableUserAction(IUser user, String name) {
		if (!hasUserActions(user))
			return false;
		return userActions.get(user).remove(new Command(name));
	}

	public static boolean delayedMsg(IChannel ch, String msg) {
		new Thread(() -> {
			if (!ch.getTypingStatus())
				ch.toggleTypingStatus();
			try {
				Thread.sleep((int) Math.round(Math.random() * 900 + 100));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			RequestBuffer.request(() -> {
				try {
					return Uni.msgr.withChannel(ch).withContent(msg).build();
				} catch (DiscordException e) {
					e.printStackTrace();
				} catch (MissingPermissionsException e) {
					e.printStackTrace();
				}
				return null;
			});
			if (ch.getTypingStatus())
				ch.toggleTypingStatus();
		}).start();
		return true;
	}

}