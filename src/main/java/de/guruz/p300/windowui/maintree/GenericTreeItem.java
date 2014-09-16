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
import javax.swing.JComponent;
import javax.swing.tree.TreeNode;

public class GenericTreeItem implements TreeNode {
	protected java.net.URL imgURL = null;
	
	protected Icon icon = null;
	
	public Icon getIcon () {
		return this.icon;
	}
	
	public Enumeration<? extends TreeNode> children() {
		return null;
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public TreeNode getChildAt(int arg0) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}

	public int getIndex(TreeNode arg0) {
		return 0;
	}

	public TreeNode getParent() {
		return null;
	}

	public boolean isLeaf() {
		return true;
	}
	
	private JComponent associatedPanel = null;
	
	public JComponent getAssociatedPanel () {
		return associatedPanel;
	}

	public void setAssociatedPanel(JComponent p) {
		associatedPanel = p;
		
	}

	public JComponent createAssociatedPanel() {
		return null;
	}
	
	private Runnable associatedRunnable = null;
	
	public Runnable getAssociatedRunnable() {
		return null;
	}
	
	public void setAssociatedRunnable (Runnable r)
	{
		associatedRunnable = r;
	}

}
