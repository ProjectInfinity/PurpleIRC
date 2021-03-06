package com.cnaude.purpleirc;

import com.cnaude.purpleirc.IRCListeners.ActionListener;
import com.cnaude.purpleirc.IRCListeners.ConnectListener;
import com.cnaude.purpleirc.IRCListeners.DisconnectListener;
import com.cnaude.purpleirc.IRCListeners.JoinListener;
import com.cnaude.purpleirc.IRCListeners.KickListener;
import com.cnaude.purpleirc.IRCListeners.MessageListener;
import com.cnaude.purpleirc.IRCListeners.ModeListener;
import com.cnaude.purpleirc.IRCListeners.MotdListener;
import com.cnaude.purpleirc.IRCListeners.NickChangeListener;
import com.cnaude.purpleirc.IRCListeners.NoticeListener;
import com.cnaude.purpleirc.IRCListeners.PartListener;
import com.cnaude.purpleirc.IRCListeners.PrivateMessageListener;
import com.cnaude.purpleirc.IRCListeners.QuitListener;
import com.cnaude.purpleirc.IRCListeners.ServerResponseListener;
import com.cnaude.purpleirc.IRCListeners.TopicListener;
import com.cnaude.purpleirc.IRCListeners.VersionListener;
import com.cnaude.purpleirc.IRCListeners.WhoisListener;
import com.cnaude.purpleirc.Utilities.CaseInsensitiveMap;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Herochat;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedSet;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.nyancraft.reportrts.data.Ticket;
import com.titankingdoms.dev.titanchat.core.participant.Participant;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.events.BlockStateChange;
import org.bukkit.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;

/**
 *
 * @author Chris Naude
 */
public final class PurpleBot {

    private PircBotX bot;

    public final PurpleIRC plugin;
    private final File file;
    private YamlConfiguration config;
    private boolean connected;
    public boolean autoConnect;
    public boolean ssl;
    public boolean trustAllCerts;
    public boolean sendRawMessageOnConnect;
    public boolean showMOTD;
    public boolean channelCmdNotifyEnabled;
    public boolean relayPrivateChat;
    public boolean partInvalidChannels;
    public int botServerPort;
    public long chatDelay;
    public String botServer;
    public String bindAddress;
    public String botNick;
    public String botLogin;
    public String botRealName;
    public int ircMaxLineLength;
    public String botServerPass;
    public String charSet;
    public String commandPrefix;
    public String quitMessage;
    public String botIdentPassword;
    public String rawMessage;
    public String channelCmdNotifyMode;
    public String partInvalidChannelsMsg;
    private String connectMessage;
    public ArrayList<String> botChannels;
    public CaseInsensitiveMap<Collection<String>> channelNicks;
    public CaseInsensitiveMap<Collection<String>> tabIgnoreNicks;
    public CaseInsensitiveMap<Collection<String>> filters;
    public CaseInsensitiveMap<String> channelPassword;
    public CaseInsensitiveMap<String> channelTopic;
    public CaseInsensitiveMap<Boolean> channelTopicChanserv;
    public CaseInsensitiveMap<String> activeTopic;
    public CaseInsensitiveMap<String> channelModes;
    public CaseInsensitiveMap<String> joinMsg;
    public CaseInsensitiveMap<Boolean> msgOnJoin;
    public CaseInsensitiveMap<Boolean> channelTopicProtected;
    public CaseInsensitiveMap<Boolean> channelAutoJoin;
    public long channelAutoJoinDelay;
    public CaseInsensitiveMap<Boolean> ignoreIRCChat;
    public CaseInsensitiveMap<Boolean> hideJoinWhenVanished;
    public CaseInsensitiveMap<Boolean> hideListWhenVanished;
    public CaseInsensitiveMap<Boolean> hideQuitWhenVanished;
    public CaseInsensitiveMap<Boolean> invalidCommandPrivate;
    public CaseInsensitiveMap<Boolean> invalidCommandCTCP;
    public CaseInsensitiveMap<Boolean> logIrcToHeroChat;
    public CaseInsensitiveMap<Boolean> enableMessageFiltering;
    private final CaseInsensitiveMap<Boolean> shortify;
    public CaseInsensitiveMap<String> heroChannel;
    public CaseInsensitiveMap<String> townyChannel;
    public CaseInsensitiveMap<Collection<String>> opsList;
    public CaseInsensitiveMap<Collection<String>> voicesList;
    public CaseInsensitiveMap<Collection<String>> worldList;
    public CaseInsensitiveMap<Collection<String>> muteList;
    public CaseInsensitiveMap<Collection<String>> enabledMessages;
    public CaseInsensitiveMap<CaseInsensitiveMap<CaseInsensitiveMap<String>>> commandMap;
    public ArrayList<CommandSender> whoisSenders;
    public List<String> channelCmdNotifyRecipients;
    public List<String> channelCmdNotifyIgnore;
    private final ArrayList<ListenerAdapter> ircListeners;
    public IRCMessageQueueWatcher messageQueue;
    private final String fileName;

    /**
     *
     * @param file
     * @param plugin
     */
    public PurpleBot(File file, PurpleIRC plugin) {
        fileName = file.getName();
        this.connected = false;
        this.botChannels = new ArrayList<>();
        this.ircListeners = new ArrayList<>();
        this.channelCmdNotifyRecipients = new ArrayList<>();
        this.channelCmdNotifyIgnore = new ArrayList<>();
        this.commandMap = new CaseInsensitiveMap<>();
        this.enabledMessages = new CaseInsensitiveMap<>();
        this.muteList = new CaseInsensitiveMap<>();
        this.worldList = new CaseInsensitiveMap<>();
        this.opsList = new CaseInsensitiveMap<>();
        this.voicesList = new CaseInsensitiveMap<>();
        this.heroChannel = new CaseInsensitiveMap<>();
        this.townyChannel = new CaseInsensitiveMap<>();
        this.invalidCommandCTCP = new CaseInsensitiveMap<>();
        this.logIrcToHeroChat = new CaseInsensitiveMap<>();
        this.shortify = new CaseInsensitiveMap<>();
        this.invalidCommandPrivate = new CaseInsensitiveMap<>();
        this.hideQuitWhenVanished = new CaseInsensitiveMap<>();
        this.hideListWhenVanished = new CaseInsensitiveMap<>();
        this.hideJoinWhenVanished = new CaseInsensitiveMap<>();
        this.ignoreIRCChat = new CaseInsensitiveMap<>();
        this.channelAutoJoin = new CaseInsensitiveMap<>();
        this.channelTopicProtected = new CaseInsensitiveMap<>();
        this.channelModes = new CaseInsensitiveMap<>();
        this.activeTopic = new CaseInsensitiveMap<>();
        this.channelTopic = new CaseInsensitiveMap<>();
        this.channelPassword = new CaseInsensitiveMap<>();
        this.tabIgnoreNicks = new CaseInsensitiveMap<>();
        this.filters = new CaseInsensitiveMap<>();
        this.channelNicks = new CaseInsensitiveMap<>();
        this.channelTopicChanserv = new CaseInsensitiveMap<>();
        this.joinMsg = new CaseInsensitiveMap<>();
        this.msgOnJoin = new CaseInsensitiveMap<>();
        this.enableMessageFiltering = new CaseInsensitiveMap<>();
        this.plugin = plugin;
        this.file = file;
        whoisSenders = new ArrayList<>();
        config = new YamlConfiguration();
        loadConfig();
        addListeners();
        buildBot();
        messageQueue = new IRCMessageQueueWatcher(this, plugin);
    }

    public void buildBot() {
        Configuration.Builder configBuilder = new Configuration.Builder()
                .setName(botNick)
                .setLogin(botLogin)
                .setAutoNickChange(true)
                .setCapEnabled(true)
                .setMessageDelay(chatDelay)
                .setRealName(botRealName)
                .setMaxLineLength(ircMaxLineLength)
                //.setAutoReconnect(autoConnect) // Why doesn't this work?
                .setServer(botServer, botServerPort, botServerPass);
        //addAutoJoinChannels(configBuilder);
        for (ListenerAdapter ll : ircListeners) {
            configBuilder.addListener(ll);
        }
        if (!botIdentPassword.isEmpty()) {
            plugin.logInfo("Setting IdentPassword ...");
            configBuilder.setNickservPassword(botIdentPassword);
        }
        if (ssl) {
            UtilSSLSocketFactory socketFactory = new UtilSSLSocketFactory();
            socketFactory.disableDiffieHellman();
            if (trustAllCerts) {
                plugin.logInfo("Enabling SSL and trusting all certificates ...");
                socketFactory.trustAllCertificates();
            } else {
                plugin.logInfo("Enabling SSL ...");
            }
            configBuilder.setSocketFactory(socketFactory);
        }
        if (charSet.isEmpty()) {
            plugin.logInfo("Using default character set: " + Charset.defaultCharset());
        } else {
            if (Charset.isSupported(charSet)) {
                plugin.logInfo("Using character set: " + charSet);
                configBuilder.setEncoding(Charset.forName(charSet));
            } else {
                plugin.logError("Invalid character set: " + charSet);
                plugin.logInfo("Available character sets: " + Joiner.on(", ").join(Charset.availableCharsets().keySet()));
                plugin.logInfo("Using default character set: " + Charset.defaultCharset());
            }
        }
        if (!bindAddress.isEmpty()) {
            plugin.logInfo("Binding to " + bindAddress);
            try {
                configBuilder.setLocalAddress(InetAddress.getByName(bindAddress));
            } catch (UnknownHostException ex) {
                plugin.logError(ex.getMessage());
            }
        }
        Configuration configuration = configBuilder.buildConfiguration();
        bot = new PircBotX(configuration);
        if (autoConnect) {
            asyncConnect();
        } else {
            plugin.logInfo("Auto-connect is disabled. To connect: /irc connect " + bot.getNick());
        }
        plugin.logInfo("Max line length: " + configBuilder.getMaxLineLength());
    }

