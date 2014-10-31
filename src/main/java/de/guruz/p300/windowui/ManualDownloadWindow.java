package de.guruz.p300.windowui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.guruz.p300.MainDialog;
import de.guruz.p300.davclient.EntityInformationFetcher;
import de.guruz.p300.dirbrowser.RemoteDir;
import de.guruz.p300.dirbrowser.RemoteEntity;
import de.guruz.p300.dirbrowser.RemoteFile;
import de.guruz.p300.http.HTTPHeadFetcher;
import de.guruz.p300.http.HTTPHeaders;
import de.guruz.p300.windowui.actions.ShowDownloadsAction;

public class ManualDownloadWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	JButton cancelButton;

	JButton closeButton;

	JTextArea consoleArea;

	protected String m_url;

	boolean m_aborted = false;

	public ManualDownloadWindow(String u) {
		m_url = u;

		this.setLayout(new BorderLayout());

		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setLineWrap(true);
		consoleArea.setWrapStyleWord(false);

		
		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(closeButton);

		add(new JScrollPane(consoleArea));
		add(bottomPanel, BorderLayout.SOUTH);

		setSize(600, 200);
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void addConsoleLine(String l) {
		consoleArea.append(l + "\n");
	}

	public void startDownload() {
		new Thread(startDownloadRunnable).start();
	}

	Runnable startDownloadRunnable = new Runnable() {
		public void run() {
			try {
				if (!m_url.toLowerCase().startsWith("http://"))
					m_url = "http://" + m_url;
				
				addConsoleLine("Starting to download " + m_url);

				RemoteEntity entity = null;
				EntityInformationFetcher informationFetcher = new EntityInformationFetcher(
						m_url);
				try {
					entity = informationFetcher.call();
				} catch (Exception e) {
					addConsoleLine("Could not PROPFIND: " + e.getMessage());
					entity = null;
				}
				
				if (m_aborted)
					return;
				
				if (entity != null)
				{
					// can get entity properly
					if (entity.isDirectory())
					{
						// start dir download
						addConsoleLine("Entity is a directory");
						MainDialog.downloadManager.startDownload(Collections.singletonList (informationFetcher.getHostLocation ()), (RemoteDir) entity);
						
					} else {
						// start file download 
						addConsoleLine("Entity is a file (" + entity.getSize() + " bytes)");
						MainDialog.downloadManager.startDownload( informationFetcher.getHostLocation (), (RemoteFile) entity, entity.getSize());
					}
					
					new ShowDownloadsAction ().actionPerformed(null);
				}
				else
				{
					if (m_aborted)
						return;
					
					// try a HEAD request
					addConsoleLine("Trying a HEAD request");
					try {
						HTTPHeadFetcher headFetcher = new HTTPHeadFetcher (m_url);
						HTTPHeaders headers = headFetcher.call();
						
						String lengthHeader = headers.getHeader("Content-Length");

						if (lengthHeader == null || lengthHeader.trim().length() == 0)
						{
							throw new Exception ("Could not get length of remote file");
						} else {
							long length = Integer.parseInt (lengthHeader);
							
							if (length >= 0)
							{
								// add file here
								addConsoleLine("Entity length is " + length + " bytes");
								RemoteFile rf = new RemoteFile (headFetcher.getPath());
								rf.setSize(length);
								MainDialog.downloadManager.startDownload(headFetcher.getHostLocation (), rf, length);
								new ShowDownloadsAction ().actionPerformed(null);
							} else {
								throw new Exception ("Invalid file length: " + length);
							}
						}
							
					} catch (Exception e) {
						throw e;
					}
				}
				
				
				
			} catch (Exception e) {
				addConsoleLine("Failure: " + e.getMessage());
			}
			
			
		}
	};

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("close"))
		{
			m_aborted = true;
			
			setVisible(false);
			
		}
		
	}

}
