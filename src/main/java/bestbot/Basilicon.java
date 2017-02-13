package bestbot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

/**
 * This is where most of the static code goes.
 */
public class Basilicon {

	// Append this regex to ignore quotes
	private static final String IGNORE_QUOTES = "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

	// Include this regex for the various "me" words
	private static final String ME = "(me|myself|I|moi|meh|m|em)";

	// Various ways of saying "uni"
	private static final String UNI = "uni( |\\.|\\?|\\!||-chan|-nee(|-chan))";

	/**
	 * A map representing certain strings to their respective actions.
	 */
	public static Map<String, Action> actions;

	/**
	 * A map representing the commands to be executed upon each message a user
	 * sends.
	 */
	public static Map<IUser, List<Command>> userActions;

	/**
	 * Normalizes spaces. Effectively the same as
	 * <code>msg.trim().replaceAll(" +", " ");</code>
	 * 
	 * @param msg
	 *            - The message string.
	 * @return A trimmed string with only monospace gaps between words and
	 *         phrases.
	 */
	public static String normalSpaces(String msg) {
		return msg.trim().replaceAll(" +", " ");
	}

	/**
	 * This detects if a command may be contained within a string
	 * 
	 * @param msg
	 *            - The message string
	 * @return True if there may be a command. Note that this isn't with
	 *         complete certainty, but it does tell you if someone meant to use
	 *         one, at the very least. Otherwise, it will return false.
	 */
	public static boolean detectCmd(String msg) {
		return msg.toLowerCase().matches(".*\\b(yo|hey)( |,).*\\b" + UNI + "\\b" + IGNORE_QUOTES + ".*")
				|| msg.toLowerCase().matches(".*\\balright(,| |then(|,)|y then(,|)|)\\b \\b" + UNI + "(|,|!|.|\\?)\\b"
						+ IGNORE_QUOTES + ".*");
	}

	/**
	 * This method attempts to remove extraneous text from the message. One day,
	 * we won't have to do that, but that day isn't today.
	 * 
	 * @param msg
	 *            - The message string
	 * @return A simplified string that can then be regexed to search for
	 *         commands.
	 */
	public static String removeNoise(String msg) {
		return normalSpaces(msg.toLowerCase().replaceAll(
				"(\\b(hey|yo|alright)( |,|then(|,)|y then(,|))\\b|\\buni-nee-chan( |\\.|\\?|\\!|)\\b|\\buni-chan( |\\.|\\?|\\!|)\\b|"
						+ "\\buni-nee( |\\.|\\?|\\!|)\\b|\\buni( |\\.|\\?|\\!|)\\b|,|\\bcan you\\b|\\bplease\\b|\\!|\\?|\\.)"
						+ IGNORE_QUOTES,
				""));
	}

	/**
	 * Checks whether a user can use any commands.
	 * 
	 * @param user
	 *            - The user to check.
	 * @return True if the user has actions. Otherwise returns false.
	 */
	public static boolean hasUserActions(IUser user) {
		return userActions.get(user) != null;
	}

	/**
	 * Enables actions for a user (note that no actions are actually added)
	 * 
	 * @param user
	 *            - The user to enable actions for.
	 */
	public static void enableUserActions(IUser user) {
		userActions.put(user, new LinkedList<>());
	}

	/**
	 * Disables actions for a user (note that no actions are actually added)
	 * 
	 * @param user
	 *            - The user to enable actions for.
	 */
	public static void disableUserActions(IUser user) {
		userActions.put(user, null);
	}

	/**
	 * Adds an action to a user.
	 * 
	 * @param user
	 *            - The user to add actions to.
	 * @param command
	 *            - The command to append to the user's actions list
	 * @return Returns true if successfully added the command, false otherwise.
	 */
	public static boolean addAction(IUser user, Command command) {
		if (!hasUserActions(user))
			enableUserActions(user);
		if (!userActions.get(user).contains(command))
			return userActions.get(user).add(command);
		return false;
	}

	/**
	 * Handles a command and conducts the appropriate action related to the
	 * command.
	 * 
	 * @param msg
	 *            - The message string, unsimplified.
	 */
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

	/**
	 * Disables an action for a specific user.
	 * 
	 * @param user
	 *            - The user.
	 * @param name
	 *            - The name of the command.
	 * @return Returns true if the user both has the command and it was
	 *         successfully removed from his list of avaliable commands.
	 */
	public static boolean disableUserAction(IUser user, String name) {
		if (!hasUserActions(user))
			return false;
		return userActions.get(user).remove(new Command(name));
	}

	/**
	 * Sends a message that simulates actual typing of the message.
	 * 
	 * @param ch
	 *            - The channel to send the message out of.
	 * @param msg
	 *            - The message string.
	 * @return Returns true once the message was successfully sent.
	 */
	public static boolean delayedMsg(IChannel ch, String msg) {
		new Thread(() -> {
			if (!ch.getTypingStatus()) // If not typing, then begin "typing"
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

	public static long frequency(String phrase, IChannel ch) {
		MessageList.shouldDownloadHistoryAutomatically(true);
		MessageList list = ch.getMessages();
		list.setCacheCapacity(MessageList.UNLIMITED_CAPACITY);
		System.out.printf("%d %s %d%n", list.size(), list.get(0), list.size());
		Pattern p = Pattern.compile(phrase);
		long count = 0;
		for (IMessage msg : list) {
			String content = msg.getContent();
			Matcher m = p.matcher(content);
			while (m.find())
				count++;
		}
		return count;
	}

	public static int countChar(String s, String c) {
		s = s.toLowerCase();
		c = c.toLowerCase();
		return s.length() - s.replace(c, "").length();
	}

}