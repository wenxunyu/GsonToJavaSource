/*文 件 名:  Tools.java
 * 版    权:  GsonToJavaSource,  All rights reserved
 * 修 改 人:  wenxunyu
 * 修改时间:  2014-9-11
 */
package com.builder.json.utils;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.json.JSONObject;

import com.builder.json.config.Config;
import com.builder.json.config.Constant;

/**
 * 工具类
 * 
 * @author wenxunyu
 * @version [GsonToJavaSource, 2014-9-11]
 */
public final class Tools {
	private Tools() {
	}

	/**
	 * 设置窗口居中
	 * 
	 * @param jFrame
	 *            需要居中的窗口
	 * @param width
	 *            窗口的宽
	 * @param height
	 *            窗口的高
	 */
	public static void setLocation(JFrame jFrame, int width, int height) {
		Toolkit kit = Toolkit.getDefaultToolkit();// 设置顶层容器框架为居中
		Dimension screenSize = kit.getScreenSize();
		int swidth = screenSize.width;
		int sheight = screenSize.height;
		int x = (swidth - width) / 2;
		int y = (sheight - height) / 2;
		jFrame.setLocation(x, y);
	}

	/**
	 * 设置窗口居中
	 * 
	 * @param jFrame
	 *            需要居中的窗口
	 * @see #setLocation(JFrame, int, int)
	 */
	public static void setLocation(JFrame jFrame) {
		setLocation(jFrame, jFrame.getWidth(), jFrame.getHeight());
	}

	/**
	 * 得到一张Image图片
	 * 
	 * @param imageUrl
	 *            图片所在路径
	 * @param c
	 *            当前操作的类
	 * @return Image
	 */
	public static Image getImage(String imageUrl, Class<?> c) {
		ImageIcon icon = getImageIcon(imageUrl, c);
		return icon.getImage();
	}

	/**
	 * 得到ImageIcon对象
	 * 
	 * @param imageUrl
	 *            图片所在路径
	 * @param c
	 *            当前操作的类
	 * @return ImageIcon
	 */
	public static ImageIcon getImageIcon(String imageUrl, Class<?> c) {
		return new ImageIcon(c.getClassLoader().getResource(imageUrl));
	}

	/**
	 * 读取指定文件中的每一行内容<br>
	 * 文件内容应该如下：
	 * 
	 * <pre>
	 * c:\a.json
	 * c:\b.json
	 * </pre>
	 * 
	 * @param path
	 *            文件完整路径
	 * @throws IOException
	 *             文件操作异常
	 * @return String[] 每一行内容数组
	 * @see BufferedReader
	 */
	public static String[] getFileLineList(String path) throws IOException {
		ArrayList<String> string = new ArrayList<String>(5);
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		while ((line = br.readLine()) != null) {
			string.add(line);
		}
		br.close();
		return string.toArray(new String[0]);
	}

	/***
	 * 读取指定文件全部内容
	 * 
	 * @param path
	 *            文件完整路径
	 * @throws IOException
	 *             文件操作异常
	 * @return String 文件内容
	 * @see FileInputStream
	 */
	public static String getFileContext(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileInputStream fis = new FileInputStream(path);
		byte[] buf = new byte[512];
		int line = 0;
		while ((line = fis.read(buf)) != -1) {
			sb.append(new String(buf, 0, line));
		}
		buf = null;
		fis.close();
		return sb.toString().trim();
	}

	/**
	 * 字符串处理<br>
	 * 把字符串"ab_cd"处理成AbCD或abCd;
	 * 
	 * @param str
	 *            需要处理的字符串
	 * @param firstUpperCase
	 *            首字符是否大写
	 * @return 返回处理后的字符串：如果 true AbCD,false abCd
	 * @see String
	 */
	public static String strHandle(String str, boolean firstUpperCase) {
		StringBuilder builderr = new StringBuilder();
		boolean flag = false;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '_') {
				flag = true;
				continue;
			} else {
				if (flag) {
					flag = false;
					builderr.append(builderr.length() > 0 ? Character.toUpperCase(ch) : ch);
				} else {
					builderr.append(ch);
				}
			}
		}
		if (firstUpperCase) {
			builderr.setCharAt(0, Character.toUpperCase(builderr.charAt(0)));
		}
		return builderr.toString();
	}

	/***
	 * 字符串首字符大写
	 * 
	 * @param str
	 *            需要转换的字符串
	 * @return String 转换后的字符串
	 * @see Character#toUpperCase(char)
	 */
	public static String firstUpperCase(String str) {
		StringBuilder builder = new StringBuilder(str);
		builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
		return builder.toString();
	}

	public static String createCommentString(JSONObject json, List<String> filedList, String tab) {
		if (Config.getInstant().isSplitGenerate()) {
			tab = Constant.TAB;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(tab).append("/** \n");
		for (int i = 0; i < filedList.size(); i++) {
			String key = filedList.get(i);
			sb.append(tab).append("* ").append(key).append(" : ");
			sb.append(json.get(key).toString().replaceAll("\r", "").replaceAll("\t ", "").replaceAll("\f", ""));
			sb.append("\n");
		}
		sb.append(tab).append("*/ \n");
		return sb.toString();
	}

	/**
	 * 转成驼峰
	 *
	 * @param text
	 * @return
	 */
	public static String captureStringLeaveUnderscore(String text) {
		if (TextUtils.isEmpty(text)) {
			return text;
		}
		String temp = text.replaceAll("^_+", "");

		if (!TextUtils.isEmpty(temp)) {
			text = temp;
		}
		String[] strings = text.split("_");
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(strings[0]);
		for (int i = 1; i < strings.length; i++) {
			stringBuilder.append(Tools.captureName(strings[i]));
		}
		return stringBuilder.toString();
	}

	/** 首字母转成大写 */
	public static String captureName(String text) {
		if (text == null) {
			return text;
		}
		if (text.length() > 1) {
			text = text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			text = text.toUpperCase();
		}
		return text;
	}

	public static String captureFirstToLowerCase(String text) {
		if (text == null) {
			return text;
		}
		if (text.length() > 1) {
			text = text.substring(0, 1).toLowerCase() + text.substring(1);
		} else {
			text = text.toLowerCase();
		}
		return text;
	}

	public static String getPackage(String generateClassName) {
		int index = generateClassName.lastIndexOf(".");
		if (index > 0) {
			return generateClassName.substring(0, index);
		}
		return null;
	}

	public static void log(Exception e) {
		e.printStackTrace();
	}

	public static void printLog(String str) {
		System.out.println(str);
	}
}
