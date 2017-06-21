package com.builder.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdesktop.swingx.ux.IJava;

import com.builder.json.config.Config;
import com.builder.json.config.Constant;
import com.builder.json.entity.FileInfo;
import com.builder.json.entity.JavaEntity;

public class DataWriter {
	private JavaEntity javaEntity;
	private FileInfo fileInfo;

	public DataWriter(JavaEntity javaEntity, FileInfo fileInfo) {
		super();
		this.javaEntity = javaEntity;
		this.fileInfo = fileInfo;
	}

	public void execute() {
		createJavaFile(javaEntity);
		try {
			String pagPath = fileInfo.packageName.replace(".", "/");
			String filePath = fileInfo.outPath + File.separatorChar + pagPath;
			File filder = new File(filePath);
			java.awt.Desktop.getDesktop().open(filder);
		} catch (IOException e) {
			GsonToJava.dialog(e.getMessage(), "打开文件夹失败");
		}
	}

	private void createJavaFile(JavaEntity java) {
		StringBuilder sb = new StringBuilder(Config.getInstant().getCopyrightStr());
		sb.append("package ").append(java.getPackName()).append(";\n");
		boolean split = Config.getInstant().isSplitGenerate();
		if (!split) {
			java.mergeImport();
		}
		for (String pag : java.getImport()) {
			sb.append("import ").append(pag).append(";\n");
		}
		sb.append(Config.getInstant().getClassComment()).append("\n");
		sb.append("public class ").append(java.getClassName()).append(" {\n");
		sb.append(java.toSrc("\t")).append("\n");
		if (split) {
			for (IJava javaClass : java.getInnerClasss()) {
				if (!javaClass.isGenerate()) {
					continue;
				}
				JavaEntity classEntity = (JavaEntity) javaClass;
				createJavaFile(classEntity);
			}
		} else {
			sb.append(innerClass(java, "\t"));
		}
		sb.append("}");
		try {
			saveToJavaFile(sb, java.getClassName());
		} catch (IOException e) {
			GsonToJava.dialog(java.getClassName() + "\n" + e.getMessage(), "文件保存失败");
		}
	}

	private StringBuilder innerClass(JavaEntity classEntity, String tab) {
		StringBuilder sb = new StringBuilder();
		String newtab = tab;
		for (IJava javaClass : classEntity.getInnerClasss()) {
			if (!javaClass.isGenerate()) {
				continue;
			}
			JavaEntity javaEntity = (JavaEntity) javaClass;
			sb.append(tab).append("public static class ").append(javaEntity.getClassName()).append(" {\n");
			sb.append(javaEntity.toSrc(newtab + "\t"));
			sb.append(tab).append("}\n\n");
			sb.append(innerClass(javaEntity, newtab));
		}
		return sb;
	}

	private void saveToJavaFile(StringBuilder sb, String generateClassName) throws IOException {
		String pagPath = fileInfo.packageName.replace(".", "/");
		String filePath = fileInfo.outPath + File.separatorChar + pagPath;
		File filder = new File(filePath);
		if (!filder.exists()) {
			filder.mkdirs();
		}
		File file = new File(filePath + File.separatorChar + generateClassName + Constant.JAVA);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(sb.toString().getBytes(Constant.CHARSET));
		fos.flush();
		fos.close();
	}

}
