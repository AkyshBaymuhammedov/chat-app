package functionality;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xevent.DefaultMessageEventRequestListener;
import org.jivesoftware.smackx.xevent.MessageEventManager;
import org.jivesoftware.smackx.xevent.MessageEventNotificationListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import sample.ChatPanel;
import sample.MainStage;
import sun.misc.IOUtils;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMPPManager {

    private static final int packetReplyTimeout = 500000; // millis
    private String server;
    private int port;

    private XMPPTCPConnectionConfiguration.Builder config;
    private AbstractXMPPConnection connection;
    private ChatManager chatManager;
    private MyMessageListener messageListener;
    public ChatPanel chatPanel;
    private MainStage mainStage;
    private Chat chat;
    private Message msg;
    private VCard vcard;
    private MessageEventManager messageEventManager, messageEventManager1;
    private String nickname, gender;
    private DateFormat dateFormat;
    private List<User> list = new ArrayList<>();
    private Image image;

    public XMPPManager(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public void init() throws XMPPException, XmppStringprepException {

        System.out.println(String.format("Initializing connection to server %1$s port %2$d", server, port));
        SmackConfiguration.setDefaultReplyTimeout(packetReplyTimeout);
        config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setSocketFactory(SocketFactory.getDefault());
        byte[] bytes = new byte[]{(byte) 127, (byte) 0, 0, (byte) 1};
        try {
            config.setHostAddress(InetAddress.getByAddress(bytes));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            config.setXmppDomain("akysh.letschat.local");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        config.setHost(server);
        config.setPort(5222);
        connection = new XMPPTCPConnection(config.build());
        try {
            connection.connect();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Connected: " + connection.isConnected());
        chatManager = ChatManager.getInstanceFor(connection);
        connection.addAsyncStanzaListener(new RequestListener(), new StanzaTypeFilter(Presence.class));
        messageListener = new MyMessageListener();
        messageEventManager = MessageEventManager.getInstanceFor(connection);
        messageEventManager.addMessageEventNotificationListener(new MessageNotificationListener());
        messageEventManager1 = MessageEventManager.getInstanceFor(connection);
        messageEventManager1.addMessageEventRequestListener(new DefaultMessageEventRequestListener() {
            @Override
            public void deliveredNotificationRequested(Jid from, String packetID, MessageEventManager messageEventManager) throws SmackException.NotConnectedException, InterruptedException {
                super.deliveredNotificationRequested(from, packetID, messageEventManager);
            }

            @Override
            public void displayedNotificationRequested(Jid from, String packetID, MessageEventManager messageEventManager) {
                super.displayedNotificationRequested(from, packetID, messageEventManager);
            }

            @Override
            public void composingNotificationRequested(Jid from, String packetID, MessageEventManager messageEventManager) {
                super.composingNotificationRequested(from, packetID, messageEventManager);
            }

            @Override
            public void offlineNotificationRequested(Jid from, String packetID, MessageEventManager messageEventManager) {
                super.offlineNotificationRequested(from, packetID, messageEventManager);
            }

        });
        dateFormat = new SimpleDateFormat("HH:mm");
    }

    public void createAccount(String phone, String nickname, String password, String name, String email, String gender) throws XMPPException, IOException, SmackException, InterruptedException {
        this.nickname = nickname;
        this.gender = gender;
        if (!connection.isConnected()) {
            reConnect();
        }
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        Map<String, String> map = new HashMap<>();
        Random rand = new Random();
        DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
        map.put("username", phone);
        map.put("password", password);
        map.put("email", email);
        map.put("name", name);
        map.put("creationDate", dateFormat.format(new Date()));
        accountManager.createAccount(Localpart.from(phone), password, map);

    }

    public void performLogin(String phone, String password) throws InterruptedException, IOException, SmackException, XMPPException {
        if (!connection.isConnected()) {
            reConnect();
        }
        connection.login(phone, password);
        setStatus(true, "Hello World!");
        printRoster();
        chatManager.addIncomingListener(messageListener);
    }

    public void setVcards() {
        try {
            VCardManager vCardManager = VCardManager.getInstanceFor(connection);
            vcard = vCardManager.loadVCard();
            System.out.println(nickname);
            System.out.println(gender);
            vcard.setNickName(nickname);
            vcard.setField("gender", gender);
            vCardManager.saveVCard(vcard);
            if (gender.equals("Female")) {
                setAvatar(new File(getClass().getResource("pic-female.jpg").getFile()));
            } else {
                setAvatar(new File(getClass().getResource("pic-male.jpg").getFile()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAvatar(File file) {
        try {
            VCardManager vCardManager = VCardManager.getInstanceFor(connection);
            VCard vcard = vCardManager.loadVCard();
            InputStream stream = new FileInputStream(file);
            byte[] avatar1 = IOUtils.readFully(stream, -1, true);
            System.out.println("avatar length " + avatar1.length);
            vcard.setAvatar(avatar1, "avatar1/jpg");
            vCardManager.saveVCard(vcard);
            /*Thread thread = new Thread(() -> sendAvatarChangedNotification());
            thread.start();*/
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
    }

    public Image getAvatar() {
        try {
            vcard = VCardManager.getInstanceFor(connection).loadVCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Getting avatar");
        byte[] bytes = vcard.getAvatar();
        InputStream in = new ByteArrayInputStream(bytes);
        image = new Image(in);
        return image;
    }

    public void sendAvatarChangedNotification() {
        printRoster().stream().forEach(e -> {
            Stanza stanza = new RosterPacket();
            try {
                messageEventManager1.sendCancelledNotification(getJid(e.getUser()), stanza.getStanzaId());
            } catch (SmackException.NotConnectedException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
    }

    public VCard getVcardOfUser(String user) {
        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        try {
            if (vCardManager.isSupported(JidCreate.bareFrom(user))) {
                vcard = vCardManager.loadVCard(JidCreate.entityBareFrom(user));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vcard;
    }


    public void setStatus(boolean available, String status) {

        Presence.Type type = available ? Presence.Type.available : Presence.Type.unavailable;
        Presence presence = new Presence(type);
        presence.setStatus(status);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rosterOnlineStatus();

    }

    public void rosterOnlineStatus() {
        Roster roster = Roster.getInstanceFor(connection);
        Presence status = new Presence(Presence.Type.available);
        status.setStatus("Hello!");
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {
                Platform.runLater(() -> {
                    printRoster();
                    mainStage.addToList();
                });
            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {
                Platform.runLater(() -> {
                    mainStage.addToList();
                });
            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {
                Platform.runLater(() -> {
                    printRoster();
                    mainStage.addToList();
                });
            }

            @Override
            public void presenceChanged(Presence presence) {
                Platform.runLater(() -> chatPanel.presenceChanged(presence.getFrom().asBareJid().toString(), presence.getType()));
            }
        });
    }


    public void destroy() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    public void addFriend(String userID) {
        String id = userID + "@akysh.letschat.local";
        if (connection != null && connection.isConnected()) {
            try {
                Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
                Roster roster = Roster.getInstanceFor(connection);
                if (!roster.contains(getBareJid(id))) {
                    Presence subscribe = new Presence(Presence.Type.subscribe);
                    subscribe.setTo(getJid(id));
                    connection.sendStanza(subscribe);
                } else {
                    Platform.runLater(() -> FXDialog.showError("Error!", "error adding!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message, String buddyJID) throws XMPPException, XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        chat = chatManager.chatWith(JidCreate.entityBareFrom(buddyJID));
        msg = new Message();
        msg.setBody(message);
        MessageEventManager.addNotificationsRequests(msg, true, true, true, true);
        chat.send(msg);
    }


    public void createEntry(String user, String name) {
        try {
            Roster roster = Roster.getInstanceFor(connection);
            if (!roster.getEntries().contains(user)) {
                System.out.println(String.format("Creating entry for buddy '%1$s' with name %2$s", user, name));
                roster.createEntry(getBareJid(user), name, null);
            }
        } catch (Exception e) {
            Platform.runLater(() -> FXDialog.showError("Entry error", "Creating entry error!"));
            e.printStackTrace();
        }
    }

    public List<User> printRoster() {
        Roster roster = Roster.getInstanceFor(connection);
        if (roster.getEntries()!=null) {
            list.clear();
            roster.getEntries().stream().forEach(e -> {
                System.out.println(e.getName());
                User user = new User(e.getUser(), e.getName());
                vcard = getVcardOfUser(e.getJid().toString());
                user.setvCard(vcard);
                byte[] bytes = vcard.getAvatar();
                InputStream in = new ByteArrayInputStream(bytes);
                user.setAvatar(new Image(in));
                list.add(user);
            });
            return list;
        }
        return null;
    }


    public User getCurrentUser() {
        try {
            AccountManager accountManager = AccountManager.getInstance(connection);
            String username = accountManager.getAccountAttribute("username");
            String name = accountManager.getAccountAttribute("name");
            vcard = VCardManager.getInstanceFor(connection).loadVCard();
            User user = new User(username, name);
            user.setvCard(vcard);
            byte[] bytes = vcard.getAvatar();
            InputStream in = new ByteArrayInputStream(bytes);
            user.setAvatar(new Image(in));
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Presence.Type getPresence(String user) {
        Roster roster = Roster.getInstanceFor(connection);
        Presence entryPresence = roster.getPresence(getBareJid(user));
        return entryPresence.getType();
    }

    public String getLastActivity(String user) {
        long time = 0;
        long minutes = 0;
        try {
            if (connection.isConnected()) {
                LastActivity lastActivity = LastActivityManager.getInstanceFor(connection).getLastActivity(getJid(user));
                time = lastActivity.getIdleTime();
                minutes = time / 60;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
        if (minutes >= 60) {
            long remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return minutes / 60 + " hours ago";
            }
            return minutes / 60 + " hours " + remainingMinutes + " minutes ago";
        } else if (minutes == 0) {
            if (time == 0) {
                return "just now";
            }
            return time + " seconds ago";
        }
        return minutes + " minutes ago";
    }

    public void setChatPanel(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

    public void setMainStage(MainStage mainStage) {
        this.mainStage = mainStage;
    }

    public void acceptRequest(String fromId) {
        Presence presence = new Presence(Presence.Type.subscribed);
        try {
            presence.setTo(JidCreate.from(fromId));
            connection.sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> mainStage.getFriendsPanel().removeRequest(fromId));
        createEntry(fromId, fromId.replace("@akysh.letschat.local/Smack", ""));
    }

    public void denyRequest(String fromId) {
        Presence presence = new Presence(Presence.Type.unsubscribe);
        try {
            presence.setTo(JidCreate.from(fromId));
            connection.sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> mainStage.getFriendsPanel().removeRequest(fromId));
        removeEntry(fromId);
    }

    public void removeEntry(String user) {
        Roster roster = Roster.getInstanceFor(connection);
        try {
            roster.removeEntry(roster.getEntry(getBareJid(user)));
            System.out.println("Removing: " + user);
            chatPanel.removeFriend(user);
        } catch (Exception e) {
            Platform.runLater(() -> FXDialog.showError("Error removing", "Unable to remove user : " + user));
            e.printStackTrace();
        }
    }

    public List<User> searchUser(String name) throws XMPPException, IOException, SmackException, InterruptedException {

        List<User> users = new ArrayList<>();
        UserSearchManager userSearchManager = new UserSearchManager(connection);
        DomainBareJid searchService = JidCreate.domainBareFrom("search." + connection.getServiceName());
        Form searchForm = userSearchManager.getSearchForm(searchService);
        Form answerForm = searchForm.createAnswerForm();
        answerForm.setAnswer("Name", true);
        answerForm.setAnswer("search", name);
        ReportedData data = userSearchManager.getSearchResults(answerForm, searchService);
        List<ReportedData.Row> it = data.getRows();
        for (ReportedData.Row row : it) {
            String name1 = row.getValues("Name").toString().replace("[", "").replace("]", "");
            String username = row.getValues("Username").toString().replace("[", "").replace("]", "");
            users.add(new User(username, name1));
        }
        return users;
    }

    public List getAllUsers() {
        List<String> users = new ArrayList<>();
        try {
            UserSearchManager userSearchManager = new UserSearchManager(connection);
            DomainBareJid searchService = JidCreate.domainBareFrom("search." + connection.getServiceName());
            Form searchForm = userSearchManager.getSearchForm(searchService);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", '*');
            ReportedData data = userSearchManager.getSearchResults(answerForm, searchService);
            List<ReportedData.Row> it = data.getRows();
            for (ReportedData.Row row : it) {
                users.add(row.getValues("Username").toString().replace("[", "").replace("]", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public void searcherLogin() {
        if (!connection.isConnected()) {
            reConnect();
        }
        try {
            connection.login("searcher", "searcher");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reConnect() {
        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean searchIfExits(String email, int id, String searchType) throws XMPPException, IOException, SmackException, InterruptedException {

        List<String> users = new ArrayList<>();
        UserSearchManager userSearchManager = new UserSearchManager(connection);
        DomainBareJid searchService = JidCreate.domainBareFrom("search." + connection.getServiceName());
        Form searchForm = userSearchManager.getSearchForm(searchService);
        Form answerForm = searchForm.createAnswerForm();
        if (searchType.equals("email")) {
            answerForm.setAnswer("Email", true);
        } else if (searchType.equals("user")) {
            answerForm.setAnswer("Username", true);
        }
        answerForm.setAnswer("search", email);
        ReportedData data = userSearchManager.getSearchResults(answerForm, searchService);
        List<ReportedData.Row> list = data.getRows();
        if (list.iterator().hasNext()) {
            return true;
        }
        return false;
    }

    public void sendNotification(String notification) {
        if (msg.getTo() != null) {
            if (notification.equals("displayed")) {
                try {
                    messageEventManager1.sendDisplayedNotification(msg.getTo(), msg.getStanzaId());
                    Platform.runLater(() -> {
                        mainStage.setIToZero();
                        chatPanel.setLabelToNormal(msg.getTo().asBareJid().toString());
                    });
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (notification.equals("composing")) {
                try {
                    messageEventManager1.sendComposingNotification(msg.getTo(), msg.getStanzaId());
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (notification.equals("cancelled")) {
                try {
                    messageEventManager1.sendCancelledNotification(msg.getTo(), msg.getStanzaId());
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public BareJid getBareJid(String user) {
        try {
            return JidCreate.bareFrom(user);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Jid getJid(String user) {
        try {
            return JidCreate.from(user);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<User> getFriends() {
        return list;
    }

    class MyMessageListener implements IncomingChatMessageListener {
        @Override
        public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
            String fromString = entityBareJid.toString();
            String body = message.getBody();
            System.out.println(body);
            if (body != null) {
                if (chatPanel != null) {
                    Platform.runLater(() -> chatPanel.showMessage(body, fromString));
                }
                Platform.runLater(() -> {
                    mainStage.notification();
                });
            }

        }
    }

    class RequestListener implements StanzaListener {
        @Override
        public void processStanza(Stanza stanza) throws SmackException.NotConnectedException, InterruptedException {
            Presence newPresence = (Presence) stanza;
            Presence.Type presenceType = newPresence.getType();
            String from = newPresence.getFrom().toString();
            BareJid fromId = getBareJid(newPresence.getFrom().toString());
            RosterEntry newEntry = Roster.getInstanceFor(connection).getEntry(fromId);
            if (presenceType == Presence.Type.subscribe) {
                if (newEntry == null) {
                    Platform.runLater(() -> mainStage.getFriendsPanel().friendRequests(fromId.toString()));
                    createEntry(from, from.replace("@akysh.letschat.local/Smack", ""));
                } else {
                    Presence presence = new Presence(Presence.Type.subscribed);
                    presence.setTo(fromId);
                    connection.sendStanza(presence);
                }
            } else if (presenceType == Presence.Type.unsubscribe) {
                removeEntry(fromId.toString());
            }
        }
    }

    class MessageNotificationListener implements MessageEventNotificationListener {
        @Override
        public void deliveredNotification(Jid from, String packetID) {
            if (chatPanel != null) {
                Platform.runLater(() -> {
                    chatPanel.setMessageStatus(from.asBareJid().toString(), "Read");
                });
            }
        }

        @Override
        public void displayedNotification(Jid from, String packetID) {
            if (chatPanel != null) {
                Platform.runLater(() -> chatPanel.setMessageStatus(from.asBareJid().toString(), "Read"));
            }

        }

        @Override
        public void composingNotification(Jid from, String packetID) {
            if (chatPanel != null) {
                Platform.runLater(() -> chatPanel.setTyping(from.asBareJid().toString()));
            }
        }

        @Override
        public void offlineNotification(Jid from, String packetID) {

        }

        @Override
        public void cancelledNotification(Jid from, String packetID) {
            /*System.out.println("Updating avatar!");
            Thread thread = new Thread(() -> {
                Platform.runLater(() -> chatPanel.setAvatarUpdate(from.asBareJid().toString()));
            });
            thread.start();*/
            Platform.runLater(() -> chatPanel.removeTyping());
        }
    }
}


