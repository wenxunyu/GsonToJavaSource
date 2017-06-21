package com.builder.json;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.ux.CheckTreeTableManager;
import org.jdesktop.swingx.ux.IField;
import org.jdesktop.swingx.ux.IJava;

import com.builder.json.entity.FileInfo;
import com.builder.json.entity.JavaEntity;
import com.builder.json.utils.Tools;

public class FieldsUi extends JFrame {
	private static final long serialVersionUID = -8790931104812337114L;
	private JScrollPane sp;
	private ArrayList<DefaultMutableTreeTableNode> defaultMutableTreeTableNodeList;
	private JButton confirm, cancel;

	public FieldsUi(final JavaEntity javaEntity, final FileInfo fileInfo) {
		setSize(800, 600);
		Tools.setLocation(this, 800, 600);
		defaultMutableTreeTableNodeList = new ArrayList<DefaultMutableTreeTableNode>();
		JXTreeTable treetable = new JXTreeTable(new FiledTreeTableModel(createData(javaEntity)));
		CheckTreeTableManager manager = new CheckTreeTableManager(treetable);
		manager.getSelectionModel().addPathsByNodes(defaultMutableTreeTableNodeList);
		treetable.getColumnModel().getColumn(0).setPreferredWidth(150);
		// treetable.setSelectionBackground(treetable.getBackground());
		treetable.expandAll();
		treetable.setCellSelectionEnabled(false);
		final DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
		treetable.setSelectionModel(defaultListSelectionModel);
		defaultListSelectionModel.setSelectionMode(SINGLE_SELECTION);
		defaultListSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				defaultListSelectionModel.clearSelection();
			}
		});
		defaultMutableTreeTableNodeList = null;
		treetable.setRowHeight(30);
		sp = new JScrollPane();
		sp.setViewportView(treetable);
		getContentPane().add(sp);
		getContentPane().add(getBottom(), BorderLayout.PAGE_END);
		confirm.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DataWriter(javaEntity, fileInfo).execute();
				setVisible(false);
			}
		});
		cancel.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
			}
		});
	}

	private JPanel getBottom() {
		JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		confirm = new JButton("确认");
		confirm.setSize(60, 30);
		cancel = new JButton("取消");
		jPanel.add(cancel);
		jPanel.add(confirm);
		return jPanel;
	}

	private DefaultMutableTreeTableNode createData(IJava classEntity) {
		DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode(classEntity);
		createDataNode(root, classEntity);
		return root;
	}

	private void createDataNode(DefaultMutableTreeTableNode root, IJava innerJavaEntity) {
		for (IField field : innerJavaEntity.getFields()) {
			DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(field);
			root.add(node);
			defaultMutableTreeTableNodeList.add(node);
		}
		for (IJava classEntity : innerJavaEntity.getInnerClasss()) {
			DefaultMutableTreeTableNode node = new DefaultMutableTreeTableNode(classEntity);
			root.add(node);
			createDataNode(node, classEntity);
		}

	}
}
