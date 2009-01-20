package de.guruz.p300.windowui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import de.guruz.p300.MainDialog;
import de.guruz.p300.utils.IconChooser;
import de.guruz.p300.windowui.maintree.LANHostTreeItem;
import de.guruz.p300.windowui.maintree.MainTree;

public class MainDialogTreeCellEditor implements TreeCellEditor {

	public MouseListener m_chatLabelMouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
			MainDialog.getInstance().actionPerformed(
					new ActionEvent(e.getSource(), 0,
							MainDialog.ACTION_OPEN_CHAT_FOR_SELECTED_HOST));
		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	};

	public MouseListener m_browseLabelMouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
			MainDialog
			.getInstance()
			.actionPerformed(
					new ActionEvent(
							e.getSource(),
							0,
							MainDialog.ACTION_BROWSE_SELECTED_HOST));
		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	};

	private MouseListener m_webinterfaceLabelMouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
			MainDialog
					.getInstance()
					.actionPerformed(
							new ActionEvent(
									e.getSource(),
									0,
									MainDialog.ACTION_OPEN_WEBINTERFACE_FOR_SELECTED_HOST));

		}

		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub

		}
	};

	private MainTree m_tree;
	private MainDialogTreeCellRenderer m_treeCellRender;

	public MainDialogTreeCellEditor(MainDialogTreeCellRenderer treeCellRender,
			MainTree tree) {
		m_tree = tree;
		m_treeCellRender = treeCellRender;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object o,
			boolean isSelected, boolean expanded, boolean leaf, int row) {

		//System.out.println("getTreeCellEditorComponent (isEditing="
		//		+ tree.isEditing() + ")");

		JLabel template = (JLabel) m_treeCellRender
				.getTreeCellRendererComponent(tree, o, isSelected, expanded,
						leaf, row, true);
		//caption.setBorder(BorderFactory.createEmptyBorder());

		FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 3, 3);
		JPanel p = new JPanel(fl);

		//System.out.println (template.getBackground());
		
		
		//p.add(new JLabel (caption.getText()));
		
		JLabel caption = new JLabel ();
		caption.setText(template.getText());
		caption.setIcon(template.getIcon());
		
		//p.setOpaque(false);
		p.setBackground(m_treeCellRender.getBackgroundSelectionColor());
		caption.setBackground(m_treeCellRender.getBackgroundSelectionColor());
		caption.setForeground(m_treeCellRender.getTextSelectionColor());
		p.add(caption);
		

		if (o instanceof LANHostTreeItem) {
			//System.out.println("JA!");

			JLabel browseLabel = new JLabel(IconChooser.getBrowseIcon(),
					SwingConstants.LEFT);
			browseLabel.setToolTipText("Browse");
			browseLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
			browseLabel.setOpaque(false);
			browseLabel.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			browseLabel.addMouseListener(m_browseLabelMouseListener);

			JLabel chatLabel = new JLabel(IconChooser.getChatIcon(),
					SwingConstants.LEFT);
			chatLabel.setToolTipText("Chat");
			chatLabel.setOpaque(false);
			chatLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			chatLabel.addMouseListener(m_chatLabelMouseListener);

			JLabel webinterfaceLabel = new JLabel(IconChooser
					.getWebinterfaceIcon(), SwingConstants.LEFT);
			webinterfaceLabel.setToolTipText("Webinterface");
			webinterfaceLabel.setOpaque(false);
			webinterfaceLabel.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			webinterfaceLabel
					.addMouseListener(m_webinterfaceLabelMouseListener);

			p.add(browseLabel);
			p.add(chatLabel);
			p.add(webinterfaceLabel);
		}

		//p.setOpaque(false);

		return p;

	}

	public void addCellEditorListener(CellEditorListener arg0) {
	}

	public void cancelCellEditing() {
	}

	public Object getCellEditorValue() {
		return null;
	}

	public boolean isCellEditable(EventObject e) {
		TreePath tp = m_tree.getSelectionPath();
		if (tp != null && tp.getLastPathComponent() instanceof LANHostTreeItem) {
			return true;
		} else {
			return false;
		}

	}

	public void removeCellEditorListener(CellEditorListener arg0) {
	}

	public boolean shouldSelectCell(EventObject arg0) {
		return false;
	}

	public boolean stopCellEditing() {
		return true;
	}

}
