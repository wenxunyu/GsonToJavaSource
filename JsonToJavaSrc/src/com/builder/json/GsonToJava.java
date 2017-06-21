/**
 * 版    权:  GsonToJavaSource,  All rights reserved
 */
package com.builder.json;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.builder.json.config.Config;
import com.builder.json.utils.Tools;

/**
 * Gson转换成Java源码
 * 
 * @author wenxunyu
 * @version [GsonToJavaSource 2015-2-13]
 */
public class GsonToJava extends JFrame {
	private int frameW = 500, frameH = 700;
	private static final long serialVersionUID = 1L;
	private JPopupMenu popup = new JPopupMenu();
	private Pattern patternPackageName = Pattern.compile("([_a-z][_a-z0-9]*([.][_a-z][_a-z0-9]*)*)");
	private Pattern patternClassName = Pattern.compile("([_A-Z][_a-zA-Z0-9]*)");

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GsonToJava gsonToJava = new GsonToJava();
				gsonToJava.setVisible(true);
			}
		});
	}

	private String outPath = "c://";

	public GsonToJava() {
		setTitle("GsonToJavaSource");
		setIconImage(Tools.getImage("image/jsonFile.png", getClass()));
		Tools.setLocation(this, frameW, frameH);
		setSize(frameW, frameH);
		// setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuBar();

		JPanel topPanel = new JPanel();
		topPanel.setSize(frameW - 10, frameH - 30);
		topPanel.setLayout(new BorderLayout());

		topPanel.add(BorderLayout.NORTH, getTop());
		topPanel.add(BorderLayout.CENTER, getCenter());
		topPanel.add(BorderLayout.SOUTH, getBottom());
		topPanel.setBackground(Color.GRAY);

		getContentPane().add(topPanel);

		JLabel text = new JLabel("文件输出路径:");
		text.setBackground(Color.WHITE);
	}

	public JPanel getTop() {
		JPanel inputPanel = new JPanel();

		GridBagLayout gbl_inputPanel = new GridBagLayout();
		gbl_inputPanel.rowHeights = new int[] { 30 };
		gbl_inputPanel.columnWeights = new double[] { 0.5, 4.5, 0.5, 4.5 };
		gbl_inputPanel.rowWeights = new double[] { 0.0 };
		inputPanel.setLayout(gbl_inputPanel);

		JLabel LFileName = new JLabel("文件名:");
		GridBagConstraints gbc_LFileName = new GridBagConstraints();
		gbc_LFileName.insets = new Insets(5, 5, 5, 5);
		gbc_LFileName.gridx = 0;
		gbc_LFileName.gridy = 0;
		inputPanel.add(LFileName, gbc_LFileName);

		fileNameText = new JTextField(Config.getInstant().getClassName());
		fileNameText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				classNameQualified();
			}
		});

		GridBagConstraints gbc_fileNameText = new GridBagConstraints();
		gbc_fileNameText.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileNameText.insets = new Insets(5, 5, 5, 5);
		gbc_fileNameText.gridx = 1;
		gbc_fileNameText.gridy = 0;
		inputPanel.add(fileNameText, gbc_fileNameText);
		fileNameText.setColumns(10);

		JLabel LPackageName = new JLabel("包名:");
		GridBagConstraints gbc_LPackageName = new GridBagConstraints();
		gbc_LPackageName.insets = new Insets(5, 5, 5, 5);
		gbc_LPackageName.gridx = 2;
		gbc_LPackageName.gridy = 0;
		inputPanel.add(LPackageName, gbc_LPackageName);

		packageNameText = new JTextField(Config.getInstant().getPackageName());
		packageNameText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				packageNameQualified();
			}
		});
		GridBagConstraints gbc_packageNameText = new GridBagConstraints();
		gbc_packageNameText.insets = new Insets(5, 5, 5, 5);
		gbc_packageNameText.fill = GridBagConstraints.HORIZONTAL;
		gbc_packageNameText.gridx = 3;
		gbc_packageNameText.gridy = 0;
		inputPanel.add(packageNameText, gbc_packageNameText);
		packageNameText.setColumns(10);
		return inputPanel;
	}

	private boolean classNameQualified() {
		Matcher matcher = patternClassName.matcher(getOutJavaName());
		if (!matcher.matches()) {
			dialog("类名不合法，请检查类名", "提示");
			return true;
		}
		return false;
	}

	private boolean packageNameQualified() {
		Matcher matcher = patternPackageName.matcher(getPagPath());
		if (!matcher.matches()) {
			dialog("包名不合法，请检查包名", "提示");
			return true;
		}
		return false;
	}

	private String getPagPath() {
		String pagNameString = packageNameText.getText().trim();
		if (pagNameString == null || pagNameString == "") {
			return "com.robot.dev";
		}
		return pagNameString;
	}

	private String getOutJavaName() {
		String name = fileNameText.getText().trim();
		if (name == null || name == " " || name == "" || name.length() == 0)
			return "GsonToJava";
		return name;
	}

	private JPanel getBottom() {
		JPanel jPanel = new JPanel(new GridLayout(1, 2));
		JButton buttonFramet = new JButton("格式化");
		JButton save = new JButton("转换");
		buttonFramet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String json = contentArea.getText().trim();
				if (json.startsWith("{")) {
					try {
						JSONObject jsonObject = new JSONObject(json);
						json = jsonObject.toString(4);
					} catch (JSONException exception) {
						dialog(exception.getMessage(), "Json格式有误");
						return;
					}
				} else if (json.startsWith("[")) {
					// 会出现异常
					try {
						JSONArray jsonArray = new JSONArray(json);
						json = jsonArray.toString(4);
					} catch (JSONException exception) {
						dialog(exception.getMessage(), "Json格式有误");
						return;
					}
				} else {
					dialog("不是JSON字符串", "提示");
					return;
				}
				contentArea.setText(json);
				// dialog("格式化成功", null);
			}
		});
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (classNameQualified() || packageNameQualified()) {
					return;
				}
				if (isRun) {
					dialog("正在保存中", "提示");
					return;
				}
				json = contentArea.getText().trim();
				new Thread(runable).start();
			}
		});
		jPanel.add(buttonFramet);
		jPanel.add(save);
		return jPanel;
	}

	private boolean isRun;
	private Runnable runable = new Runnable() {
		@Override
		public void run() {
			isRun = true;
			String javaName = getOutJavaName();
			new ConvertBridge(json, outPath, getPagPath(), javaName).run();
			isRun = false;

		}
	};

	private JTextArea contentArea = new JTextArea();
	private JScrollPane infoScrollPane = new JScrollPane();

	private JSplitPane getCenter() {
		initPopup();
		contentArea.setEditable(true);
		contentArea.setLineWrap(true);
		contentArea.setComponentPopupMenu(popup);
		infoScrollPane.setViewportView(contentArea);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, null, infoScrollPane);
		splitPane.setDividerLocation(HEIGHT - 200);
		return splitPane;
	}

	JMenuBar jMenuBar;// 菜单条

	/** 菜单栏 */
	public void menuBar() {
		jMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("文件");
		JMenu editMenu = new JMenu("编辑");
		JMenu systemMenu = new JMenu("系统");

		jMenuBar.add(fileMenu);
		jMenuBar.add(editMenu);
		jMenuBar.add(systemMenu);
		setJMenuBar(jMenuBar);

		/* 选择需要转换的json文件 */
		JMenuItem selectFile = new JMenuItem("选择json文件");
		selectFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
		/* 选择输出文件根目录 如..../project/src */
		JMenuItem selectOutDir = new JMenuItem("选择输出目录");
		selectOutDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFolder();
			}
		});

		JMenuItem openOutDir = new JMenuItem("打开输出目录");
		openOutDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = new File(outPath);
				try {
					java.awt.Desktop.getDesktop().open(file);
				} catch (IOException exception) {
					JOptionPane.showOptionDialog(null, "文件夹不存在", "提示", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE, null, new String[] { "确认" }, null);
				}
			}
		});

		JMenuItem exit = new JMenuItem("退出");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(selectFile);
		fileMenu.addSeparator();
		fileMenu.add(selectOutDir);
		fileMenu.add(openOutDir);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		editMenu.add(new AbstractAction("清空内容") {

			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.setText("");
			}
		});

		systemMenu.add(new AbstractAction("帮助") {
			@Override
			public void actionPerformed(ActionEvent e) {
				openURI("https://github.com/wenxunyu/GsonToJavaSource");
			}
		});
		systemMenu.add(new AbstractAction("反馈") {
			@Override
			public void actionPerformed(ActionEvent e) {
				openURI("https://github.com/wenxunyu/GsonToJavaSource");
			}
		});
	}

	private void openURI(String url) {
		try {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				URI uri = new URI(url);
				desktop.browse(uri);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initPopup() {
		JMenuItem clearItem = new JMenuItem("清除");
		clearItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.setText("");
			}
		});
		JMenuItem coypItem = new JMenuItem("复制");
		coypItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.copy();
			}
		});
		JMenuItem cutItem = new JMenuItem("剪切");
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.cut();
			}
		});
		JMenuItem pasteItem = new JMenuItem("粘贴");
		pasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.paste();
			}
		});
		JMenuItem allSelectItem = new JMenuItem("全选");
		allSelectItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contentArea.selectAll();
			}
		});
		popup.add(pasteItem);
		popup.add(coypItem);
		popup.add(cutItem);
		popup.add(allSelectItem);
		popup.add(clearItem);
	}

	private JFileChooser fc = new JFileChooser("C:\\");
	private String path = null;

	/** 打开文件夹 */
	private void openFolder() {
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 只能选择目录
		File f = null;
		int flag = -1;
		try {
			flag = fc.showOpenDialog(null);
		} catch (HeadlessException head) {
			System.out.println("Open File Dialog ERROR!");
		}
		if (flag == JFileChooser.APPROVE_OPTION) {
			// 获得该文件
			f = fc.getSelectedFile();
			path = f.getPath();
		}
		if (path == null) {
			JOptionPane.showOptionDialog(null, "未选择任何文件夹", "警告", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new String[] { "确认" }, null);
			return;
		}
		setTitle("GsonToJavaSource " + path);
		outPath = path;

	}

	private String json = "";
	private JTextField fileNameText;
	// Pattern.compile("^([_a-z][_a-z0-9]*([.][_a-z][_a-z0-9]*)*)");//包名判定
	private JTextField packageNameText;

	/** 打开文件 */
	private void openFile() {
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);// 只能选择目录
		fc.setFileFilter(new FileNameExtensionFilter("json", "json"));
		fc.setDialogTitle("选择要转换的Json文件");
		int result = fc.showOpenDialog(GsonToJava.this); // 打开"打开文件"对话框
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				json = Tools.getFileContext(file.getPath());
			} catch (IOException e) {
				dialog("打开文件失败", null);
				return;
			}
			contentArea.setText(json);
			String fileName = file.getName();
			String name = fileName.substring(0, fileName.lastIndexOf('.'));
			fileNameText.setText(Tools.firstUpperCase(name));
		}
	}

	/**
	 * 提示框
	 * 
	 * @param message
	 *            提示内容
	 * @param title
	 *            标题
	 * @see JOptionPane#showOptionDialog(java.awt.Component, Object, String,
	 *      int, int, javax.swing.Icon, Object[], Object)
	 */
	public static void dialog(String message, String title) {
		if (title == null) {
			title = "提示";
		}
		JOptionPane.showOptionDialog(null, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[] { "确认" }, null);
	}

	/**
	 * 是，否，取消
	 * 
	 * @param message
	 *            提示信息
	 * @return int 0 是，1否，2取消
	 * @see JOptionPane#showConfirmDialog(java.awt.Component, Object)
	 */
	public static int confirm(Component parentComponent, String message) {
		return JOptionPane.showConfirmDialog(parentComponent, message);
	}
}
