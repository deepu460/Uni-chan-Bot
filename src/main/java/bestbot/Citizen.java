package bestbot;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public class Citizen {

	/**
	 * Defines all of the ranks for a citizen and the powers (in the String
	 * array) associated with it. Can be modified at runtime, thus there is a
	 * fail-safe in place for server managers.
	 */
	public static Multimap<String, String> ranks;

	/**
	 * Initializes default ranks. One day, this will all be loaded from a file
	 * as opposed to manually hard-coded.
	 */
	private static void initRanks() {
		ranks = HashMultimap.create();
		// TODO: Load ranks dynamically from a file
		ranks.put("plebian", "");
		ranks.put("god", "all");
	}

	static {
		initRanks(); // Ensure that the ranks are preset
	}

	/**
	 * The actual user
	 */
	private IUser user;

	/**
	 * What Uni calls the user
	 */
	private String nick;

	/**
	 * User-specific commands that run w/ every sentence...
	 */
	private List<Command> cmds;

	/**
	 * The server the user is located on
	 */
	private IGuild server;

	/**
	 * Detects if the user is a server manager
	 */
	private boolean admin;

	/**
	 * The rank of the user, overridden by server manager privileges
	 */
	private String rank;

	/**
	 * Generates a citizen for a given user within a server.
	 * 
	 * @param user
	 *            - The user
	 * @param server
	 *            - The server
	 */
	public Citizen(IUser user, IGuild server) {
		this.user = user;
		nick = user.getName();
		cmds = new LinkedList<>();
		this.server = server;
		admin = user.getRolesForGuild(server).stream().map(n -> n.getPermissions())
				.filter(n -> n.contains(Permissions.MANAGE_SERVER)).count() > 0;
		rank = admin ? "god" : "plebian";
	}

	public IUser getUser() {
		return user;
	}

	public void setUser(IUser user) {
		this.user = user;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public List<Command> getCmds() {
		return cmds;
	}

	public void setCmds(List<Command> cmds) {
		this.cmds = cmds;
	}

	public IGuild getServer() {
		return server;
	}

	public void setServer(IGuild server) {
		this.server = server;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

}