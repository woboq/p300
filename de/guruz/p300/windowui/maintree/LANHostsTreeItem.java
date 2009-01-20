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
package de.guruz.p300.windowui.maintree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.CaseInsensitiveStringComparator;
import de.guruz.p300.utils.IconChooser;

public class LANHostsTreeItem extends GenericTreeItem implements Observer {
	@Override
	public JComponent getAssociatedPanel() {
		return null;
	}

	@Override
	public Runnable getAssociatedRunnable() {
		return null;
	}

	protected static LANHostsTreeItem i = null;
	
	public static synchronized LANHostsTreeItem instance () {
		if (LANHostsTreeItem.i == null) {
			LANHostsTreeItem.i = new LANHostsTreeItem ();
		}
		
		return LANHostsTreeItem.i;
	}
	
	Object synchronizer = new Object ();
	
	protected List<LANHostTreeItem> onlineHosts = new ArrayList<LANHostTreeItem>();

	protected List<LANHostTreeItem> offlineHosts = new ArrayList<LANHostTreeItem>();
	
	protected List<LANHostTreeItem> allHosts = new ArrayList<LANHostTreeItem>();

	protected LANHostsTreeItem () {
		this.imgURL = null;
		this.icon = IconChooser.iconImageFromResource("16x16/network-receive.png");
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return this.allHosts.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return this.allHosts.size();
	}
	
	@Override
	public int getIndex(TreeNode node) {
		return this.allHosts.indexOf(node);
	}


	
	
	@Override
	public TreeNode getParent() {
		return RootTreeItem.instance();
	}
	@Override
	public boolean isLeaf() {
		return false;
	}
	@Override
	public String toString () {
		return "LAN/VPN hosts";
	}
	public void update(Observable sender, Object h) {
		if (h != null) {
			// new object
			Host host = (Host) h;
			LANHostTreeItem lhti = new LANHostTreeItem ();
			lhti.setHost(host);
			
			if (host.state == Host.HostStateType.ONLINE) {
				this.onlineHosts.add(lhti);
			} else {
				this.offlineHosts.add (lhti);
			}
		} else {
			// only the online/offline state has changed
			synchronized (this.synchronizer) {
				// check if any online hosts are now offline
				Iterator<LANHostTreeItem> iterator = this.onlineHosts.iterator();
				while (iterator.hasNext()) {
					LANHostTreeItem item = iterator.next();
					
					if (item.getHost().state == Host.HostStateType.OFFLINE) {
						this.offlineHosts.add(item);
						//onlineHosts.remove(item);
						iterator.remove();
					}
				}
				
				// check if any offline hosts are now online
				iterator = this.offlineHosts.iterator();
				while (iterator.hasNext()) {
					LANHostTreeItem item = iterator.next();
					
					if (item.getHost().state == Host.HostStateType.ONLINE) {
						this.onlineHosts.add(item);
						//offlineHosts.remove(item);
						iterator.remove();
					}
				}
			}
		}
		
		// tell the tree model that we changed
		synchronized (this.onlineHosts) {
			java.util.Collections.sort(this.onlineHosts, CaseInsensitiveStringComparator.instance());
			java.util.Collections.sort(this.offlineHosts, CaseInsensitiveStringComparator.instance());
			
			allHosts.clear();
			allHosts.addAll(onlineHosts);
			allHosts.addAll(offlineHosts);
		}
		
		final LANHostsTreeItem t = this;
		SwingUtilities.invokeLater(new Runnable () {

			public void run() {
				MainDialogTreeModel.instance ().reload(t);
				
			}});
		
		
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		final Iterator<LANHostTreeItem> iterator = this.allHosts.iterator();
		
		return new Enumeration<TreeNode>() {

			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			public TreeNode nextElement() {
				return iterator.next();
			}
			
		};
	}

}
