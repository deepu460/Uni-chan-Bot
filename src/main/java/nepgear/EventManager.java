package nepgear;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import bestbot.Basilicon;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class EventManager {
	// These regex are used for certain commands
	private static final String FREQ_REGEX = "(?i).*freq(uency|) of \"(.*)\".*";

	private static final String REF_REGEX = "(?i).*ref(resh|) \"(.*)\".*";

	private static final String CHILL_REGEX = "(?i).*chill (-?[0-9.]+)%.*";

	/**
	 * Name of the bot...should always be Gear-chan, but it's subject to
	 * change...
	 */
	private String name;

	/**
	 * Quirks are things that nepgear says even though she is not explicitly
	 * mentioned
	 */
	private static Map<String, String> quirks;

	/**
	 * Responses are things that nepgear will respond to when certain key
	 * phrases are said in conjunction with a mention
	 */
	private static Map<String, String> responses;

	@EventSubscriber
	public void onReadyEvent(ReadyEvent e) {
		System.out.println("Ready~!");
		Nepgear.msgr = new MessageBuilder(Nepgear.client);
		IUser me = Nepgear.client.getOurUser();
		name = me.getName() + "#" + me.getDiscriminator();
		System.out.println("Logged in as " + name);
		ImageManager.init();
		initQuirks();
		initResponses();
		Nepgear.setChill(0);
	}

	@EventSubscriber
	public void onMessageEvent(MessageReceivedEvent e) {
		IMessage msg = e.getMessage();
		IChannel ch = msg.getChannel();
		String content = Normalizer.normalize(msg.getContent(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		List<String> mojis = msg.getGuild().getEmojis().stream().map(moji -> moji.getName())
				.collect(Collectors.toList());
		String sender = msg.getAuthor().getName();
		double response = Math.random();
		// TODO: Fix this terrible if-else-if chain
		try {
			if (!msg.getAuthor().isBot()) {
				// Check that this is a mention...
				if (content.contains("<@" + Nepgear.client.getOurUser().getID() + ">")) {
					// This one is special since accents may be searched for...
					if (msg.getContent().matches(FREQ_REGEX))
						freq(msg.getContent(), ch);
					else if (content.matches(REF_REGEX))
						refresh(content, ch);
					else if (content.matches(CHILL_REGEX))
						chillify(content, ch);
					else if (content.matches("(?i).*(hey|hi|hello|yo).*"))
						Nepgear.say(ch, "Hi " + sender + "!");
					else if (content.matches(".*guilty.*"))
						Nepgear.sayImage(ch, ImageManager.fetchRandomPic("guilt.txt"),
								"Let's see what the judge has to say about that...");
					else {
						if (content.matches(".*chill.*"))
							Nepgear.reportChill(ch);
						tryResponse(content, ch, responses);
					}
				} else if (content.matches("back"))
					Nepgear.say(ch, "Welcome back " + sender + "~!");
				else if (content.matches("(?i)(good |gud |')night(|!)"))
					Nepgear.say(ch, "Good night " + sender + "~!");
				else if (response >= Nepgear.chillness()) {
					if (content.matches("(?i).*trash.*"))
						Nepgear.sayImage(ch, ImageManager.fetchRandomPic("trash.txt"), "Pfft, trash!");
					else if (content.matches("(?i)nani(\\?|!|)") || content.matches("(?i)w(ha|u)t(|\\?)"))
						Nepgear.sayImage(ch, ImageManager.fetchRandomPic("nep-shock.txt"), "😲");
					else if (content.matches("(?i).*mlg.*"))
						Nepgear.sayImage(ch, ImageManager.fetchRandomPic("mlg.txt"), "😎");
					else if (content.matches("(?i).*rek.*"))
						Nepgear.sayImage(ch, ImageManager.fetchRandomPic("rekt.txt"), "🔥");
					else if (content.matches("(?i)i(c| c)"))
						Nepgear.say(ch, "Do you hear that guys? " + sender + " sees~!");
					else if (content.matches("(?i)o"))
						Nepgear.say(ch, "I know right, " + sender + "? Shocking...");
					else if (content.matches("(?i)a[y]+"))
						RepResponse(ch, content, "y", "lma", "!", "o");
					else if (content.matches("(?i)no[o]+"))
						RepResponse(ch, content, "o", "", "", "o", "h");
					else if (content.matches("(?i)e[h]+"))
						RepResponse(ch, content, "h", "ble", "~?", "h");
					else if (content.matches("(?i)ble[h]+"))
						RepResponse(ch, content, "h", "eh", "~?", "h");
					else if (content.matches("(?i)lma[o]+") || content.matches("[I|\\\\|]ma[o]+"))
						RepResponse(ch, content, "o", "a", "", "y");
					else if (content.matches("(?i)pl[z]+"))
						RepResponse(ch, content, "z", "n", "", "o");
					else if (content.matches("\\.\\.[\\.]+"))
						Nepgear.say(ch, content);
					else if (content.matches("(?i)y[e]+")) {
						msg.delete();
						Nepgear.say(ch, "Yeah ~Fixed " + sender + "✨!");
					} else
						tryResponse(content, ch, quirks);
				}
				// This is an easter egg
				for (String moji : mojis)
					if (content.matches("(?i).*" + moji + ".*"))
						msg.addReaction(msg.getGuild().getEmojiByName(moji));
			}
		} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
			e1.printStackTrace();
		}
		System.out.println("[MSG][" + msg.getAuthor().getName() + "]: " + msg.getContent());
	}

	private static void tryResponse(String content, IChannel ch, Map<String, String> map)
			throws RateLimitException, DiscordException, MissingPermissionsException {
		boolean clever = true;
		Set<Entry<String, String>> entries = map.entrySet();
		for (Entry<String, String> entry : entries)
			if (content.matches("(?i)" + entry.getKey()))
				Nepgear.say(ch, entry.getValue());
			else if (clever)
				clever = !clever;
		if (clever) {
			Nepgear.say(ch, "I bet you thought you were really clever, entering in all of my keywords...");
			Nepgear.say(ch, "Sadly for you, I noticed 😛");
		}
	}

	/**
	 * This maps certain phrases within messages to an image list.
	 */
	private static void triggeredImages() {
		// TODO: Implement an easier way to respond with images from input
	}

	/**
	 * A quirk just detects if a certain phrase is contained within a message.
	 * If so, Nepgear will take the appropriate response from the mapping...
	 */
	private static void initQuirks() {
		quirks = new HashMap<>();
		quirks.put("ya", "ikr");
		quirks.put("gg", "no re");
		quirks.put("rip", "in pepperoni");
		quirks.put("help", "_sends help_");
		quirks.put("D:", "!?!");
		quirks.put("let(|')s get down to business", "...and defeat the huns!");
		quirks.put(".*[┻|┸][━]*+[┻|┸].*", "┬──┬ ノ( ゜-゜ノ)");
		quirks.put("also", "Also?");
		quirks.put("ah well", "All's well that ends well...");
		quirks.put("😐", "😛");
		quirks.put("same", "same...");
		quirks.put("le(z|t's|ts) go", "Onwards ho!");
		quirks.put("welp", "Well you know...");
	}

	private static void initResponses() {
		responses = new HashMap<>();
		responses.put(".*lenny.*", "( ͡° ͜ʖ ͡°)");
		responses.put(".*table( |)flip.*", "(╯°□°）╯︵ ┻━┻");
		responses.put(".*gitgud.*", "I'm trying sempai~!");
		responses.put(".*morning.*", "Good morning~!");
		responses.put(".*evening.*", "Good evening~!");
		responses.put(".*afternoon.*", "Good afternoon~!");
		responses.put(".*night.*", "Good night~!");
	}

	/**
	 * Gives a response and repeats certain parts dependent on user input
	 * 
	 * @param ch
	 *            - channel
	 * @param content
	 *            - original message
	 * @param c
	 *            - char to count
	 * @param prefix
	 *            - prefix to add at the beginning
	 * @param final
	 *            - last string to be added, after the suffixes
	 * @param suffixes
	 *            - suffixes to add, in order, ad the end
	 */
	private static void RepResponse(IChannel ch, String content, String c, String prefix, String finish,
			String... suffixes) throws RateLimitException, DiscordException, MissingPermissionsException {
		int count = Basilicon.countChar(content, c);
		String response = prefix;
		for (String s : suffixes)
			for (int i = 0; i < count; i++)
				response += s;
		Nepgear.say(ch, response + finish);
	}

	/**
	 * Refreshes a list within <code>ImageManager</code>.
	 */
	private static void refresh(String content, IChannel ch)
			throws RateLimitException, DiscordException, MissingPermissionsException {
		Matcher m = Pattern.compile(REF_REGEX).matcher(content);
		if (!m.matches())
			return;
		String phrase = m.group(2);
		if (!phrase.endsWith(".txt"))
			phrase += ".txt";
		if (ImageManager.refreshFile(phrase))
			Nepgear.say(ch, "\"" + phrase + "\" successfully refreshed~!");
		else
			Nepgear.say(ch, "Couldn't find the file \"" + phrase + "\"...");

	}

	/**
	 * Finds the frequency of a word or phrase within the last few messages. No
	 * real use now that I've implemented it, but hey, maybe it'll tell you just
	 * how often you spam something...
	 */
	private static void freq(String content, IChannel ch)
			throws RateLimitException, DiscordException, MissingPermissionsException {
		Matcher m = Pattern.compile(FREQ_REGEX).matcher(content);
		if (!m.matches())
			return;
		String phrase = m.group(2);
		Nepgear.say(ch, "Finding frequency of \"" + phrase + "\"...");
		long freq = Basilicon.frequency(phrase, ch);
		if (freq == 0)
			Nepgear.say(ch, "Um...you've never said this before...not sure how?");
		if (freq == 1)
			Nepgear.say(ch, "You just said it for the first time right there...");
		else
			Nepgear.say(ch, String.format("\"%s\" has been said %,d times", phrase, freq));
	}

	/**
	 * Attempt to read a message and change the "chill" level of Nepgear
	 */
	private static void chillify(String content, IChannel ch)
			throws RateLimitException, MissingPermissionsException, DiscordException {
		Matcher m = Pattern.compile(CHILL_REGEX).matcher(content);
		if (!m.matches())
			return;
		String phrase = m.group(1);
		try {
			double k = Double.parseDouble(phrase);
			k *= 0.01;
			if (k == Nepgear.chillness())
				Nepgear.say(ch, "But I'm already that chill~!");
			else if (Nepgear.chillness() < k)
				Nepgear.say(ch, "Increasing chill");
			else
				Nepgear.say(ch, "Decreasing chill");
			Nepgear.setChill(k);
		} catch (NumberFormatException e) {
			Nepgear.say(ch, "Try a real number next time...");
		}
	}

}