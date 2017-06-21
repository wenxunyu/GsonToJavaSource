package com.builder.json.entity;

public class FileInfo {
	public String outPath;
	public String packageName;
	public String generateClassName;

	public FileInfo(String outPath, String packageName, String generateClassName) {
		this.outPath = outPath;
		this.packageName = packageName;
		this.generateClassName = generateClassName;
	}

}