    private void addListeners() {
        ircListeners.add(new ActionListener(plugin, this));
        ircListeners.add(new ConnectListener(plugin, this));
        ircListeners.add(new DisconnectListener(plugin, this));
        ircListeners.add(new JoinListener(plugin, this));
        ircListeners.add(new KickListener(plugin, this));
        ircListeners.add(new MessageListener(plugin, this));
        ircListeners.add(new ModeListener(plugin, this));
        ircListeners.add(new NickChangeListener(plugin, this));
        ircListeners.add(new NoticeListener(plugin, this));
        ircListeners.add(new PartListener(plugin, this));
        ircListeners.add(new PrivateMessageListener(plugin, this));
        ircListeners.add(new QuitListener(plugin, this));
        ircListeners.add(new TopicListener(plugin, this));
        ircListeners.add(new VersionListener(plugin));
        ircListeners.add(new WhoisListener(plugin, this));
        ircListeners.add(new MotdListener(plugin, this));
        ircListeners.add(new ServerResponseListener(plugin, this));
    }

    /*
     private void addAutoJoinChannels(Configuration.Builder configBuilder) {
     for (String channelName : botChannels) {
     if (channelAutoJoin.containsKey(channelName)) {
     if (channelAutoJoin.get(channelName)) {
     if (channelPassword.get(channelName).isEmpty()) {
     configBuilder.addAutoJoinChannel(channelName);
     } else {
     configBuilder.addAutoJoinChannel(channelName, channelPassword.get(channelName));
     }
     }
     }
     }
     }
     */
    public void autoJoinChannels() {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (String channelName : botChannels) {
                    if (channelAutoJoin.containsKey(channelName)) {
                        if (channelAutoJoin.get(channelName)) {
                            if (channelPassword.get(channelName).isEmpty()) {
                                bot.sendIRC().joinChannel(channelName);
                            } else {
                                bot.sendIRC().joinChannel(channelName, channelPassword.get(channelName));
                            }
                        }
                    }
                }

            }
        }, channelAutoJoinDelay);

    }

    public void reload(CommandSender sender) {
        sender.sendMessage("Reloading bot: " + botNick);
        reload();
    }

    public void reload() {
        asyncQuit(true);
    }

    /**
     *
     * @param sender
     */
    public void reloadConfig(CommandSender sender) {
        config = new YamlConfiguration();
        loadConfig();
        sender.sendMessage("[PurpleIRC] [" + botNick + "] IRC bot configuration reloaded.");
    }

    /**
     *
     * @param channelName
     * @param sender
     * @param user
     */
    public void mute(String channelName, CommandSender sender, String user) {
        if (muteList.get(channelName).contains(user)) {
            sender.sendMessage("User '" + user + "' is already muted.");
        } else {
            sender.sendMessage("User '" + user + "' is now muted.");
            muteList.get(channelName).add(user);
            saveConfig();
        }
    }

    /**
     *
     * @param channelName
     * @param sender
     */
    public void muteList(String channelName, CommandSender sender) {
        if (muteList.get(channelName).isEmpty()) {
            sender.sendMessage("There are no users muted for " + channelName);
        } else {
            sender.sendMessage("Muted users for " + channelName
                    + ": " + Joiner.on(", ").join(muteList.get(channelName)));
            saveConfig();
        }
    }

    /**
     *
     * @param channelName
     * @param sender
     * @param user
     */
    public void unMute(String channelName, CommandSender sender, String user) {
        if (muteList.get(channelName).contains(user)) {
            sender.sendMessage("User '" + user + "' is no longer muted.");
            muteList.get(channelName).remove(user);
            saveConfig();
        } else {
            sender.sendMessage("User '" + user + "' is not muted.");
        }
    }

    public void asyncConnect(CommandSender sender) {
        sender.sendMessage(connectMessage);
        asyncConnect();
    }

    public boolean isShortifyEnabled(String channelName) {
        if (shortify.containsKey(channelName)) {
            return shortify.get(channelName);
        }
        return false;
    }

    /**
     *
     */
    public void asyncConnect() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.logInfo(connectMessage);
                    bot.startBot();
                } catch (IOException | IrcException ex) {
                    plugin.logError("Problem connecting to " + botServer + " => "
                            + " as " + botNick + " [Error: " + ex.getMessage() + "]");
                }
            }
        });
    }

    public void asyncIRCMessage(final String target, final String message) {
        plugin.logDebug("Entering aysncIRCMessage");
        messageQueue.add(new IRCMessage(target, plugin.colorConverter.
                gameColorsToIrc(message), false));
    }

    public void asyncCTCPMessage(final String target, final String message) {
        plugin.logDebug("Entering asyncCTCPMessage");
        messageQueue.add(new IRCMessage(target, plugin.colorConverter
                .gameColorsToIrc(message), true));
    }

    public void blockingIRCMessage(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        plugin.logDebug("[blockingIRCMessage] About to send IRC message to " + target);
        bot.sendIRC().message(target, plugin.colorConverter
                .gameColorsToIrc(message));
        plugin.logDebug("[blockingIRCMessage] Message sent to " + target);
    }

    public void blockingCTCPMessage(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        plugin.logDebug("[blockingCTCPMessage] About to send IRC message to " + target);
        bot.sendIRC().ctcpResponse(target, plugin.colorConverter
                .gameColorsToIrc(message));
        plugin.logDebug("[blockingCTCPMessage] Message sent to " + target);
    }

    public void asyncCTCPCommand(final String target, final String command) {
        if (!this.isConnected()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().ctcpCommand(target, command);
            }
        });
    }

    /**
     *
     * @param sender
     */
    public void saveConfig(CommandSender sender) {
        try {
            config.save(file);
            sender.sendMessage(plugin.LOG_HEADER_F
                    + " Saving bot \"" + botNick + "\" to " + file.getName());
        } catch (IOException ex) {
            plugin.logError(ex.getMessage());
            sender.sendMessage(ex.getMessage());
        }
    }

    /**
     *
     */
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException ex) {
            plugin.logError(ex.getMessage());
        }
    }

    /**
     *
     * @param sender
     * @param newNick
     */
    public void asyncChangeNick(final CommandSender sender, final String newNick) {
        if (!this.isConnected()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().changeNick(newNick);

            }
        });
        sender.sendMessage("Setting nickname to " + newNick);
        config.set("nick", newNick);
        saveConfig();
    }

    public void asyncJoinChannel(final String channelName, final String password) {
        if (!this.isConnected()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().joinChannel(channelName, password);
            }
        });
    }

    public void asyncNotice(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().notice(target, message);
            }
        });
    }

    public void asyncRawlineNow(final String message) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendRaw().rawLineNow(message);
            }
        });
    }

    public void asyncIdentify(final String password) {
        if (!this.isConnected()) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().identify(password);
            }
        });
    }

    /**
     *
     * @param sender
     * @param newLogin
     */
    public void changeLogin(CommandSender sender, String newLogin) {
        sender.sendMessage(ChatColor.DARK_PURPLE
                + "Login set to " + ChatColor.WHITE
                + newLogin + ChatColor.DARK_PURPLE
                + ". Reload the bot for the change to take effect.");
        config.set("login", newLogin);
        saveConfig();
    }

    private void sanitizeServerName() {
        botServer = botServer.replace("^.*\\/\\/", "");
        botServer = botServer.replace(":\\d+$", "");
        config.set("server", botServer);
        saveConfig();
    }

    private void loadConfig() {
        try {
            config.load(file);
            autoConnect = config.getBoolean("autoconnect", true);
            ssl = config.getBoolean("ssl", false);
            trustAllCerts = config.getBoolean("trust-all-certs", false);
            sendRawMessageOnConnect = config.getBoolean("raw-message-on-connect", false);
            rawMessage = config.getString("raw-message", "");
            relayPrivateChat = config.getBoolean("relay-private-chat", false);
            partInvalidChannels = config.getBoolean("part-invalid-channels", false);
            partInvalidChannelsMsg = config.getString("part-invalid-channels-message", "");
            botNick = config.getString("nick", "");
            plugin.loadTemplates(config, botNick);
            botLogin = config.getString("login", "PircBot");
            botRealName = config.getString("realname", "");
            ircMaxLineLength = config.getInt("max-line-length", 512);
            if (botRealName.isEmpty()) {
                botRealName = plugin.getServer()
                        .getPluginManager().getPlugin("PurpleIRC")
                        .getDescription().getWebsite();
            }
            botServer = config.getString("server", "");
            bindAddress = config.getString("bind", "");
            channelAutoJoinDelay = config.getLong("channel-auto-join-delay", 20);
            charSet = config.getString("charset", "");
            sanitizeServerName();
            showMOTD = config.getBoolean("show-motd", false);
            botServerPort = config.getInt("port");
            botServerPass = config.getString("password", "");
            botIdentPassword = config.getString("ident-password", "");
            commandPrefix = config.getString("command-prefix", ".");
            chatDelay = config.getLong("message-delay", 1000);
            plugin.logDebug("Message Delay => " + chatDelay);
            quitMessage = ChatColor.translateAlternateColorCodes('&', config.getString("quit-message", ""));
            plugin.logDebug("Nick => " + botNick);
            plugin.logDebug("Login => " + botLogin);
            plugin.logDebug("Server => " + botServer);
            plugin.logDebug("Channel Auto Join Delay => " + channelAutoJoinDelay);
            plugin.logDebug(("Bind => ") + bindAddress);
            plugin.logDebug("SSL => " + ssl);
            plugin.logDebug("Trust All Certs => " + trustAllCerts);
            plugin.logDebug("Port => " + botServerPort);
            plugin.logDebug("Command Prefix => " + commandPrefix);
            //plugin.logDebug("Server Password => " + botServerPass);
            plugin.logDebug("Quit Message => " + quitMessage);
            botChannels.clear();
            opsList.clear();
            voicesList.clear();
            muteList.clear();
            enabledMessages.clear();
            worldList.clear();
            commandMap.clear();

            channelCmdNotifyEnabled = config.getBoolean("command-notify.enabled", false);
            plugin.logDebug(" CommandNotifyEnabled => " + channelCmdNotifyEnabled);

            channelCmdNotifyMode = config.getString("command-notify.mode", "msg");
            plugin.logDebug(" channelCmdNotifyMode => " + channelCmdNotifyMode);

            // build command notify recipient list            
            for (String recipient : config.getStringList("command-notify.recipients")) {
                if (!channelCmdNotifyRecipients.contains(recipient)) {
                    channelCmdNotifyRecipients.add(recipient);
                }
                plugin.logDebug(" Command Notify Recipient => " + recipient);
            }
            if (channelCmdNotifyRecipients.isEmpty()) {
                plugin.logInfo(" No command recipients defined.");
            }
            
            // build command notify ignore list            
            for (String command : config.getStringList("command-notify.ignore")) {
                if (!channelCmdNotifyIgnore.contains(command)) {
                    channelCmdNotifyIgnore.add(command);
                }
                plugin.logDebug(" Command Notify Ignore => " + command);
            }
            if (channelCmdNotifyIgnore.isEmpty()) {
                plugin.logInfo(" No command-notify ignores defined.");
            }

            for (String enChannelName : config.getConfigurationSection("channels").getKeys(false)) {
                String channelName = decodeChannel(enChannelName);
                if (isValidChannel(channelName)) {
                    plugin.logError("Ignoring duplicate channel: " + channelName);
                    continue;
                }
                plugin.logDebug("Channel  => " + channelName);
                botChannels.add(channelName);

                channelAutoJoin.put(channelName, config.getBoolean("channels." + enChannelName + ".autojoin", true));
                plugin.logDebug("  Autojoin => " + channelAutoJoin.get(channelName));

                channelPassword.put(channelName, config.getString("channels." + enChannelName + ".password", ""));

                channelTopic.put(channelName, config.getString("channels." + enChannelName + ".topic", ""));
                plugin.logDebug("  Topic => " + channelTopic.get(channelName));

                channelModes.put(channelName, config.getString("channels." + enChannelName + ".modes", ""));
                plugin.logDebug("  Channel Modes => " + channelModes.get(channelName));

                channelTopicProtected.put(channelName, config.getBoolean("channels." + enChannelName + ".topic-protect", false));
                plugin.logDebug("  Topic Protected => " + channelTopicProtected.get(channelName).toString());

                channelTopicChanserv.put(channelName, config.getBoolean("channels." + enChannelName + ".topic-chanserv", false));
                plugin.logDebug("  Topic Chanserv Mode => " + channelTopicChanserv.get(channelName).toString());

                heroChannel.put(channelName, config.getString("channels." + enChannelName + ".hero-channel", ""));
                plugin.logDebug("  HeroChannel => " + heroChannel.get(channelName));

                townyChannel.put(channelName, config.getString("channels." + enChannelName + ".towny-channel", ""));
                plugin.logDebug("  TownyChannel => " + townyChannel.get(channelName));

                logIrcToHeroChat.put(channelName, config.getBoolean("channels." + enChannelName + ".log-irc-to-hero-chat", false));
                plugin.logDebug("  LogIrcToHeroChat => " + logIrcToHeroChat.get(channelName));

                ignoreIRCChat.put(channelName, config.getBoolean("channels." + enChannelName + ".ignore-irc-chat", false));
                plugin.logDebug("  IgnoreIRCChat => " + ignoreIRCChat.get(channelName));

                hideJoinWhenVanished.put(channelName, config.getBoolean("channels." + enChannelName + ".hide-join-when-vanished", true));
                plugin.logDebug("  HideJoinWhenVanished => " + hideJoinWhenVanished.get(channelName));

                hideListWhenVanished.put(channelName, config.getBoolean("channels." + enChannelName + ".hide-list-when-vanished", true));
                plugin.logDebug("  HideListWhenVanished => " + hideListWhenVanished.get(channelName));

                hideQuitWhenVanished.put(channelName, config.getBoolean("channels." + enChannelName + ".hide-quit-when-vanished", true));
                plugin.logDebug("  HideQuitWhenVanished => " + hideQuitWhenVanished.get(channelName));

                invalidCommandPrivate.put(channelName, config.getBoolean("channels." + enChannelName + ".invalid-command.private", false));
                plugin.logDebug("  InvalidCommandPrivate => " + invalidCommandPrivate.get(channelName));

                invalidCommandCTCP.put(channelName, config.getBoolean("channels." + enChannelName + ".invalid-command.ctcp", false));
                plugin.logDebug("  InvalidCommandCTCP => " + invalidCommandCTCP.get(channelName));

                shortify.put(channelName, config.getBoolean("channels." + enChannelName + ".shortify", true));
                plugin.logDebug("  Shortify => " + shortify.get(channelName));

                joinMsg.put(channelName, config.getString("channels." + enChannelName + ".raw-message", ""));
                plugin.logDebug("  JoinMessage => " + joinMsg.get(channelName));

                msgOnJoin.put(channelName, config.getBoolean("channels." + enChannelName + ".raw-message-on-join", false));
                plugin.logDebug("  SendMessageOnJoin => " + msgOnJoin.get(channelName));

                enableMessageFiltering.put(channelName, config.getBoolean("channels." + enChannelName + ".enable-filtering", false));
                plugin.logDebug("  EnableMessageFiltering => " + enableMessageFiltering.get(channelName));

                // build channel op list
                Collection<String> cOps = new ArrayList<>();
                for (String channelOper : config.getStringList("channels." + enChannelName + ".ops")) {
                    if (!cOps.contains(channelOper)) {
                        cOps.add(channelOper);
                    }
                    plugin.logDebug("  Channel Op => " + channelOper);
                }
                opsList.put(channelName, cOps);
                if (opsList.isEmpty()) {
                    plugin.logInfo("No channel ops defined.");
                }

                // build channel voice list
                Collection<String> cVoices = new ArrayList<>();
                for (String channelVoice : config.getStringList("channels." + enChannelName + ".voices")) {
                    if (!cVoices.contains(channelVoice)) {
                        cVoices.add(channelVoice);
                    }
                    plugin.logDebug("  Channel Voice => " + channelVoice);
                }
                voicesList.put(channelName, cVoices);
                if (voicesList.isEmpty()) {
                    plugin.logInfo("No channel voices defined.");
                }

                // build mute list
                Collection<String> m = new ArrayList<>();
                for (String mutedUser : config.getStringList("channels." + enChannelName + ".muted")) {
                    if (!m.contains(mutedUser)) {
                        m.add(mutedUser);
                    }
                    plugin.logDebug("  Channel Mute => " + mutedUser);
                }
                muteList.put(channelName, m);
                if (muteList.isEmpty()) {
                    plugin.logInfo("IRC mute list is empty.");
                }

                // build valid chat list
                Collection<String> c = new ArrayList<>();
                for (String validChat : config.getStringList("channels." + enChannelName + ".enabled-messages")) {
                    if (!c.contains(validChat)) {
                        c.add(validChat);
                    }
                    plugin.logDebug("  Enabled Message => " + validChat);
                }
                enabledMessages.put(channelName, c);
                if (enabledMessages.isEmpty()) {
                    plugin.logInfo("There are no enabled messages!");
                }

                // build valid world list
                Collection<String> w = new ArrayList<>();
                for (String validWorld : config.getStringList("channels." + enChannelName + ".worlds")) {
                    if (!w.contains(validWorld)) {
                        w.add(validWorld);
                    }
                    plugin.logDebug("  Enabled World => " + validWorld);
                }
                worldList.put(channelName, w);
                if (worldList.isEmpty()) {
                    plugin.logInfo("World list is empty!");
                }

                // build valid world list
                Collection<String> t = new ArrayList<>();
                for (String name : config.getStringList("channels." + enChannelName + ".custom-tab-ignore-list")) {
                    if (!t.contains(name)) {
                        t.add(name);
                    }
                    plugin.logDebug("  Tab Ignore => " + name);
                }
                tabIgnoreNicks.put(channelName, t);
                if (tabIgnoreNicks.isEmpty()) {
                    plugin.logInfo("World list is empty!");
                }

                // build valid world list
                Collection<String> f = new ArrayList<>();
                for (String word : config.getStringList("channels." + enChannelName + ".filter-list")) {
                    if (!f.contains(word)) {
                        f.add(word);
                    }
                    plugin.logDebug("  Filtered From IRC => " + word);
                }
                filters.put(channelName, f);
                if (filters.isEmpty()) {
                    plugin.logInfo("World list is empty!");
                }

                // build command map
                CaseInsensitiveMap<CaseInsensitiveMap<String>> map = new CaseInsensitiveMap<>();
                try {
                    for (String command : config.getConfigurationSection("channels." + enChannelName + ".commands").getKeys(false)) {
                        plugin.logDebug("  Command => " + command);
                        CaseInsensitiveMap<String> optionPair = new CaseInsensitiveMap<>();
                        String commandKey = "channels." + enChannelName + ".commands." + command + ".";
                        optionPair.put("modes", config.getString(commandKey + "modes", "*"));
                        optionPair.put("private", config.getString(commandKey + "private", "false"));
                        optionPair.put("ctcp", config.getString(commandKey + "ctcp", "false"));
                        optionPair.put("game_command", config.getString(commandKey + "game_command", "@help"));
                        optionPair.put("private_listen", config.getString(commandKey + "private_listen", "true"));
                        optionPair.put("channel_listen", config.getString(commandKey + "channel_listen", "true"));
                        optionPair.put("perm", config.getString(commandKey + "perm", ""));
                        for (String s : optionPair.keySet()) {
                            config.set(commandKey + s, optionPair.get(s));
                        }
                        map.put(command, optionPair);
                    }
                } catch (Exception ex) {
                    plugin.logError("No commands found for channel " + enChannelName);
                }
                commandMap.put(channelName, map);
                if (map.isEmpty()) {
                    plugin.logInfo("No commands specified!");
                }
                connectMessage = "Connecting to \"" + botServer + ":"
                        + botServerPort + "\" as \"" + botNick
                        + "\" [SSL: " + ssl + "]" + " [TrustAllCerts: "
                        + trustAllCerts + "]";
            }
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.logError(ex.getMessage());
        }
    }

    /**
     *
     * @param sender
     * @param delay
     */
    public void setIRCDelay(CommandSender sender, long delay) {
        config.set("message-delay", delay);
        saveConfig();
        sender.sendMessage(ChatColor.DARK_PURPLE
                + "IRC message delay changed to "
                + ChatColor.WHITE + delay + ChatColor.DARK_PURPLE + " ms. "
                + "Reload for the change to take effect.");
    }

    private boolean isPlayerInValidWorld(Player player, String channelName) {
        if (worldList.containsKey(channelName)) {
            if (worldList.get(channelName).contains("*")) {
                return true;
            }
            if (worldList.get(channelName).contains(player.getWorld().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called from normal game chat listener
     *
     * @param player
     * @param message
     */
    public void gameChat(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            if (plugin.fcHook != null) {
                String playerChatMode;
                String playerFactionName;
                try {
                    playerChatMode = plugin.fcHook.getChatMode(player);
                } catch (IllegalAccessError ex) {
                    plugin.logDebug("FC Error: " + ex.getMessage());
                    playerChatMode = "public";
                }
                try {
                    playerFactionName = plugin.fcHook.getFactionName(player);
                } catch (IllegalAccessError ex) {
                    plugin.logDebug("FC Error: " + ex.getMessage());
                    playerFactionName = "unknown";
                }

                String chatName = "faction-" + playerChatMode.toLowerCase() + "-chat";
                plugin.logDebug("Faction [Player: " + player.getName()
                        + "] [Tag: " + playerFactionName + "] [Mode: "
                        + playerChatMode + "]");
                if (enabledMessages.get(channelName)
                        .contains(chatName)) {
                    asyncIRCMessage(channelName, plugin.tokenizer
                            .chatFactionTokenizer(player, botNick, message,
                                    playerFactionName, playerChatMode));
                } else {
                    plugin.logDebug("Player " + player.getName() + " is in chat mode \""
                            + playerChatMode + "\" but \"" + chatName + "\" is disabled.");
                }
            } else {
                plugin.logDebug("No Factions");
            }
            if (isMessageEnabled(channelName, TemplateName.GAME_CHAT)) {
                plugin.logDebug("[" + TemplateName.GAME_CHAT + "] => "
                        + channelName + " => " + message);
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(botNick, TemplateName.GAME_CHAT), message));
            } else {
                plugin.logDebug("Ignoring message due to "
                        + TemplateName.GAME_CHAT + " not being listed.");
            }
        }
    }

    // Called from HeroChat listener
    /**
     *
     * @param chatter
     * @param chatColor
     * @param message
     */
    public void heroChat(Chatter chatter, ChatColor chatColor, String message) {
        if (!this.isConnected()) {
            return;
        }
        Player player = chatter.getPlayer();
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            String hChannel = chatter.getActiveChannel().getName();
            String hNick = chatter.getActiveChannel().getNick();
            String hColor = chatColor.toString();
            plugin.logDebug("HC Channel: " + hChannel);
            if (isMessageEnabled(channelName, "hero-" + hChannel + "-chat")
                    || isMessageEnabled(channelName, TemplateName.HERO_CHAT)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .chatHeroTokenizer(player, message, hColor, hChannel,
                                hNick, plugin.getHeroChatChannelTemplate(botNick, hChannel)));
            } else {
                plugin.logDebug("Player " + player.getName() + " is in \""
                        + hChannel + "\" but hero-" + hChannel + "-chat is disabled.");
            }
        }
    }

    public void mcMMOAdminChat(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            if (isMessageEnabled(channelName, TemplateName.MCMMO_ADMIN_CHAT)) {
                plugin.logDebug("Sending message because " + TemplateName.MCMMO_ADMIN_CHAT + " is enabled.");
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.MCMMO_ADMIN_CHAT), message));
            } else {
                plugin.logDebug("Player " + player.getName()
                        + " is in mcMMO AdminChat but " + TemplateName.MCMMO_ADMIN_CHAT + " is disabled.");
            }
        }
    }

    public void mcMMOPartyChat(Player player, String partyName, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            if (isMessageEnabled(channelName, TemplateName.MCMMO_PARTY_CHAT)) {
                plugin.logDebug("Sending message because " + TemplateName.MCMMO_PARTY_CHAT + " is enabled.");
                asyncIRCMessage(channelName, plugin.tokenizer
                        .mcMMOChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.MCMMO_PARTY_CHAT), message, partyName));
            } else {
                plugin.logDebug("Player " + player.getName()
                        + " is in mcMMO PartyChat but " + TemplateName.MCMMO_PARTY_CHAT + " is disabled.");
            }
        }
    }

    public void mcMMOChat(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            if (isMessageEnabled(channelName, TemplateName.MCMMO_CHAT)) {
                plugin.logDebug("Sending message because " + TemplateName.MCMMO_CHAT + " is enabled.");
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.MCMMO_CHAT), message));
            } else {
                plugin.logDebug("Player " + player.getName()
                        + " is in mcMMO Chat but " + TemplateName.MCMMO_CHAT + " is disabled.");
            }
        }
    }

    public void townyChat(Player player, com.palmergames.bukkit.TownyChat.channels.Channel townyChannel, String message) {
        if (!this.isConnected()) {
            return;
        }
        if (plugin.tcHook != null) {
            for (String channelName : botChannels) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    continue;
                }
                plugin.logDebug("townyChat: Checking for towny-"
                        + townyChannel.getName() + "-chat"
                        + " or " + "towny-" + townyChannel.getChannelTag() + "-chat"
                        + " or towny-chat");
                if (isMessageEnabled(channelName, "towny-" + townyChannel.getName() + "-chat")
                        || isMessageEnabled(channelName, "towny-" + townyChannel.getChannelTag() + "-chat")
                        || isMessageEnabled(channelName, "towny-chat")
                        || isMessageEnabled(channelName, "towny-channel-chat")) {
                    asyncIRCMessage(channelName, plugin.tokenizer
                            .chatTownyChannelTokenizer(player, townyChannel, message,
                                    plugin.getMsgTemplate(botNick, "towny-channel-chat")));
                }
            }
        }
    }

    public void heroAction(Chatter chatter, ChatColor chatColor, String message) {
        if (!this.isConnected()) {
            return;
        }
        Player player = chatter.getPlayer();
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            String hChannel = chatter.getActiveChannel().getName();
            String hNick = chatter.getActiveChannel().getNick();
            String hColor = chatColor.toString();
            plugin.logDebug("HC Channel: " + hChannel);
            if (isMessageEnabled(channelName, "hero-" + hChannel + "-action")
                    || isMessageEnabled(channelName, "hero-action")) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .chatHeroTokenizer(player, message, hColor, hChannel,
                                hNick, plugin.getHeroActionChannelTemplate(botNick, hChannel)));
            } else {
                plugin.logDebug("Player " + player.getName() + " is in \""
                        + hChannel + "\" but hero-" + hChannel + "-action is disabled.");
            }
        }
    }

    // Called from TitanChat listener
    /**
     *
     * @param participant
     * @param tChannel
     * @param tColor
     * @param message
     */
    public void titanChat(Participant participant, String tChannel, String tColor, String message) {
        if (!this.isConnected()) {
            return;
        }
        Player player = plugin.getServer().getPlayer(participant.getName());
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            plugin.logDebug("TC Channel: " + tChannel);
            if (isMessageEnabled(channelName, "titan-" + tChannel + "-chat")
                    || isMessageEnabled(channelName, "titan-chat")) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .titanChatTokenizer(player, tChannel, tColor, message,
                                plugin.getMsgTemplate(botNick, "titan-chat")));
            } else {
                plugin.logDebug("Player " + player.getName() + " is in \""
                        + tChannel + "\" but titan-" + tChannel + "-chat is disabled.");
            }
        }
    }

    // Called from /irc send
    /**
     *
     * @param player
     * @param channelName
     * @param message
     */
    public void gameChat(Player player, String channelName, String message) {
        if (!this.isConnected()) {
            return;
        }
        if (isValidChannel(channelName)) {
            asyncIRCMessage(channelName, plugin.tokenizer
                    .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                    botNick, TemplateName.GAME_SEND), message));
        }
    }

    // Called from CleverEvent
    /**
     *
     * @param cleverBotName
     * @param message
     */
    public void cleverChat(String cleverBotName, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, "clever-chat")) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(cleverBotName, plugin.getMsgTemplate(botNick, "clever-send"), message));
            }
        }
    }

    // Called from ReportRTS event
    /**
     *
     * @param pName
     * @param ticket
     * @param botNick
     * @param messageType
     */
    public void reportRTSNotify(String pName, Ticket ticket,
            String botNick, String messageType) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, messageType)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .reportRTSTokenizer(pName, plugin.getMsgTemplate(botNick, messageType), ticket));
            }
        }
    }

    public void reportRTSNotify(CommandSender sender, String message, String botNick, String messageType) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, messageType)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .reportRTSTokenizer(sender, message, plugin.getMsgTemplate(botNick, messageType)));
            }
        }
    }

    /**
     *
     * @param channelName
     * @param message
     */
    public void consoleChat(String channelName, String message) {
        if (!this.isConnected()) {
            return;
        }
        if (isValidChannel(channelName)) {
            asyncIRCMessage(channelName, plugin.tokenizer
                    .gameChatToIRCTokenizer("CONSOLE", message, plugin.getMsgTemplate(
                                    botNick, TemplateName.GAME_SEND)));
        }
    }

    /**
     *
     * @param message
     */
    public void consoleChat(String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.CONSOLE_CHAT)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(plugin.getMsgTemplate(botNick,
                                        TemplateName.CONSOLE_CHAT), ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameBroadcast(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.BROADCAST_MESSAGE)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin
                                .getMsgTemplate(botNick, TemplateName.BROADCAST_MESSAGE),
                                ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void essHelpOp(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.ESS_HELPOP)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin
                                .getMsgTemplate(botNick, TemplateName.ESS_HELPOP),
                                ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameOreBroadcast(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.ORE_BROADCAST)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(botNick, TemplateName.ORE_BROADCAST), ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param name
     * @param message
     * @param source
     */
    public void dynmapWebChat(String source, String name, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.DYNMAP_WEB_CHAT)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .dynmapWebChatToIRCTokenizer(source, name, plugin.getMsgTemplate(
                                        botNick, TemplateName.DYNMAP_WEB_CHAT),
                                ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param message
     */
    public void consoleBroadcast(String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.BROADCAST_CONSOLE_MESSAGE)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(plugin.getMsgTemplate(botNick,
                                        TemplateName.BROADCAST_CONSOLE_MESSAGE), ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }
    
    /**
     *
     * @param message
     */
    public void redditStreamBroadcast(String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.REDDIT_MESSAGES)) {
                plugin.logDebug("Checking if " + TemplateName.REDDIT_MESSAGES + " is enabled... YES");
                asyncIRCMessage(channelName, plugin.tokenizer.gameChatToIRCTokenizer(plugin.getMsgTemplate(botNick, TemplateName.REDDIT_MESSAGES), message));
            } else {
                plugin.logDebug("Checking if " + TemplateName.REDDIT_MESSAGES + " is enabled... NOPE");
            }
                
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameJoin(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_JOIN)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                if (hideJoinWhenVanished.get(channelName)) {
                    plugin.logDebug("Checking if player " + player.getName()
                            + " is vanished.");
                    if (plugin.vanishHook.isVanished(player)) {
                        plugin.logDebug("Not sending join message to IRC for player "
                                + player.getName() + " due to being vanished.");
                        continue;
                    }
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_JOIN), message));
            } else {
                plugin.logDebug("Not sending join message due to "
                        + TemplateName.GAME_JOIN + " being disabled");
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameQuit(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_QUIT)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                if (hideQuitWhenVanished.get(channelName)) {
                    plugin.logDebug("Checking if player " + player.getName()
                            + " is vanished.");
                    if (plugin.vanishHook.isVanished(player)) {
                        plugin.logDebug("Not sending quit message to IRC for player "
                                + player.getName() + " due to being vanished.");
                        continue;
                    }
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_QUIT), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param achievement
     */
    public void gameAchievement(Player player, Achievement achievement) {
        if (!this.isConnected()) {
            return;
        }
        String message = achievement.toString();
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_ACHIEVEMENT)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_ACHIEVEMENT), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param gameMode
     */
    public void gameModeChange(Player player, GameMode gameMode) {
        if (!this.isConnected()) {
            return;
        }
        String message = gameMode.toString();
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_MODE)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_MODE), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     * @param reason
     */
    public void gameKick(Player player, String message, String reason) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_KICK)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameKickTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_KICK), message, reason));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameAction(Player player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_ACTION)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, TemplateName.GAME_ACTION), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     * @param templateName
     */
    public void gameDeath(Player player, String message, String templateName) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, templateName)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                        botNick, templateName), message));
            }
        }
    }

    /**
     *
     * @param channelName
     * @param topic
     * @param sender
     */
    public void changeTopic(String channelName, String topic, CommandSender sender) {
        Channel channel = this.getChannel(channelName);
        String tTopic = tokenizedTopic(topic);
        if (channel != null) {
            setTheTopic(channel, tTopic);
            config.set("channels." + encodeChannel(getConfigChannelName(channelName)) + ".topic", topic);
            channelTopic.put(channelName, topic);
            saveConfig();
            sender.sendMessage("IRC topic for " + channelName + " changed to \"" + topic + "\"");
        } else {
            sender.sendMessage("Invalid channel: " + channelName);
        }
    }

    public String getConfigChannelName(String channelName) {
        for (String s : botChannels) {
            if (channelName.equalsIgnoreCase(s)) {
                return s;
            }
        }
        return channelName;
    }

    public Channel getChannel(String channelName) {
        Channel channel = null;
        for (Channel c : getChannels()) {
            if (c.getName().equalsIgnoreCase(channelName)) {
                return c;
            }
        }
        return channel;
    }

    /**
     *
     * @param sender
     * @param botServer
     */
    public void setServer(CommandSender sender, String botServer) {
        setServer(sender, botServer, autoConnect);
    }

    /**
     *
     * @param sender
     * @param server
     * @param auto
     */
    public void setServer(CommandSender sender, String server, Boolean auto) {

        if (server.contains(":")) {
            botServerPort = Integer.parseInt(server.split(":")[1]);
            botServer = server.split(":")[0];
        } else {
            botServer = server;
        }
        sanitizeServerName();
        autoConnect = auto;
        config.set("server", botServer);
        config.set("port", botServerPort);
        config.set("autoconnect", autoConnect);

        sender.sendMessage("IRC server changed to \"" + botServer + ":"
                + botServerPort + "\". (AutoConnect: "
                + autoConnect + ")");
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void addOp(String channelName, String userMask, CommandSender sender) {
        if (opsList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " is already in the ops list.");
        } else {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " has been added to the ops list.");
            opsList.get(channelName).add(userMask);
        }
        config.set("channels." + encodeChannel(getConfigChannelName(channelName)) + ".ops", opsList.get(channelName));
        saveConfig();
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void addVoice(String channelName, String userMask, CommandSender sender) {
        if (voicesList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " is already in the voices list.");
        } else {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " has been added to the voices list.");
            voicesList.get(channelName).add(userMask);
        }
        config.set("channels." + encodeChannel(getConfigChannelName(channelName)) + ".voices", voicesList.get(channelName));
        saveConfig();
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void removeOp(String channelName, String userMask, CommandSender sender) {
        if (opsList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " has been removed to the ops list.");
            opsList.get(channelName).remove(userMask);
        } else {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " is not in the ops list.");
        }
        config.set("channels." + encodeChannel(getConfigChannelName(channelName)) + ".ops", opsList.get(channelName));
        saveConfig();
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void removeVoice(String channelName, String userMask, CommandSender sender) {
        if (voicesList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " has been removed to the voices list.");
            voicesList.get(channelName).remove(userMask);
        } else {
            sender.sendMessage("User mask " + ChatColor.WHITE + userMask
                    + ChatColor.RESET + " is not in the voices list.");
        }
        config.set("channels." + encodeChannel(getConfigChannelName(channelName)) + ".voices", voicesList.get(channelName));
        saveConfig();
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void op(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().op(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void voice(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().voice(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void deOp(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().deOp(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void deVoice(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().deVoice(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void kick(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().kick(user);
                    return;
                }
            }
        }
    }

    private String encodeChannel(String s) {
        return s.replace(".", "%2E");
    }

    private String decodeChannel(String s) {
        return s.replace("%2E", ".");
    }

    /**
     *
     * @param channel
     * @param topic
     * @param setBy
     */
    public void fixTopic(Channel channel, String topic, String setBy) {
        String channelName = channel.getName();
        String tTopic = tokenizedTopic(topic);
        if (setBy.equals(botNick)) {
            //config.set("channels." + encodeChannel(channelName) + ".topic", topic);
            //saveConfig();
            return;
        }

        if (channelTopic.containsKey(channelName)) {
            if (channelTopicProtected.containsKey(channelName)) {
                if (channelTopicProtected.get(channelName)) {
                    plugin.logDebug("[" + channel.getName() + "] Topic protected.");
                    String myTopic = tokenizedTopic(channelTopic.get(channelName));
                    plugin.logDebug("rTopic: " + channelTopic.get(channelName));
                    plugin.logDebug("tTopic: " + tTopic);
                    plugin.logDebug("myTopic: " + myTopic);
                    if (!tTopic.equals(myTopic)) {
                        plugin.logDebug("Topic is not correct. Fixing it.");
                        setTheTopic(channel, myTopic);
                    } else {
                        plugin.logDebug("Topic is correct.");
                    }
                }
            }
        }
    }

    private void setTheTopic(Channel channel, String topic) {
        String myChannel = channel.getName();
        if (channelTopicChanserv.containsKey(myChannel)) {
            if (channelTopicChanserv.get(myChannel)) {
                String msg = String.format("TOPIC %s %s", myChannel, topic);
                plugin.logDebug("Sending chanserv rmessage: " + msg);
                asyncIRCMessage("chanserv", msg);
                return;
            }
        }
        channel.send().setTopic(topic);
    }

    private String tokenizedTopic(String topic) {
        return plugin.colorConverter
                .gameColorsToIrc(topic.replace("%MOTD%", plugin.getServer().getMotd()));
    }

    /**
     *
     * @param sender
     */
    public void asyncQuit(CommandSender sender) {
        sender.sendMessage("Disconnecting " + bot.getNick() + " from IRC server " + botServer);
        asyncQuit(false);
    }

    /**
     *
     * @param reload
     */
    public void asyncQuit(final Boolean reload) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                quit();
                if (reload) {
                    buildBot();
                }
            }
        });

    }

    public void quit() {
        if (this.isConnected()) {
            plugin.logDebug("Q: " + quitMessage);
            if (quitMessage.isEmpty()) {
                bot.sendIRC().quitServer();
            } else {
                bot.sendIRC().quitServer(plugin.colorConverter.gameColorsToIrc(quitMessage));
            }
        }
    }

    /**
     *
     * @param sender
     */
    public void sendTopic(CommandSender sender) {
        for (String channelName : botChannels) {
            if (commandMap.containsKey(channelName)) {
                sender.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_PURPLE
                        + botNick + ChatColor.WHITE + "]" + ChatColor.RESET
                        + " IRC topic for " + ChatColor.WHITE + channelName
                        + ChatColor.RESET + ": \""
                        + ChatColor.WHITE + plugin.colorConverter
                        .ircColorsToGame(activeTopic.get(channelName))
                        + ChatColor.RESET + "\"");
            }
        }
    }

    /**
     *
     * @param sender
     * @param nick
     */
    public void sendUserWhois(CommandSender sender, String nick) {
        User user = null;
        for (Channel channel : getChannels()) {
            for (User u : channel.getUsers()) {
                if (u.getNick().equals(nick)) {
                    user = u;
                }
            }
        }

        if (user == null) {
            sender.sendMessage(ChatColor.RED + "Invalid user: " + ChatColor.WHITE + nick);
        } else {
            bot.sendRaw().rawLineNow(String.format("WHOIS %s %s", nick, nick));
            whoisSenders.add(sender);
        }
    }

    /**
     *
     * @param sender
     * @param channelName
     */
    public void sendUserList(CommandSender sender, String channelName) {
        String invalidChannel = ChatColor.RED + "Invalid channel: "
                + ChatColor.WHITE + channelName;
        if (!isValidChannel(channelName)) {
            sender.sendMessage(invalidChannel);
            return;
        }
        Channel channel = getChannel(channelName);
        if (channel != null) {
            sendUserList(sender, channel);
        } else {
            sender.sendMessage(invalidChannel);
        }
    }

    /**
     *
     * @param sender
     * @param channel
     */
    public void sendUserList(CommandSender sender, Channel channel) {
        String channelName = channel.getName();
        if (!isValidChannel(channelName)) {
            sender.sendMessage(ChatColor.RED + "Invalid channel: "
                    + ChatColor.WHITE + channelName);
            return;
        }
        sender.sendMessage(ChatColor.DARK_PURPLE + "-----[  " + ChatColor.WHITE + channelName
                + ChatColor.DARK_PURPLE + " - " + ChatColor.WHITE + bot.getNick() + ChatColor.DARK_PURPLE + " ]-----");
        if (!this.isConnected()) {
            sender.sendMessage(ChatColor.RED + " Not connected!");
            return;
        }
        List<String> channelUsers = new ArrayList<>();
        for (User user : channel.getUsers()) {
            String nick = user.getNick();
            nick = getNickPrefix(user, channel) + nick;
            if (user.isAway()) {
                nick = nick + ChatColor.GRAY + " | Away";
            }
            if (nick.equals(bot.getNick())) {
                nick = ChatColor.DARK_PURPLE + nick;
            }
            channelUsers.add(nick);
        }
        Collections.sort(channelUsers, Collator.getInstance());
        for (String userName : channelUsers) {
            sender.sendMessage("  " + ChatColor.WHITE + userName);
        }
    }

    public String getNickPrefix(User user, Channel channel) {
        try {
            if (user.getChannels() != null) {
                if (user.isIrcop()) {
                    return plugin.ircNickPrefixIrcOp;
                } else if (user.getChannelsSuperOpIn().contains(channel)) {
                    return plugin.ircNickPrefixSuperOp;
                } else if (user.getChannelsOpIn().contains(channel)) {
                    return plugin.ircNickPrefixOp;
                } else if (user.getChannelsHalfOpIn().contains(channel)) {
                    return plugin.ircNickPrefixHalfOp;
                } else if (user.getChannelsVoiceIn().contains(channel)) {
                    return plugin.ircNickPrefixVoice;
                }
            }
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
        }
        return "";
    }

    /**
     *
     * @param sender
     */
    public void sendUserList(CommandSender sender) {
        for (Channel channel : getChannels()) {
            if (isValidChannel(channel.getName())) {
                sendUserList(sender, channel);
            }
        }
    }

    /**
     *
     */
    public void updateNickList() {
        if (!this.isConnected()) {
            return;
        }
        for (Channel channel : this.getChannels()) {
            this.updateNickList(channel);
        }
    }

    /**
     *
     * @param channel
     */
    public void updateNickList(Channel channel) {
        if (!this.isConnected()) {
            return;
        }
        // Build current list of names in channel
        ArrayList<String> users = new ArrayList<>();
        for (User user : channel.getUsers()) {
            //plugin.logDebug("N: " + user.getNick());
            users.add(user.getNick());
        }
        // Iterate over previous list and remove from tab list
        String channelName = channel.getName();
        if (channelNicks.containsKey(channelName)) {
            for (String name : channelNicks.get(channelName)) {
                //plugin.logDebug("O: " + name);
                if (!users.contains(name)) {
                    plugin.logDebug("Removing " + name + " from list.");
                    if (plugin.netPackets != null) {
                        plugin.netPackets.remFromTabList(name);
                    }
                }
            }
            channelNicks.remove(channelName);
        }
        channelNicks.put(channelName, users);
    }

    /**
     *
     * @param channel
     */
    public void opIrcUsers(Channel channel) {
        for (User user : channel.getUsers()) {
            opIrcUser(channel, user);
        }
    }

    /**
     *
     * @param channelName
     */
    public void opIrcUsers(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                opIrcUser(channel, user);
            }
        }
    }

    /**
     *
     * @param channel
     */
    public void voiceIrcUsers(Channel channel) {
        for (User user : channel.getUsers()) {
            voiceIrcUser(channel, user);
        }
    }

    /**
     *
     * @param channelName
     */
    public void voiceIrcUsers(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                voiceIrcUser(channel, user);
            }
        }
    }

    /**
     *
     * @param user
     * @param userMask
     * @return
     */
    public boolean checkUserMask(User user, String userMask) {
        String mask[] = userMask.split("[\\!\\@]", 3);
        if (mask.length == 3) {
            String gUser = plugin.regexGlobber.createRegexFromGlob(mask[0]);
            String gLogin = plugin.regexGlobber.createRegexFromGlob(mask[1]);
            String gHost = plugin.regexGlobber.createRegexFromGlob(mask[2]);
            return (user.getNick().matches(gUser)
                    && user.getLogin().matches(gLogin)
                    && user.getHostmask().matches(gHost));
        }
        return false;
    }

    /**
     *
     * @param channel
     * @param user
     */
    public void opIrcUser(Channel channel, User user) {
        String channelName = channel.getName();
        if (user.getNick().equals(botNick)) {
            return;
        }
        if (channel.getOps().contains(user)) {
            plugin.logInfo("User " + user.getNick() + " is already an operator on " + channelName);
            return;
        }
        for (String userMask : opsList.get(channelName)) {
            if (checkUserMask(user, userMask)) {
                plugin.logInfo("Giving operator status to " + user.getNick() + " on " + channelName);
                channel.send().op(user);
                break;
            }
        }
    }

    /**
     *
     * @param channel
     * @param user
     */
    public void voiceIrcUser(Channel channel, User user) {
        String channelName = channel.getName();
        if (user.getNick().equals(botNick)) {
            return;
        }
        if (channel.getVoices().contains(user)) {
            plugin.logInfo("User " + user.getNick() + " is already a voice on " + channelName);
            return;
        }
        for (String userMask : voicesList.get(channelName)) {
            if (checkUserMask(user, userMask)) {
                plugin.logInfo("Giving voice status to " + user.getNick() + " on " + channelName);
                channel.send().voice(user);
                break;
            }
        }
    }

    public String filterMessage(String message, String myChannel) {
        if (filters.containsKey(myChannel)) {
            if (!filters.get(myChannel).isEmpty()) {
                for (String filter : filters.get(myChannel)) {
                    if (filter.startsWith("/") && filter.endsWith("/")) {
                        filter = filter.substring(1, filter.length() - 1);
                        plugin.logDebug("Regex filtering " + filter + " from " + message);
                        message = message.replaceAll(filter, "");
                    } else {
                        plugin.logDebug("Filtering " + filter + " from " + message);
                        message = message.replace(filter, "");
                    }
                }
            }
        }
        return message;
    }

    // Broadcast chat messages from IRC
    /**
     *
     * @param user
     * @param channel
     * @param message
     * @param override
     */
    public void broadcastChat(User user, org.pircbotx.Channel channel, String message, boolean override) {
        String myChannel = channel.getName();
        if (plugin.dynmapHook != null) {
            plugin.logDebug("Checking if " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is enabled ...");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_DYNMAP_WEB_CHAT)) {
                plugin.logDebug("Yes, " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is enabled...");
                plugin.logDebug("broadcastChat [DW]: " + message);
                String template = plugin.getMsgTemplate(botNick, TemplateName.IRC_DYNMAP_WEB_CHAT);
                String rawDWMessage = filterMessage(
                        plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, template, message), myChannel);
                plugin.dynmapHook.sendMessage(user.getNick(), rawDWMessage);
            } else {
                plugin.logDebug("Nope, " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is NOT enabled...");
            }
        }
        if (plugin.tcHook != null) {
            plugin.logDebug("Checking if " + TemplateName.IRC_TOWNY_CHAT + " is enabled ...");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_TOWNY_CHAT)) {
                plugin.logDebug("Yes, " + TemplateName.IRC_TOWNY_CHAT + " is enabled...");
                if (townyChannel.containsKey(myChannel)) {
                    String tChannel = townyChannel.get(myChannel);
                    if (!tChannel.isEmpty()) {
                        String tmpl = plugin.getIRCTownyChatChannelTemplate(botNick, tChannel);
                        plugin.logDebug("broadcastChat [TC]: " + tChannel + ": " + tmpl);
                        String rawTCMessage = filterMessage(
                                plugin.tokenizer.ircChatToTownyChatTokenizer(this, user, channel, tmpl, message, tChannel), myChannel);
                        plugin.tcHook.sendMessage(tChannel, rawTCMessage);
                    }
                }
            } else {
                plugin.logDebug("Nope, " + TemplateName.IRC_TOWNY_CHAT + " is NOT enabled...");
            }
        }

        plugin.logDebug("Checking if " + TemplateName.IRC_CHAT
                + " is enabled before broadcasting chat from IRC");
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_CHAT) || override) {
            plugin.logDebug("Yup we can broadcast due to " + TemplateName.IRC_CHAT + " enabled");
            String newMessage = filterMessage(
                    plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, plugin.getMsgTemplate(
                                    botNick, TemplateName.IRC_CHAT), message), myChannel);
            if (!newMessage.isEmpty()) {
                plugin.getServer().broadcast(newMessage, "irc.message.chat");
            }
        } else {
            plugin.logDebug("NOPE we can't broadcast due to " + TemplateName.IRC_CHAT
                    + " disabled");
        }

        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_CONSOLE_CHAT)) {
            String tmpl = plugin.getMsgTemplate(botNick, TemplateName.IRC_CONSOLE_CHAT);
            plugin.logDebug("broadcastChat [Console]: " + tmpl);
            plugin.getServer().getConsoleSender().sendMessage(plugin.tokenizer.ircChatToGameTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_CONSOLE_CHAT), message));
        }

        plugin.logDebug("Checking if " + TemplateName.IRC_HERO_CHAT
                + " is enabled before broadcasting chat from IRC to HeroChat");
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_HERO_CHAT)) {
            String hChannel = heroChannel.get(myChannel);
            String tmpl = plugin.getIRCHeroChatChannelTemplate(botNick, hChannel);
            plugin.logDebug("broadcastChat [HC]: " + hChannel + ": " + tmpl);
            String rawHCMessage = filterMessage(
                    plugin.tokenizer.ircChatToHeroChatTokenizer(this, user, channel, tmpl, message, Herochat.getChannelManager(), hChannel), myChannel);
            if (!rawHCMessage.isEmpty()) {
                Herochat.getChannelManager().getChannel(hChannel).sendRawMessage(rawHCMessage);
                if (logIrcToHeroChat.containsKey(myChannel)) {
                    if (logIrcToHeroChat.get(myChannel)) {
                        plugin.getServer().getConsoleSender().sendMessage(rawHCMessage);
                    }
                }
            }
        } else {
            plugin.logDebug("NOPE we can't broadcast to HeroChat due to "
                    + TemplateName.IRC_HERO_CHAT + " disabled");
        }
        if (plugin.isPluginEnabled("Essentials")) {
            plugin.logDebug("Checking if " + TemplateName.IRC_ESS_HELPOP
                    + " is enabled before broadcasting chat from IRC");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_ESS_HELPOP) || override) {
                plugin.logDebug("Yup we can broadcast due to " + TemplateName.IRC_ESS_HELPOP + " enabled");
                String newMessage = filterMessage(
                        plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, plugin.getMsgTemplate(
                                        botNick, TemplateName.IRC_ESS_HELPOP), message), myChannel);
                if (!newMessage.isEmpty()) {
                    plugin.getServer().broadcast(newMessage, "essentials.helpop.receive");
                }
            } else {
                plugin.logDebug("NOPE we can't broadcast due to " + TemplateName.IRC_ESS_HELPOP
                        + " disabled");
            }
        }
    }

