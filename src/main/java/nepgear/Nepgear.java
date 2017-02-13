package nepgear;

import java.util.Scanner;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * The main "driver" class of this bot. Seperated from <code>Uni</code> by the
 * fact that she's a totally new bot with different functionality. Rather, the
 * two bots may be run in parallel...It's just convenient to have both bots in
 * the same place if they share some of the same boiler plate code...
 * 
 * @author Harsh Ramesh
 */
public class Nepgear {

	/**
	 * The client instance of the bot.
	 */
	public static IDiscordClient client;

	/**
	 * The standard message builder...it lets nepgear transmit messages
	 */
	public static MessageBuilder msgr;

	/**
	 * The more chill Nepgear is, the less likely she is to respond
	 */
	private static double chillLevel;

	/**
	 * The entry point to begin the Nepgear Bot.
	 */
	public static void main(String[] args) {
		// Initialize user input from console
		Scanner s = new Scanner(System.in);
		boolean successful = true; // Used in loop only
		// This loop asks for a valid token
		do {
			// Asserts initial validity of token
			successful = true;
			try {
				// Gets token from user
				String token = "";
				if (args.length < 1) {
					System.out.print("Enter token> ");
					token = s.nextLine();
				} else
					token = args[0];
				// Awaits confirmation before starting
				System.out.println("Press enter to start");
				s.nextLine();
				// Logs in
				client = getClient(token);
			} catch (DiscordException e) {
				// Login failed, try again
				successful = false;
			}
		} while (!successful);
		// Register the event manager to listen to messages
		client.getDispatcher().registerListener(new EventManager());
		// Await user input to stop the program
		while (true)
			if (s.nextLine().toLowerCase().matches("exit|quit"))
				break;
		// Close console and log out
		s.close();
		finish();
	}

	/**
	 * Says a message on a given channel
	 */
	public static void say(IChannel ch, String msg)
			throws RateLimitException, MissingPermissionsException, DiscordException {
		sayImage(ch, null, msg);
	}

	/**
	 * Says a message on a given channel with an image given by its URL
	 */
	public static void sayImage(IChannel ch, String imgURL, String msg)
			throws RateLimitException, MissingPermissionsException, DiscordException {
		// Checks for valid inputs
		if (ch == null || (imgURL == null && msg == null))
			return;
		// Breaks large messages into multiple parts
		if (msg.length() > 2000) { // 2k char limit in discord
			for (int i = 0; i < msg.length(); i += 2000)
				if (i + 2000 <= msg.length())
					say(ch, msg.substring(i, i + 2000));
				else
					say(ch, msg.substring(i));
			// If no image, no point in not printing it...
			if (imgURL != null)
				return;
			// Clear msg so that we won't print anything w/ this image
			msg = null;
		}
		// Asserts success of loop
		boolean success = true;
		do {
			// Re-assert with each iteration
			success = true;
			try {
				// Echo to console
				System.out.println("[MSG][SELF]: " + msg);
				// Build the message
				EmbedBuilder em = new EmbedBuilder();
				em.withImage(imgURL);
				msgr.withChannel(ch);
				// Only add message if there is one...
				if (msg != null)
					msgr.withContent(msg);
				// Only add an image if there is one...
				if (imgURL != null)
					msgr.withEmbed(em.build());
				// Send the message
				msgr.build();
				// Wipe the message builder
				msgr.withEmbed(null);
				msgr.withContent(null);
			} catch (RateLimitException e) {
				// We've failed!
				success = false;
				// Wait out the delay, then retry
				try {
					Thread.sleep(e.getRetryDelay());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (DiscordException e) {
				if (e.getCause().getMessage().equals("Message was unable to be sent.")) {
					// An error in connection, retry after a brief delay
					success = false;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} else
					// Unknown cause, print to console
					e.printStackTrace();
			}
		} while (!success); // Repeat if unsuccessful
	}

	/**
	 * Log out of discord and any other final tasks
	 */
	public static void finish() {
		try {
			client.logout();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
		System.out.println("Bot turned off...");
	}

	/**
	 * Logs in and returns the client received from discord.
	 */
	public static IDiscordClient getClient(String token) throws DiscordException {
		return new ClientBuilder().withToken(token).login();
	}

	/**
	 * A measure of how often to respond to messages proactively
	 */
	public static double chillness() {
		return chillLevel;
	}

	/**
	 * Set how often to respond to messages proactively
	 */
	public static void setChill(double chill) {
		chillLevel = chill;
	}

	/**
	 * This says a message to a channel reporting her chill level, regulated
	 * through thresholds...
	 */
	public static void reportChill(IChannel ch)
			throws RateLimitException, MissingPermissionsException, DiscordException {
		if (chillLevel < 0)
			say(ch, "I'm way, waaaay, un~chill right now!");
		else if (chillLevel < 0.01)
			say(ch, "I have no chill, what are you talking about~?");
		else if (chillLevel < 0.1)
			say(ch, "I'm not _always_ talking~!");
		else if (chillLevel < 0.5)
			say(ch, "I'm sort of chill right now...");
		else if (chillLevel < 1)
			say(ch, "I'm pretty chill, you know~!");
		else
			say(ch, "Hm? I'm lurking-level chill xD");
	}

}