/*
 Copyright 2006, 2007, 2008 Markus Goetz, Sebastian Breier
 Webpage on http://p300.eu/
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 This file is part of p300.


 p300 is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 p300 is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with p300.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.guruz.p300;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import de.guruz.guruzsplash.interfaces.GuruzsplashManager;
import de.guruz.guruztray.interfaces.GuruztrayManager;
import de.guruz.p300.downloader.DownloadManager;
import de.guruz.p300.hosts.Host;
import de.guruz.p300.hosts.HostMap;
import de.guruz.p300.hosts.HostWatchThread;
import de.guruz.p300.hosts.allowing.HostAllowanceManager;
import de.guruz.p300.hosts.httpmulticast.HostFinderThread;
import de.guruz.p300.internet.InternetListenThread;
import de.guruz.p300.logging.D;
import de.guruz.p300.onetoonechat.LanMessageRemoteOutbox;
import de.guruz.p300.onetoonechat.LanMessageRouter;
import de.guruz.p300.onetoonechat.ui.ChatWindowMap;
import de.guruz.p300.onetoonechat.ui.UiMessageRouter;
import de.guruz.p300.osx.OSXCallbackInterface;
import de.guruz.p300.osx.OSXInterface;
import de.guruz.p300.search.IndexerThread;
import de.guruz.p300.search.ui.SearchResultsWindow;
import de.guruz.p300.threads.BandwidthThread;
import de.guruz.p300.threads.ListenThread;
import de.guruz.p300.threads.NewVersionNotificationThread;
import de.guruz.p300.threads.ShutdownThread;
import de.guruz.p300.threads.UdpListenThread;
import de.guruz.p300.threads.UpdaterThread;
import de.guruz.p300.utils.DirectoryUtils;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.utils.LockFile;
import de.guruz.p300.utils.OsUtils;
import de.guruz.p300.utils.launchers.BareBonesBrowserLaunch;
import de.guruz.p300.windowui.MainButtonPopupMenu;
import de.guruz.p300.windowui.MainDialogTreeCellEditor;
import de.guruz.p300.windowui.MainDialogTreeCellRenderer;
import de.guruz.p300.windowui.actions.AddDowloadAction;
import de.guruz.p300.windowui.actions.AddHostAction;
import de.guruz.p300.windowui.actions.AddShareAction;
import de.guruz.p300.windowui.actions.BrowseHostAction;
import de.guruz.p300.windowui.actions.ChatWithOtherUsersAction;
import de.guruz.p300.windowui.actions.ConfigurationAction;
import de.guruz.p300.windowui.actions.ConsoleAction;
import de.guruz.p300.windowui.actions.DonationsAction;
import de.guruz.p300.windowui.actions.EarnAction;
import de.guruz.p300.windowui.actions.GetHelpAction;
import de.guruz.p300.windowui.actions.QuitAction;
import de.guruz.p300.windowui.actions.SendFeatureRequestAction;
import de.guruz.p300.windowui.actions.SendFeedbackAction;
import de.guruz.p300.windowui.actions.ShowDownloadsAction;
import de.guruz.p300.windowui.actions.UploadsAction;
import de.guruz.p300.windowui.actions.WebinterfaceAction;
import de.guruz.p300.windowui.maintree.InternetHostsTreeItem;
import de.guruz.p300.windowui.maintree.LANHostTreeItem;
import de.guruz.p300.windowui.maintree.LANHostsTreeItem;
import de.guruz.p300.windowui.maintree.MainDialogTreeModel;
import de.guruz.p300.windowui.maintree.MainTree;
import de.guruz.p300.windowui.panels.ConfigurationPanel;
import de.guruz.p300.windowui.panels.ConsolePanel;
import de.guruz.p300.windowui.panels.DownloadsPanel;
import de.guruz.p300.windowui.panels.InfoPanel;
import de.guruz.p300.windowui.panels.NotImplementedYetPanel;
import de.guruz.p300.windowui.panels.UploadsPanel;

/**
 * This is the "real" main class which creates everything :)
 * 
 * @author guruz
 * 
 */