// Broadcast chat messages from IRC to specific hero channel
    /**
     *
     * @param user
     * @param channel
     * @param target
     * @param message
     */
    public void broadcastHeroChat(User user, org.pircbotx.Channel channel, String target, String message) {
        String ircChannel = channel.getName();
        if (message == null) {
            plugin.logDebug("H: NULL MESSAGE");
            asyncIRCMessage(target, "No channel specified!");
            return;
        }
        if (message.contains(" ")) {
            String hChannel;
            String msg;
            hChannel = message.split(" ", 2)[0];
            msg = message.split(" ", 2)[1];
            plugin.logDebug("Check if " + TemplateName.IRC_HERO_CHAT + " is enabled before broadcasting chat from IRC");
            if (enabledMessages.get(ircChannel).contains(TemplateName.IRC_HERO_CHAT)) {
                plugin.logDebug("Checking if " + hChannel + " is a valid hero channel...");
                if (Herochat.getChannelManager().hasChannel(hChannel)) {
                    hChannel = Herochat.getChannelManager().getChannel(hChannel).getName();
                    String template = plugin.getIRCHeroChatChannelTemplate(botNick, hChannel);
                    plugin.logDebug("T: " + template);
                    String t = plugin.tokenizer.ircChatToHeroChatTokenizer(this, user,
                            channel, template, msg,
                            Herochat.getChannelManager(), hChannel);
                    plugin.logDebug("Sending message to" + hChannel + ":" + t);
                    Herochat.getChannelManager().getChannel(hChannel)
                            .sendRawMessage(t);
                    plugin.logDebug("Channel format: " + Herochat.getChannelManager().getChannel(hChannel).getFormat());
                    // Let the sender know the message was sent
                    String responseTemplate = plugin.getMsgTemplate(botNick, TemplateName.IRC_HCHAT_RESPONSE);
                    if (!responseTemplate.isEmpty()) {
                        asyncIRCMessage(target, plugin.tokenizer
                                .targetChatResponseTokenizer(hChannel, msg, responseTemplate));
                    }
                } else {
                    asyncIRCMessage(target, "Hero channel \"" + hChannel + "\" does not exist!");
                }
            } else {
                plugin.logDebug("NOPE we can't broadcast due to "
                        + TemplateName.IRC_HERO_CHAT + " disabled");
            }
        } else {
            asyncIRCMessage(target, "No message specified.");
        }
    }

    // Send chat messages from IRC to player
    /**
     *
     * @param user
     * @param channel
     * @param target
     * @param message
     */
    public void playerChat(User user, org.pircbotx.Channel channel, String target, String message) {
        String myChannel = channel.getName();
        if (message == null) {
            plugin.logDebug("H: NULL MESSAGE");
            asyncIRCMessage(target, "No player specified!");
            return;
        }
        if (message.contains(" ")) {
            String pName;
            String msg;
            pName = message.split(" ", 2)[0];
            msg = message.split(" ", 2)[1];
            plugin.logDebug("Check if " + TemplateName.IRC_PCHAT + " is enabled before broadcasting chat from IRC");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_PCHAT)) {
                plugin.logDebug("Yup we can broadcast due to " + TemplateName.IRC_PCHAT
                        + " enabled... Checking if " + pName + " is a valid player...");
                Player player = plugin.getServer().getPlayer(pName);
                if (player != null) {
                    if (player.isOnline()) {
                        plugin.logDebug("Yup, " + pName + " is a valid player...");
                        String template = plugin.getMsgTemplate(botNick, TemplateName.IRC_PCHAT);
                        String t = plugin.tokenizer.ircChatToGameTokenizer(this, user,
                                channel, template, msg);
                        String responseTemplate = plugin.getMsgTemplate(botNick,
                                TemplateName.IRC_PCHAT_RESPONSE);
                        if (!responseTemplate.isEmpty()) {
                            asyncIRCMessage(target, plugin.tokenizer
                                    .targetChatResponseTokenizer(pName, msg, responseTemplate));
                        }
                        plugin.logDebug("Tokenized message: " + t);
                        player.sendMessage(t);
                    } else {
                        asyncIRCMessage(target, "Player is offline: " + pName);
                    }
                } else {
                    asyncIRCMessage(target, "Player not found (possibly offline): " + pName);
                }
            } else {
                plugin.logDebug("NOPE we can't broadcast due to irc-pchat disabled");
            }
        } else {
            asyncIRCMessage(target, "No message specified.");
        }
    }

