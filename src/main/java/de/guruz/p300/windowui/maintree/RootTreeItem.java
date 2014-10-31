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
import java.util.Vector;

import javax.swing.tree.TreeNode;

import de.guruz.p300.utils.IconChooser;

/**
 * The root of all tree items in the main window
 * @author guruz
 *
 */
public class RootTreeItem extends GenericTreeItem {
	protected RootTreeItem () {
		//this.childs.add(ConsoleTreeItem.instance ());
		//this.childs.add(OpenWebinterfaceTreeItem.instance());
		//this.childs.add(ConfigurationTreeItem.instance ());
		//this.childs.add(DownloadsTreeItem.instance ());
		//this.childs.add(UploadsTreeItem.instance ());
		//this.childs.add(SearchTreeItem.instance ());
		this.childs.add(LANHostsTreeItem.instance());
		this.childs.add(InternetHostsTreeItem.instance());
		//this.childs.add(OpenDonationsWebsiteTreeItem.instance());
		
		this.imgURL = null;
		this.icon = IconChooser.iconImageFromResource("16x16/go-home.png");
	}
	
	protected static RootTreeItem i = null;
	public static synchronized RootTreeItem instance () {
		if (RootTreeItem.i == null) {
			RootTreeItem.i = new RootTreeItem ();
		}
		
		return RootTreeItem.i;
	}

	
	Vector<TreeNode> childs = new Vector<TreeNode> ();
	@Override
	public Enumeration<TreeNode> children() {
		Enumeration<TreeNode> e = this.childs.elements();
		return e;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return this.childs.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return this.childs.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		return this.childs.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}
	
	@Override
	public String toString () {
		return "p300";
	}

}
