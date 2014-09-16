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

import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.tree.TreeNode;

import de.guruz.p300.hosts.Host;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.panels.LANHostPanel;

public class LANHostTreeItem extends GenericTreeItem implements Comparable<LANHostTreeItem> {



	//static URL onlineIconURL =  Request.class.getResource("static/mini/computer.png");
	//static URL offlineIconURL = Request.class.getResource("static/mini/computer_grey.png");
	static ImageIcon onlineIcon = IconChooser.getOnlineHostImageIcon(); //new ImageIcon(onlineIconURL);
	static ImageIcon offlineIcon = IconChooser.getOfflineHostImageIcon(); //new ImageIcon(offlineIconURL);	
	
	public LANHostTreeItem() {
		super();
		//this.imgURL = onlineIconURL;
		this.icon = onlineIcon;
	}

	protected Host host = null;
	public Host getHost () {
		return this.host;
	}
	public void setHost (Host h) {
		this.host = h;
		
	}
	
	@Override
	public Icon getIcon() {
		if (host == null)
			return offlineIcon;
		else
		{
			if (host.seemsOnline())
				return onlineIcon;
			else
				return offlineIcon;
		}
	}
	
	
	@Override
	public Enumeration<TreeNode> children() {
		return null;
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public TreeNode getChildAt(int arg0) {
		return null;
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public int getIndex(TreeNode arg0) {
		return 0;
	}

	@Override
	public TreeNode getParent() {
		return LANHostsTreeItem.instance();
	}
	
	@Override
	public String toString () {
		return this.host.getDisplayName();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
	public int compareTo(LANHostTreeItem ti) {
		return (this.getHost().getDisplayName().compareTo(ti.getHost().getDisplayName()));
	}
	
	@Override
	public JComponent createAssociatedPanel() {
		LANHostPanel lhp = new LANHostPanel (this.getHost ());
		this.setAssociatedPanel(lhp);
		return lhp;
	}
	

}