// Broadcast action messages from IRC
    /**
     *
     * @param user
     * @param channel
     * @param message
     */
    public void broadcastAction(User user, org.pircbotx.Channel channel, String message) {
        String myChannel = channel.getName();
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_ACTION)) {
            plugin.getServer().broadcast(plugin.tokenizer.ircChatToGameTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_ACTION), message), "irc.message.action");
        } else {
            plugin.logDebug("Ignoring action due to "
                    + TemplateName.IRC_ACTION + " is false");
        }

        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_HERO_ACTION)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(myChannel))
                    .sendRawMessage(plugin.tokenizer.ircChatToHeroChatTokenizer(
                                    this, user, channel, plugin.getMsgTemplate(
                                            botNick, TemplateName.IRC_HERO_ACTION), message,
                                    Herochat.getChannelManager(),
                                    heroChannel.get(myChannel)
                            )
                    );
        }
    }

    /**
     *
     * @param recipient
     * @param kicker
     * @param reason
     * @param channel
     */
    public void broadcastIRCKick(User recipient, User kicker, String reason, org.pircbotx.Channel channel) {
        String myChannel = channel.getName();
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_KICK)) {
            plugin.getServer().broadcast(plugin.tokenizer.ircKickTokenizer(
                    this, recipient, kicker, reason, channel, plugin.getMsgTemplate(
                            botNick, TemplateName.IRC_KICK)),
                    "irc.message.kick");
        }

        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_HERO_KICK)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(myChannel))
                    .sendRawMessage(plugin.tokenizer
                            .ircKickToHeroChatTokenizer(this,
                                    recipient, kicker,
                                    reason, channel,
                                    plugin.getMsgTemplate(botNick, TemplateName.IRC_HERO_KICK),
                                    Herochat.getChannelManager(),
                                    heroChannel.get(myChannel)
                            )
                    );
        }
    }

    /**
     *
     * @return
     */
    public boolean isConnectedBlocking() {
        return bot.isConnected();
    }

    /**
     *
     * @param user
     * @param mode
     * @param channel
     */
    public void broadcastIRCMode(User user, String mode, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_MODE)) {
            plugin.getServer().broadcast(plugin.tokenizer.ircModeTokenizer(this, user, mode,
                    channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_MODE)), "irc.message.mode");
        }
    }

    /**
     *
     * @param user
     * @param message
     * @param notice
     * @param channel
     */
    public void broadcastIRCNotice(User user, String message, String notice, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_NOTICE)) {
            plugin.getServer().broadcast(plugin.tokenizer.ircNoticeTokenizer(this, user,
                    message, notice, channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_NOTICE)), "irc.message.notice");
        }
    }

    /**
     *
     * @param user
     * @param channel
     */
    public void broadcastIRCJoin(User user, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_JOIN)) {
            plugin.logDebug("[broadcastIRCJoin] Broadcasting join message because "
                    + TemplateName.IRC_JOIN + " is true.");
            plugin.getServer().broadcast(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_JOIN)), "irc.message.join");
        } else {
            plugin.logDebug("[broadcastIRCJoin] NOT broadcasting join message because irc-join is false.");
        }

        if (isMessageEnabled(channel, TemplateName.IRC_HERO_JOIN)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(channel.getName()))
                    .sendRawMessage(plugin.tokenizer.ircChatToHeroChatTokenizer(
                                    this, user, channel, plugin.getMsgTemplate(botNick,
                                            TemplateName.IRC_HERO_JOIN),
                                    Herochat.getChannelManager(),
                                    heroChannel.get(channel.getName())));
        }
    }

    public void broadcastIRCPart(User user, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_PART)) {
            String message = plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_PART));
            plugin.logDebug("[broadcastIRCPart]  Broadcasting part message because "
                    + TemplateName.IRC_PART + " is true: " + message);
            plugin.getServer().broadcast(message, "irc.message.part");
        } else {
            plugin.logDebug("[broadcastIRCPart] NOT broadcasting part message because "
                    + TemplateName.IRC_PART + " is false.");
        }

        if (isMessageEnabled(channel, TemplateName.IRC_HERO_PART)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(channel.getName()))
                    .sendRawMessage(plugin.tokenizer.ircChatToHeroChatTokenizer(
                                    this, user, channel, plugin.getMsgTemplate(
                                            botNick, TemplateName.IRC_HERO_PART),
                                    Herochat.getChannelManager(),
                                    heroChannel.get(channel.getName())));
        }
    }

    public void broadcastIRCQuit(User user, org.pircbotx.Channel channel, String reason) {
        if (isMessageEnabled(channel, TemplateName.IRC_QUIT)) {
            plugin.logDebug("[broadcastIRCQuit] Broadcasting quit message because "
                    + TemplateName.IRC_QUIT + " is true.");
            plugin.getServer().broadcast(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_QUIT))
                    .replace("%REASON%", reason), "irc.message.quit");
        } else {
            plugin.logDebug("[broadcastIRCQuit] NOT broadcasting quit message because "
                    + TemplateName.IRC_QUIT + " is false.");
        }

        if (isMessageEnabled(channel, TemplateName.IRC_HERO_QUIT)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(channel.getName()))
                    .sendRawMessage(plugin.tokenizer.ircChatToHeroChatTokenizer(
                                    this, user, channel, plugin.getMsgTemplate(
                                            botNick, TemplateName.IRC_HERO_QUIT),
                                    Herochat.getChannelManager(),
                                    heroChannel.get(channel.getName())));
        }

    }

    /**
     * Broadcast topic changes from IRC
     *
     * @param user
     * @param channel
     * @param message
     */
    public void broadcastIRCTopic(User user, org.pircbotx.Channel channel, String message) {
        if (isMessageEnabled(channel, TemplateName.IRC_TOPIC)) {
            plugin.getServer().broadcast(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_TOPIC)), "irc.message.topic");
        }

        if (isMessageEnabled(channel, TemplateName.IRC_HERO_TOPIC)) {
            Herochat.getChannelManager().getChannel(heroChannel.get(channel.getName()))
                    .sendRawMessage(plugin.tokenizer.ircChatToHeroChatTokenizer(
                                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_HERO_TOPIC), message,
                                    Herochat.getChannelManager(),
                                    heroChannel.get(channel.getName())));
        }
    }

    /**
     *
     * @param channelName
     * @param templateName
     * @return
     */
    public boolean isMessageEnabled(String channelName, String templateName) {
        return enabledMessages.get(channelName).contains(templateName);
    }

    /**
     *
     * @param channel
     * @param templateName
     * @return
     */
    public boolean isMessageEnabled(Channel channel, String templateName) {
        return isMessageEnabled(channel.getName(), templateName);
    }

    /**
     * Broadcast disconnect messages from IRC
     *
     * @param nick
     */
    public void broadcastIRCDisconnect(String nick) {
        plugin.getServer().broadcast("[" + nick + "] Disconnected from IRC server.", "irc.message.disconnect");
    }

    /**
     * Broadcast connect messages from IRC
     *
     * @param nick
     */
    public void broadcastIRCConnect(String nick) {
        plugin.getServer().broadcast("[" + nick + "] Connected to IRC server.", "irc.message.connect");
    }

    /**
     * Notify when players use commands
     *
     * @param player
     * @param cmd
     * @param params
     */
    public void commandNotify(Player player, String cmd, String params) {
        if (!this.isConnected()) {
            return;
        }
        String msg = plugin.tokenizer.gameCommandToIRCTokenizer(player,
                plugin.getMsgTemplate(botNick, TemplateName.GAME_COMMAND), cmd, params);
        if (channelCmdNotifyMode.equalsIgnoreCase("msg")) {
            for (String recipient : channelCmdNotifyRecipients) {
                asyncIRCMessage(recipient, msg);
            }
        } else if (channelCmdNotifyMode.equalsIgnoreCase("ctcp")) {
            for (String recipient : channelCmdNotifyRecipients) {
                asyncCTCPMessage(recipient, msg);
            }
        }
    }

    // Notify when player goes AFK
    /**
     *
     * @param player
     * @param afk
     */
    public void essentialsAFK(Player player, boolean afk) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_AFK)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                String template;
                if (afk) {
                    template = plugin.getMsgTemplate(botNick, TemplateName.ESS_PLAYER_AFK);
                } else {
                    template = plugin.getMsgTemplate(botNick, TemplateName.ESS_PLAYER_NOT_AFK);
                }
                plugin.logDebug("Sending AFK message to " + channelName);
                asyncIRCMessage(channelName, plugin.tokenizer.gamePlayerAFKTokenizer(player, template));
            }
        }
    }

    /**
     *
     * @param sender
     * @param nick
     * @param message
     */
    public void msgPlayer(Player sender, String nick, String message) {
        String msg = plugin.tokenizer.gameChatToIRCTokenizer(sender,
                plugin.getMsgTemplate(botNick, TemplateName.GAME_PCHAT), message);
        asyncIRCMessage(nick, msg);
    }

    /**
     *
     * @param nick
     * @param message
     */
    public void consoleMsgPlayer(String nick, String message) {
        String msg = plugin.tokenizer.gameChatToIRCTokenizer("console",
                plugin.getMsgTemplate(botNick, TemplateName.CONSOLE_CHAT), message);
        asyncIRCMessage(nick, msg);
    }

    /**
     *
     * @param player
     * @return
     */
    protected String getFactionName(Player player) {
        UPlayer uPlayer = UPlayer.get(player);
        Faction faction = uPlayer.getFaction();
        return faction.getName();
    }

    public boolean isConnected() {
        return connected;
    }

    public ImmutableSortedSet<Channel> getChannels() {
        if (bot.getNick().isEmpty()) {
            return ImmutableSortedSet.<Channel>naturalOrder().build();
        }
        return bot.getUserBot().getChannels();
    }

    public long getMessageDelay() {
        return bot.getConfiguration().getMessageDelay();
    }

    public String getMotd() {
        return bot.getServerInfo().getMotd();
    }

    public boolean isValidChannel(String channelName) {
        for (String s : botChannels) {
            if (channelName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        plugin.logDebug("Channel " + channelName + " is not valid.");
        return false;
    }

    public PircBotX getBot() {
        return bot;
    }

    /**
     *
     * @param connected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getFileName() {
        return fileName;
    }

    public String prismBlockStateChangeTokens(String message, ArrayList<BlockStateChange> blockStateChange) {
        String X;
        String Y;
        String Z;
        String origBlock;
        String newBlock;
        String blockWorld;
        try {
            blockWorld = String.valueOf(blockStateChange.get(0).getNewBlock().getWorld().getName());
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
            blockWorld = "";
        }
        try {
            origBlock = String.valueOf(blockStateChange.get(0).getOriginalBlock().getType().name());
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
            origBlock = "";
        }
        try {
            newBlock = String.valueOf(blockStateChange.get(0).getNewBlock().getType().name());
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
            newBlock = "";
        }
        try {
            X = String.valueOf(blockStateChange.get(0).getNewBlock().getX());
            Y = String.valueOf(blockStateChange.get(0).getNewBlock().getY());
            Z = String.valueOf(blockStateChange.get(0).getNewBlock().getZ());
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
            X = "";
            Y = "";
            Z = "";
        }
        if (X == null) {
            X = "";
        }
        if (Y == null) {
            Y = "";
        }
        if (Z == null) {
            Z = "";
        }
        if (blockWorld == null) {
            blockWorld = null;
        }
        if (origBlock == null) {
            origBlock = "";
        }
        if (newBlock == null) {
            newBlock = "";
        }
        return message
                .replace("%ORIGINALBLOCK%", origBlock)
                .replace("%NEWBLOCK%", newBlock)
                .replace("%X%", X)
                .replace("%Y%", Y)
                .replace("%Z%", Z)
                .replace("%BLOCKWORLD%", blockWorld);
    }

    public void gamePrismRollback(Player player, QueryParameters queryParams, ArrayList<BlockStateChange> blockStateChange) {
        if (!this.isConnected()) {
            return;
        }
        String keyword = queryParams.getKeyword();
        String sortDirection = queryParams.getSortDirection();
        String worldName = queryParams.getWorld();
        String id = String.valueOf(queryParams.getId());
        String radius = String.valueOf(queryParams.getRadius());
        if (keyword == null) {
            keyword = "";
        }
        if (sortDirection == null) {
            sortDirection = "";
        }
        if (worldName == null) {
            worldName = "";
        }
        if (id == null) {
            id = "";
        }
        if (radius == null) {
            radius = "";
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.PRISM_ROLLBACK)) {
                asyncIRCMessage(channelName, prismBlockStateChangeTokens(plugin.tokenizer
                        .playerTokenizer(player, plugin.getMsgTemplate(botNick, TemplateName.PRISM_ROLLBACK))
                        .replace("%COMMAND%", queryParams.getOriginalCommand())
                        .replace("%KEYWORD%", keyword)
                        .replace("%SORTDIRECTION%", sortDirection)
                        .replace("%PARAMWORLD%", worldName)
                        .replace("%ID%", id)
                        .replace("%RADIUS%", radius), blockStateChange
                ));
            }
        }
    }

    public void gamePrismDrainOrExtinguish(String template, Player player, int radius, ArrayList<BlockStateChange> blockStateChange) {
        if (!this.isConnected()) {
            return;
        }
        String radiusStr = String.valueOf(radius);

        if (radiusStr == null) {
            radiusStr = "";
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, template)) {
                asyncIRCMessage(channelName, prismBlockStateChangeTokens(plugin.tokenizer
                        .playerTokenizer(player, plugin.getMsgTemplate(botNick, template))
                        .replace("%RADIUS%", radiusStr), blockStateChange
                ));
            }
        }
    }

    public void gamePrismCustom(Player player, String actionName, String message, String pluginName) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.PRISM_CUSTOM)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .playerTokenizer(player, plugin.getMsgTemplate(botNick, TemplateName.PRISM_CUSTOM))
                        .replace("%ACTION%", actionName)
                        .replace("%MESSAGE%", message)
                        .replace("%PLUGIN%", pluginName)
                );
            }
        }
    }
}
