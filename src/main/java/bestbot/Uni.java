package bestbot;

import static bestbot.Basilicon.actions;
import static bestbot.Basilicon.userActions;

import java.util.HashMap;
import java.util.Scanner;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MessageBuilder;

public class Uni {

	public static IDiscordClient client;

	public static MessageBuilder msgr;

	public static Kei kei;

	private static final Scanner s = new Scanner(System.in);

	public static void main(String[] args) {
		while (true) {
			boolean successful = true;
			try {
				String token = "";
				if (args.length < 1) {
					System.out.print("Enter token> ");
					token = s.nextLine();
				} else
					token = args[0];
				System.out.println("Press enter to start");
				s.nextLine();
				client = getClient(token);
			} catch (DiscordException e) {
				successful = false;
			}
			if (successful)
				break;
		}
		kei = new Kei();
		cmdinit();
		client.getDispatcher().registerListener(kei);
		while (true)
			if (s.nextLine().toLowerCase().matches("exit|quit"))
				break;
		end();
	}

	public static void end() {
		try {
			client.logout();
		} catch (HTTP429Exception | DiscordException e) {
			e.printStackTrace();
		}
		s.close();
		System.out.println("\nGoodbye for now...");
		System.exit(0);
	}

	public static IDiscordClient getClient(String token) throws DiscordException {
		return new ClientBuilder().withToken(token).login();
	}

	public static void cmdinit() {
		actions = new HashMap<>();
		userActions = new HashMap<>();
		actions.put("copy", (client, msg) -> {
			Basilicon.addAction(msg.getAuthor(),
					new Command("copy", (c, m) -> Basilicon.delayedMsg(m.getChannel(), m.getContent())));
			Basilicon.delayedMsg(msg.getChannel(), "It's just my job, i-it's not like I want to or anything, ok?");
		});
	}

}