public class MainDialog extends Object implements ActionListener,
		OSXCallbackInterface {

	private static final String ACTION_SEARCH = "search";

	public static final String BROWSE_LAN_HOST_HERE = "browseLanHostHere";

	public static final String ACTION_GLOBAL_OPEN_WEBINTERFACE = "OpenWebinterface";

	public static final String ACTION_ADD_SHARE = "addshare";

	public static final String ACTION_RESET_ADMIN_PASSWORD = "resetadminpassword";

	public static final String ACTION_SHOW_HIDE_ADMINPASSWORD = "getadminpassword";

	public static final String ACTION_MODIFY_SHARE = "modifyshare";

	public static final String ACTION_DELETE_SHARE = "deleteshare";

	public static final String ACTION_ABOUT = "About";

	public static final String ACTION_QUIT = "Quit";

	public static final String ACTION_OPEN_MY_WEBINTERFACE = "OpenMyWebinterface";

	public static final String ACTION_HIDE = "Hide";

	public static final String ACTION_SHOW = "Show";

	public static final String ACTION_OPEN_CHAT_FOR_SELECTED_HOST = "OpenChatForSelectedHost";

	public static final String ACTION_BROWSE_SELECTED_HOST = "BrowseSelectedHost";

	public static final String ACTION_OPEN_WEBINTERFACE_FOR_SELECTED_HOST = "OpenWebinterfaceForSelectedHost";

	public static BandwidthThread bandwidthThread;

	public static boolean graphicConsoleEnabled;

	public static HostFinderThread hostFinderThread;

	public static HostMap hostMap;

	public static HostWatchThread hostWatchThread;

	public static MainDialog instance;

	public static IndexerThread indexerThread;

	static LaunchTypeType launchType = LaunchTypeType.Undetermined;

	public static LockFile lockFile = null;

	public static ListenThread listenThread;

	public static UdpListenThread multicastListenThread;

	public static InternetListenThread internetListenThread;

	public static UpdaterThread updaterThread;

	/**
	 * will be overwritten
	 */
	private static String myURL = "http://127.0.0.1:4337";

	public static NewVersionNotificationThread newVersionNotificationThread;

	public static boolean requestedShutdown;

	/**
	 * Unnecessary but fixes compiler warning
	 * 
	 * @author tomcat
	 */
	private static final long serialVersionUID = 0;

	/**
	 * Start time in millis since epoch
	 * 
	 * @author guruz
	 */
	public static long startedOn = System.currentTimeMillis();;

	/**
	 * 
	 * 
	 * @author guruz
	 */
	static Timer timer;

	public static DownloadManager downloadManager;

	public static HostAllowanceManager m_hostAllowanceManager;

	private static GuruzsplashManager guruzsplashManager = null;

	public static String launchedByRevision = "";

	/**
	 * 
	 * 
	 * @author guruz
	 */
	private static void createLockFile() {
		File lockDir = new File(Configuration.configDirFileName("locks"));
		DirectoryUtils.makeSureDirectoryExists(lockDir);

		String fn = Configuration.configDirFileName("locks/p300."
				+ MainDialog.getCurrentHTTPPort());
		MainDialog.lockFile = new LockFile(fn);
		MainDialog.lockFile.lock();
	}

	/**
	 * Create all important threads/objects
	 * 
	 * @author guruz
	 */
	private static void createThreadsAndObjects() {
		P300ProxySelector.useIt();

		m_hostAllowanceManager = new HostAllowanceManager();

		// Where we store the discovered multicast hosts
		MainDialog.hostMap = new HostMap();

		// The thread responsible for bandwidth quota
		MainDialog.bandwidthThread = new BandwidthThread();

		// The thread responsible for incoming UDP multicast packets
		MainDialog.multicastListenThread = new UdpListenThread();

		// the thread responsible for multicast over http
		MainDialog.hostFinderThread = new HostFinderThread();

		// how watch thread
		MainDialog.hostWatchThread = new HostWatchThread();
		MainDialog.hostWatchThread.setHostMap(MainDialog.hostMap);

		MainDialog.indexerThread = new IndexerThread();

		MainDialog.updaterThread = new UpdaterThread();
		MainDialog.newVersionNotificationThread = new NewVersionNotificationThread();

		MainDialog.internetListenThread = new InternetListenThread();

		MainDialog.downloadManager = new DownloadManager();

	}

	/**
	 * Return the current HTTP port from listener thread or config
	 * 
	 * @return Integer, usually 4337
	 * @author guruz
	 */
	public static int getCurrentHTTPPort() {
		if (MainDialog.listenThread == null) {
			return Configuration.instance().getDefaultHTTPPort();
		}

		return MainDialog.listenThread.getPort();
	}

	/**
	 * Return the currently running p300 instance
	 * 
	 * @return The current MainDialog instance
	 * @author guruz
	 */
	public static MainDialog getInstance() {
		return MainDialog.instance;
	}

	/**
	 * Return the URL of the current/local p300 instance
	 * 
	 * @return A String of the form "http://192.168.42.10:4337/"
	 * @author guruz
	 */
	public static String getMyURL() {
		return MainDialog.myURL;
	}

	/**
	 * 
	 * 
	 * @return
	 * @author guruz
	 */
	public static Window getWindow() {
		return MainDialog.getInstance().realWindow;
	}

	/**
	 * Read paramteres and do stuff
	 * 
	 * @param args
	 *            Arguments from command line
	 * @author guruz
	 * @see #help()
	 */
	public static void handleParameters(String[] args) {
		if (args[0].equals("--altID")) {
			Configuration.useAltUniqueInstanceHash = true;
			System.err.println("Using alternative Instance Hash/ID");
		} else if (args[0].equals("--headless") || args[0].equals("--console")) {
			MainDialog.launchType = LaunchTypeType.Console;
		} else if (args[0].equals("--head") || args[0].equals("--graphical")) {
			MainDialog.launchType = LaunchTypeType.GUI;
		} else if (args[0].equals("--setadminpass")
				|| args[0].equals("--setadminpw")
				|| args[0].equals("--setadminpassword")) {
			// Generate a new password
			MainDialog.resetAdminPassword();

			System.out.println(Configuration.instance().getAdminPassword());
			System.exit(1);
		} else if (args[0].equals("--getadminpass")
				|| args[0].equals("--getadminpw")
				|| args[0].equals("--getadminpassword")) {
			// Output the password
			System.out.println(Configuration.instance().getAdminPassword());
			System.exit(1);
		} else if (args[0].equals("--help") || args[0].equals("-h")) {
			MainDialog.help();
		} else if (args[0].equals("--allow")) {
			if (args.length == 2) {
				System.out.print("Allowing ");
				System.out.println(args[1]);
				Configuration.instance().setIpExplicitlyAllowed(args[1], true);
				System.exit(0);
			} else {
				MainDialog.help();
			}
		} else {
			System.err.println("Unknown option, try --help");
			System.exit(1);
		}
		;
	}

	/**
	 * Prints out the possible parameters to stdout
	 * 
	 * @author guruz
	 * @see #handleParameters(String[])
	 */
	public static void help() {
		System.err.println("Usage (use one of the parameters):");
		System.err.println(" --console             console only");
		System.err.println(" --graphical           with graphical environment");
		System.err
				.println(" --setadminpassword    sets a random admin password");
		System.err.println(" --getadminpassword    gets the admin password");
		System.err
				.println(" --allow HOST          enables HOST for the webinterface");
		System.err.println("");
		System.err.println("Current configuration:");

		System.err.println(" Listening on");
		System.err.print("  http://127.0.0.1:");
		System.err.println(MainDialog.getCurrentHTTPPort());

		System.err.println("  The username for the configuration is: admin");
		System.err.println("  You can set the remaining settings there");
		System.exit(0);
	}

	/**
	 * Hide the splash screen after one second
	 * 
	 * @author guruz
	 * @see #guruzsplashManager
	 * @see #initSplashScreen()
	 */
	private static void hideSplashScreen() {
		if (MainDialog.guruzsplashManager != null) {
			try {
				Thread.sleep(1000);
				MainDialog.guruzsplashManager.hide();
			} catch (Throwable t) {
				// e.printStackTrace();
				D.out("Splashscreen: " + t.toString());
			}

		}
	}

	/**
	 * Initialize and show the splash screen
	 * 
	 * @return True: Splash is visible; False: Splash not visible (might be no
	 *         GUI)
	 * @author guruz
	 * @see #guruzsplashManager
	 * @see #hideSplashScreen()
	 */
	private static boolean initSplashScreen() {
		try {
			String className = "de.guruz.guruzsplash.implementation.GuruzsplashManagerImplementation";
			Class<?> c = Class.forName(className);
			Object o = c.newInstance();
			MainDialog.guruzsplashManager = (GuruzsplashManager) o;

			if (MainDialog.guruzsplashManager.isVisible()) {
				return true;
			} else {
				MainDialog.guruzsplashManager = null;
				return false;
			}

		} catch (Throwable t) {
			D
					.out("No splash screen supported on this platform or java version");
			MainDialog.guruzsplashManager = null;
			return false;
		}
	}

	/**
	 * Check if this is running in console
	 * 
	 * @return True: Running headlessly; False: Running with GUI
	 * @author guruz
	 */
	public static boolean isHeadless() {
		return (MainDialog.launchType == LaunchTypeType.Console);
	}

	/**
	 * Start method
	 * 
	 * @param args
	 *            Command line arguments
	 * @author guruz
	 */
	public static void main(String[] args) {
		Thread
				.setDefaultUncaughtExceptionHandler(new P300UncaughtExceptionHandler());

		if (OsUtils.isOSX()) {
			System.setProperty("apple.awt.fileDialogForDirectories", "true");
			System.setProperty(
					"com.apple.mrj.application.apple.menu.about.name", "p300");
		}

		System.setProperty("java.net.preferIPv4Stack", "true");

		System.setProperty("java.net.useSystemProxies", "true");

		// init our default settings
		if (java.awt.GraphicsEnvironment.isHeadless()) {
			MainDialog.launchType = LaunchTypeType.Console;
		} else {
			MainDialog.launchType = LaunchTypeType.GUI;
		}

		// handle paraeters
		if (args.length >= 1) {
			MainDialog.handleParameters(args);
		}

		// the user may have requested a GUI mode although we do not support it
		if (java.awt.GraphicsEnvironment.isHeadless()) {
			MainDialog.launchType = LaunchTypeType.Console;
		}

		// see if we have java 1.6 and a splash screen
		MainDialog.initSplashScreen();

		try {
			launchedByRevision = System.getProperty("launchedByP300Revision");
		} catch (Exception e) {
		}
		;

		if (launchedByRevision != null && launchedByRevision.length() > 0)
			D.out("This is p300, revision " + Configuration.getSVNRevision()
					+ " launched by " + launchedByRevision);
		else
			D.out("This is p300, revision " + Configuration.getSVNRevision());

		Configuration.createDotP300();

		// The thread responsible for incoming (HTTP) connections
		try {
			MainDialog.listenThread = new ListenThread();
		} catch (Exception e) {
			String msg = "Could not bind to any TCP port. Please properly configure your firewall. Exiting now.";
			if (MainDialog.isHeadless()) {
				D.out(msg);
			} else {
				JOptionPane.showMessageDialog(null, msg, "alert",
						JOptionPane.ERROR_MESSAGE);
			}

			System.exit(1);
		}
		MainDialog.myURL = "http://127.0.0.1:"
				+ MainDialog.getCurrentHTTPPort() + '/';

		// well :)
		MainDialog.createLockFile();

		if (!MainDialog.isHeadless()) {
			// show the main dialog
			try {
				MainDialog.instance = new MainDialog();
				MainDialog.graphicConsoleEnabled = true;
			} catch (Throwable t) {
				D
						.out("Failure while initializing main window! Try --console as option to run headless");
				t.printStackTrace();
				MainDialog.instance = null;
				MainDialog.graphicConsoleEnabled = false;
				System.exit(1);
			}
		}

		if (MainDialog.instance != null) {
			D
					.out("Graphic mode. CTRL+C or close window to exit. Restart with parameter --help for help");
		} else {
			D
					.out("Headless mode. CTRL+C to exit. Restart with parameter --help for help");
		}

		try {
			MainDialog.createThreadsAndObjects();
			MainDialog.startThreadsAndObjects();
		} catch (Exception e) {
			// FIXME
			e.printStackTrace();
			D.out("Exiting because of exception");
			return;
		}
		// de the splash, no matter if it exists or is visible
		Runnable r = new Runnable() {
			public void run() {
				MainDialog.hideSplashScreen();
			}
		};
		new Thread(r).start();

		MainDialog.handleFirstStart();

		if (!MainDialog.isHeadless() && MainDialog.instance != null) {
			MainDialog.instance.setGUIEnabled(true);

			if ((MainDialog.instance.guruztrayManager != null)
					&& !Configuration.instance().isFirstStart()) {
				// if we have a tray icon and this is NOT the first start then
				// hide oursevles
				// this is for using p300 in autorun
				MainDialog.instance.hideP300();
			} else {
				// show us :)
				MainDialog.instance.showP300();
			}

			MainDialog.instance.loadOSXInterface();

		}

		// have this get called when the VM exits
		Runtime.getRuntime().addShutdownHook(new ShutdownThread());
		// updaterThread.go(401);

	}

	/**
	 * Reset the administrator password (for web GUI) to a random String of 8
	 * characters
	 * 
	 * @author guruz
	 * @see de.guruz.p300.Configuration.setAdminPassword(String)
	 * @see de.guruz.p300.utils.RandomGenerator.string()
	 */
	public static void resetAdminPassword() {
		String newPass = de.guruz.p300.utils.RandomGenerator.string()
				.substring(0, 8);
		Configuration.instance().setAdminPassword(newPass);
	}

	/**
	 * Do stuff on first ever start of p300 - Init host ranges - Show welcome
	 * message
	 * 
	 * @author guruz
	 * @see de.guruz.p300.Configuration.isFirstStart()
	 */
	public static void handleFirstStart() {
		Configuration conf = Configuration.instance();
		boolean firstStart = conf.isFirstStart();

		if (!firstStart) {
			return;
		}

		// allow some host ranges here :)
		// http://en.wikipedia.org/wiki/Private_network
		conf.setIpExplicitlyAllowed("192.168.", true);
		conf.setIpExplicitlyAllowed("10.", true);
		conf.setIpExplicitlyAllowed("169.254.", true); // zeroconf stuff
		conf.setIpExplicitlyAllowed("5.", true); // hamachi
		// enable the automatic detection of local network hosts
		conf.setLocalNetworkIpsImplicitlyAllowed(true);
		

		boolean headless = MainDialog.isHeadless();

		String msg = "This is your first start of p300. Welcome!\n\n"
				+ "You can now add a share. Other hosts should be found automatically, but at the \n"
				+ "first start this can take some time. You can also manually add a host.\n";

		if (Configuration.isJava15()) {
			msg = msg
					+ "\n\nNote that you should update to java 1.6 for tray icon support.";
		}
		

		if (MainDialog.instance.guruztrayManager != null
				&& MainDialog.instance.guruztrayManager.isSupported()) {
			msg = msg
					+ "\n\nIf you close the main window, it will be minimized in the system tray. On your next start\n"
					+ "of p300 it will automatically be started in the system tray.";
		}

		if (headless) {
			D.out(msg);
		} else {
			JOptionPane.showMessageDialog(null, msg, "information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Start a scheduler that send multicast messages and prints upload
	 * connections
	 * 
	 * Start all important threads
	 */
	private static void startThreadsAndObjects() {
		// every 60 seconds (after 30 seconds)
		MainDialog.timer = new Timer();
		// MainDialog.timer.schedule(new TimerTask() {
		// public void run() {
		// D.out ("Start!!!!");
		// MainDialog.multicastListenThread.sendIAmHere();
		// MainDialog.listenThread.printUploadConnections();
		// D.out ("End!!!!");
		// }
		// }, 30 * 1000, Constants.DISCOVERY_MULTICAST_IAMHERE_INTERVAL_MSEC);

		MainDialog.bandwidthThread.start();
		MainDialog.listenThread.start();
		MainDialog.multicastListenThread.start();
		MainDialog.hostFinderThread.start();
		MainDialog.indexerThread.start();
		MainDialog.updaterThread.start();
		MainDialog.newVersionNotificationThread.start();

		if (MainDialog.instance != null) {
			MainDialog.hostWatchThread.addObserver(LANHostsTreeItem.instance());
		}
		MainDialog.hostWatchThread.addObserver(MainDialog.hostMap);
		MainDialog.hostWatchThread.start();

		MainDialog.internetListenThread.start();

		MainDialog.downloadManager.start();
	}

	public ChatWindowMap chatWindowMap;

	public LanMessageRouter lanMessageRouter;

	public ConfigurationPanel configurationPanel;

	private JTextArea consoleField;

	protected JScrollPane consoleFieldSP;

	JPanel infoPanel = null;

	/**
	 * A panel showing a "not implemented yet" message
	 * 
	 * @author guruz
	 */
	protected JPanel notImplementedYetPanel;

	/**
	 * A panel with the running downloads
	 */
	public DownloadsPanel downloadsPanel;

	/**
	 * 
	 * 
	 * @author guruz
	 */
	MainTree tree = null;

	/**
	 * The panel showing the search query + buttons
	 * 
	 * @author tomcat
	 */

	public JComponent consolePanel;

	public JPanel uploadsPanel;

	/**
	 * 
	 * 
	 * @author guruz
	 */
	java.awt.Window realWindow = null;

	/**
	 * 
	 * 
	 * @author guruz
	 * @see de.guruz.guruztray.interfaces.GuruztrayManager
	 * @see #createTrayIcon()
	 */
	GuruztrayManager guruztrayManager = null;

	/**
	 * This call changes the content of the info panel at the right
	 * 
	 * @param c
	 */
	private Map<JComponent, Window> frames = new HashMap<JComponent, Window>();

	private JTextField m_searchTextField;

	/**
	 * Initialize main dialog and show it This is purely GUI stuff
	 * 
	 * FIXME most of this stuff needs to be put out from here and put into
	 * classes
	 * 
	 * @author guruz
	 */
	protected MainDialog() {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				"p300");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		MainDialog.instance = this;

		this.infoPanel = new InfoPanel();

		this.createTree();

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		mainPanel.add(new JScrollPane(this.tree), BorderLayout.CENTER);

		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		final JButton searchButton = new JButton(IconChooser.getSearchIcon());
		searchButton.setToolTipText("Search");
		searchButton.addActionListener(this);
		searchButton.setActionCommand(MainDialog.ACTION_SEARCH);
		searchButton.setEnabled(false);

		searchPanel.add(searchButton, BorderLayout.EAST);
		JPanel textFieldPanel = new JPanel(new BorderLayout());
		m_searchTextField = new JTextField();
		textFieldPanel.add(m_searchTextField, BorderLayout.CENTER);
		m_searchTextField.getDocument().addDocumentListener(
				new DocumentListener() {

					public void changedUpdate(DocumentEvent e) {
						searchButton.setEnabled(m_searchTextField.getText()
								.length() > 0);

					}

					public void insertUpdate(DocumentEvent e) {
						changedUpdate(e);

					}

					public void removeUpdate(DocumentEvent e) {
						changedUpdate(e);

					}
				});
		m_searchTextField.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseMoved(MouseEvent e) {
				m_searchTextField.requestFocusInWindow();

			}
		});

		searchPanel.add(textFieldPanel, BorderLayout.CENTER);
		mainPanel.add(searchPanel, BorderLayout.NORTH);

		final JButton homeButton = new JButton(IconChooser.getSmallP300Icon());
		homeButton.setText("p300");

		JButton webinterfaceButton = new JButton(new WebinterfaceAction());
		// webinterfaceButton.setText("");

		final JPopupMenu homeMenu = MainButtonPopupMenu.instance();




		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		// System.out.println(buttonPanel.getLayout());
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(homeButton);
		buttonPanel.add(webinterfaceButton);
		buttonPanel.add(Box.createHorizontalGlue());

		homeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Dimension d = homeMenu.getPreferredSize();

				if (d != null && d.height != 0 && d.width != 0) {
					homeMenu.show(homeButton, 0, 0 - d.height);
				} else {
					homeMenu.show(homeButton, 0, 0);
				}
			}
		});

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.notImplementedYetPanel = new NotImplementedYetPanel();
		this.downloadsPanel = new DownloadsPanel();
		this.configurationPanel = new ConfigurationPanel();
		this.consolePanel = new ConsolePanel();
		this.uploadsPanel = new UploadsPanel();

		// if we have a tray we open a JDialog, else we
		// open a JFrame
		// because JFrame is in taskbar which we do not need
		// when having
		// a tray
		boolean haveTray = this.createTrayIcon();

		String windowTitle = "p300 (revision " + Configuration.getSVNRevision()
				+ ")";

		if (haveTray) {
			this.realWindow = new JDialog((java.awt.Frame) null, windowTitle);
			this.realWindow.setLayout(new BorderLayout());

			JDialog realWindowDialog = (JDialog) this.realWindow;
			realWindowDialog.add(mainPanel, BorderLayout.CENTER);
			// realWindowDialog.add(splitPane,
			// BorderLayout.CENTER);
			// only hide when closing,
			// closing is possible via
			// tray menu
			realWindowDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

			realWindowDialog.getRootPane().setDefaultButton(searchButton);

			// tell the tray which window to
			// show/hide
			this.guruztrayManager.setAssociatedWindow(this.realWindow);
		} else {
			this.realWindow = new JFrame(windowTitle);
			this.realWindow.setLayout(new BorderLayout());
			JFrame realWindowFrame = (JFrame) this.realWindow;

			// realWindowFrame.add(splitPane,
			// BorderLayout.CENTER);
			realWindowFrame.add(mainPanel, BorderLayout.CENTER);
			realWindowFrame.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

			realWindowFrame.getRootPane().setDefaultButton(searchButton);
		}

		this.realWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowActivated(arg0);
				m_searchTextField.requestFocusInWindow();
			}

			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowGainedFocus(arg0);
				m_searchTextField.requestFocusInWindow();
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				super.windowOpened(arg0);
				m_searchTextField.requestFocusInWindow();
			}

			public void windowDeactivated(WindowEvent e) {
				try {
					if (tree.isEditing())
						tree.stopEditing();
				} catch (Exception ex) {

				}
			}
		});

		// set size
		int preferedWidth = (int) Math.max(buttonPanel.getPreferredSize()
				.getWidth(), searchPanel.getPreferredSize().getWidth());
		this.realWindow.setSize((int) (preferedWidth * 1.3), 350);

		// the size may be larger than the screen size available
		Rectangle screenRect = java.awt.GraphicsEnvironment
				.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		Rectangle windowRect = this.realWindow.getBounds();

		if (windowRect.width > screenRect.width) {
			windowRect.width = screenRect.width;
			windowRect.x = screenRect.x;
		}
		if (windowRect.height > screenRect.height) {
			windowRect.height = screenRect.height;
			windowRect.y = screenRect.y;
		}

		this.realWindow.setBounds(windowRect);

		// center it
		this.realWindow.setLocationRelativeTo(null);

		// select
		// the
		// root
		// node
		this.tree.setSelectionRow(0);

		// if we have do not have a splash show us now
		if (MainDialog.guruzsplashManager == null) {
			this.setGUIEnabled(false);
			this.realWindow.setVisible(true);
		}

		chatWindowMap = new ChatWindowMap();
		lanMessageRouter = new LanMessageRouter();
		UiMessageRouter uiMessageRouter = new UiMessageRouter();
		LanMessageRemoteOutbox lanMessageRemoteOutbox = new LanMessageRemoteOutbox();

		lanMessageRouter.setUiMessageRouter(uiMessageRouter);
		lanMessageRouter.setLanMessageRemoteOutbox(lanMessageRemoteOutbox);
		lanMessageRemoteOutbox.setUiMessageRouter(uiMessageRouter);
	}

	public void actionPerformed(ActionEvent e) {
		String acs = e.getActionCommand();

		if (acs.equals(MainDialog.ACTION_OPEN_MY_WEBINTERFACE)) {
			new WebinterfaceAction().actionPerformed(null);
		} else if (acs.equals(MainDialog.ACTION_ABOUT)) {
			String URL = MainDialog.getMyURL() + "about";
			BareBonesBrowserLaunch.openURL(URL);
		} else if (acs.equals(MainDialog.ACTION_QUIT)) {
			System.exit(0);
		} else if (acs.equals(MainDialog.ACTION_SHOW)) {
			this.showP300();
		} else if (acs.equals(MainDialog.ACTION_HIDE)) {
			this.hideP300();
		} else if (acs.equals(MainDialog.ACTION_SEARCH)) {
			this.startSearchFromMainWindow();
		} else if (acs
				.equals(MainDialog.ACTION_OPEN_WEBINTERFACE_FOR_SELECTED_HOST)) {
			try {
				LANHostTreeItem ti = (LANHostTreeItem) tree.getEditingPath()
						.getLastPathComponent();
				String url = ti.getHost().toURL();
				BareBonesBrowserLaunch.openURL(url);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (acs.equals(MainDialog.ACTION_BROWSE_SELECTED_HOST)) {
			try {
				LANHostTreeItem ti = (LANHostTreeItem) tree.getEditingPath()
						.getLastPathComponent();

				new BrowseHostAction(ti.getHost(), null).actionPerformed(null);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (acs.equals(MainDialog.ACTION_OPEN_CHAT_FOR_SELECTED_HOST)) {
			try {
				LANHostTreeItem ti = (LANHostTreeItem) tree.getEditingPath()
						.getLastPathComponent();

				Host host = ti.getHost();
				ChatWindowMap cwm = MainDialog.getInstance().chatWindowMap;

				if (cwm != null) {
					cwm.getChatWindowFor(host).setVisible(true);
					cwm.getChatComponentFor(host).tryToFocusInputField();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public void showSubWindow(final Image icon, final String title,
			final JComponent c) {
		showSubWindow(icon, title, c, true);
	}

	public void showSubWindow(final Image icon, final String title,
			final JComponent c, final boolean toForeground) {
		if (c == null)
			return;

		final Window f;
		if (frames.containsKey(c))
			f = frames.get(c);
		else {
			f = new JFrame();
			((JFrame) f).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			f.add(c);
			f.setSize(new Dimension(800, 600));
			f.setLocationByPlatform(true);
			frames.put(c, f);
		}

		Runnable r = new Runnable() {
			public void run() {
				((JFrame) f).setTitle(title);
				if (toForeground || !f.isVisible())
					f.setVisible(true);
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a string to our console text area
	 * 
	 * @param s
	 * @author guruz
	 */
	public void consolePrint(String s) {
		this.consoleField.append(s);
	}

	/**
	 * Adds a line to our console text area
	 * 
	 * @param line
	 * @author guruz
	 */
	public void consolePrintln(String line) {
		this.consoleField.append(line);
		this.consoleField.append("\n");
	}

	/**
	 * Initialize and show the tray icon if support is there
	 * 
	 * @author guruz
	 * @return True: Tray icon visible; False: No tray icon support
	 * @see #guruztrayManager
	 */
	protected boolean createTrayIcon() {

		try {
			String className = "de.guruz.guruztray.implementation.GuruztrayManagerImplementation";
			Class<?> c = Class.forName(className);
			Object o = c.newInstance();
			this.guruztrayManager = (GuruztrayManager) o;

			// System.out.println ("Tray supported = " +
			// guruztrayManager.isSupported());

			if (!this.guruztrayManager.isSupported()) {
				throw new Exception("Tray not supported");
			}

			Image im_22x22 = null;
			java.net.URL imgURL = Thread.currentThread()
					.getContextClassLoader().getResource(
							"de/guruz/p300/requests/static/trayicon_22x22.png");
			if (imgURL != null) {
				im_22x22 = Toolkit.getDefaultToolkit().createImage(imgURL);
			} else {
				System.err.println("Couldn't load image");
				throw new Exception("Image for tray icon could not be loaded");
			}

			Image im_16x16 = null;
			imgURL = Thread.currentThread().getContextClassLoader()
					.getResource(
							"de/guruz/p300/requests/static/trayicon_16x16.png");
			if (imgURL != null) {
				im_16x16 = Toolkit.getDefaultToolkit().createImage(imgURL);
			} else {
				System.err.println("Couldn't load image");
				throw new Exception("Image for tray icon could not be loaded");
			}

			// test
			// p300.png
			imgURL = Thread.currentThread().getContextClassLoader()
					.getResource(
							"de/guruz/p300/requests/static/p300_bordered.png");
			if (imgURL != null) {
				im_16x16 = Toolkit.getDefaultToolkit().createImage(imgURL);
				im_22x22 = Toolkit.getDefaultToolkit().createImage(imgURL);
			} else {
				System.err.println("Couldn't load image");
				throw new Exception("Image for tray icon could not be loaded");
			}

			PopupMenu pm = new PopupMenu();

			MenuItem mi = null;

			mi = new MenuItem("Show");
			mi.setActionCommand(MainDialog.ACTION_SHOW);
			mi.addActionListener(this);
			pm.add(mi);
			mi = new MenuItem("Open Webinterface");
			mi.setActionCommand(MainDialog.ACTION_OPEN_MY_WEBINTERFACE);
			mi.addActionListener(this);
			pm.add(mi);

			pm.addSeparator();

			mi = new MenuItem("Hide");
			mi.setActionCommand(MainDialog.ACTION_HIDE);
			mi.addActionListener(this);
			// mi.setEnabled(false);
			pm.add(mi);
			mi = new MenuItem("Quit");
			mi.setActionCommand(MainDialog.ACTION_QUIT);
			mi.addActionListener(this);
			// mi.setEnabled(false);
			pm.add(mi);

			pm.addSeparator();

			mi = new MenuItem("About");
			mi.setActionCommand(MainDialog.ACTION_ABOUT);
			mi.addActionListener(this);
			mi.setEnabled(true);
			pm.add(mi);

			this.guruztrayManager.setTrayIcon(im_16x16, im_22x22, "p300", pm);

		} catch (Throwable t) {
			D
					.out("No tray icon supported on this platform or java version (msg=\""
							+ t.getMessage() + "\"");

			// t.printStackTrace();
			return false;
		}
		;

		return (true);

	}

	private void createTree() {
		MainDialogTreeModel mdtm = MainDialogTreeModel.instance();
		this.tree = new MainTree(mdtm);
		this.tree.setRootVisible(false);
		this.tree.setRowHeight(0);
		this.tree.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

		this.tree.setShowsRootHandles(false);

		for (int i = 0; i < 10; i++) {
			this.tree.expandRow(i);
		}

		tree.setEditable(true);
		MainDialogTreeCellRenderer treeCellRender = new MainDialogTreeCellRenderer(
				tree);
		MainDialogTreeCellEditor treeCellEditor = new MainDialogTreeCellEditor(
				treeCellRender, tree);

		this.tree.setCellEditor(treeCellEditor);
		this.tree.setCellRenderer(treeCellRender);

		this.tree.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent me) {

			}

			public void mouseEntered(MouseEvent arg0) {

			}

			public void mouseExited(MouseEvent arg0) {

			}

			public void mousePressed(MouseEvent me) {
				if (me.getClickCount() > 1 && me.getButton() == me.BUTTON1) {
					me.consume();
					if (tree.getSelectionPath() != null
							&& tree.getSelectionPath().getLastPathComponent() == InternetHostsTreeItem
									.instance())
						showSubWindow(null, "Not implemented yet",
								notImplementedYetPanel);
					else if (tree.getSelectionPath() != null
							&& tree.getSelectionPath().getLastPathComponent() == LANHostsTreeItem
									.instance())
						new AddHostAction().actionPerformed(null);
				}

			}

			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		this.tree.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			public void mouseMoved(MouseEvent e) {
				try {
					if (!realWindow.isActive())
						return;

					TreePath p = tree.getClosestPathForLocation(e.getX(), e
							.getY());

					if (p != null)
						tree.setSelectionPath(p);

					if (tree.getEditingPath() != p
							&& p.getLastPathComponent() instanceof LANHostTreeItem) {
						// System.out.println ("mouse motion listener");
						tree.setSelectionPath(p);
						tree.startEditingAtPath(p);

					}

					if (!(p.getLastPathComponent() instanceof LANHostTreeItem)) {
						tree.stopEditing();
					}

				} catch (Exception ex) {

				}

			}
		});

		this.tree.addTreeWillExpandListener(new TreeWillExpandListener() {

			public void treeWillCollapse(TreeExpansionEvent event)
					throws ExpandVetoException {
				throw new ExpandVetoException(event);
			}

			public void treeWillExpand(TreeExpansionEvent event)
					throws ExpandVetoException {
			}
		});
	}

	/**
	 * Hide the p300 window
	 * 
	 * @author guruz
	 */
	private void hideP300() {
		if (this.realWindow == null) {
			return;
		}

		this.realWindow.setVisible(false);

	}

	public boolean isDownloadsPanelShown() {
		// System.out.println ("downloads panel showing " +
		// downloadsPanel.isShowing());
		return downloadsPanel.isShowing();
	}

	/**
	 * 
	 * 
	 * @author guruz
	 */
	private void loadOSXInterface() {
		if (!OsUtils.isOSX()) {
			return;
		}

		try {
			OSXInterface i = null;
			String className = "de.guruz.p300.osx.OSX";
			Class<?> c = Class.forName(className);
			Object o = c.newInstance();
			i = (OSXInterface) o;
			i.setCallback(this);

		} catch (Throwable t) {
			// e.printStackTrace();
			return;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.guruz.p300.osx.OSXCallbackInterface#OSXabout()
	 */
	public void OSXabout() {
		// System.out.println ("OSX about");
		this.actionPerformed(new ActionEvent(this, 0, MainDialog.ACTION_ABOUT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.guruz.p300.osx.OSXCallbackInterface#OSXreOpenApplication()
	 */
	public void OSXreOpenApplication() {
		this.showP300();
	}

	/**
	 * 
	 * 
	 * @param b
	 * @author guruz
	 */
	private void setGUIEnabled(boolean b) {
		this.tree.setEnabled(b);
	}

	/**
	 * 
	 * 
	 * @author guruz
	 */
	private void showP300() {
		if (this.realWindow == null) {
			return;
		}

		// this.valueChanged(null);
		this.realWindow.setVisible(true);
		this.realWindow.requestFocus();
		this.realWindow.toFront();
	}

	private void startSearchFromMainWindow() {
		String s = m_searchTextField.getText();
		SearchResultsWindow srw = new SearchResultsWindow(s);

		// JFrame f = new JFrame ();
		// f.setTitle(srw.getTitle ());
		// f.setLocationByPlatform(true);
		// f.setVisible(true);

		showSubWindow(null, srw.getTitle(), srw);

		srw.asyncSearchStartAllHosts();

	}

	public static HostAllowanceManager getHostAllowanceManager() {
		return m_hostAllowanceManager;
	}

}
