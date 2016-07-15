package bestbot;

import java.util.LinkedList;
import java.util.List;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public class Citizen {

	private IUser user;

	// What Uni calls the user
	private String nick;

	// User-specific commands that run w/ every sentance...
	private List<Command> cmds;

	// The server the user is located on
	private IGuild server;

	// Detects if the user is a server manager
	private boolean admin;

	public Citizen(IUser user, IGuild server) {
		this.user = user;
		nick = user.getName();
		cmds = new LinkedList<>();
		this.server = server;
		admin = user.getRolesForGuild(server).stream().map(n -> n.getPermissions())
				.filter(n -> n.contains(Permissions.MANAGE_SERVER)).count() > 0;
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

}