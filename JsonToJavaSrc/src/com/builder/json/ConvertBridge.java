package com.builder.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdesktop.swingx.ux.IField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.builder.json.config.Config;
import com.builder.json.entity.FileInfo;
import com.builder.json.entity.IterableFieldEntity;
import com.builder.json.entity.JavaEntity;
import com.builder.json.entity.JavaFieldEntity;
import com.builder.json.utils.CheckUtil;
import com.builder.json.utils.DataType;
import com.builder.json.utils.TextUtils;
import com.builder.json.utils.Tools;

public class ConvertBridge {
	private HashMap<String, JavaEntity> declareClass;
	private HashMap<String, JavaFieldEntity> declareFields;
	private String jsonStr;
	private FileInfo fileInfo;
	private JavaEntity generateClassEntity;

	public ConvertBridge(String jsonStr, String outPath, String packageName, String generateClassName) {
		this.jsonStr = jsonStr;
		this.fileInfo = new FileInfo(outPath, packageName, generateClassName);
		declareClass = new HashMap<String, JavaEntity>();
		declareFields = new HashMap<String, JavaFieldEntity>();
		generateClassEntity = new JavaEntity();
		generateClassEntity.setClassName(generateClassName);
		generateClassEntity.setPackName(packageName);
	}

	public void run() {
		System.out.println("ConvertBridge.run()");
		JSONObject json = null;
		try {
			json = parseJSONObject(jsonStr);
		} catch (Exception e) {
			String jsonTS = removeComment(jsonStr);
			jsonTS = jsonTS.replaceAll("^.*?\\{", "{");
			try {
				json = parseJSONObject(jsonTS);
			} catch (Exception exception) {
				GsonToJava.dialog(exception.getMessage(), "Json格式有误");
				return;
			}
		}
		if (json != null) {
			parseJson(json);
		} else {
			GsonToJava.dialog("请检查Json数据是否正确", "Json格式有误");
		}
	}

	private void parseJson(JSONObject json) {
		List<String> generateFiled = collectGenerateFiled(json);
		if (Config.getInstant().isVirgoMode()) {
			handleVirgoMode(json, generateFiled, generateClassEntity);
		} else {
			handleNormal(json, generateFiled, generateClassEntity);
		}
		CheckUtil.getInstant().cleanDeclareData();
	}

	private void handleVirgoMode(JSONObject json, List<String> generateFiled, JavaEntity parentClass) {
		generateClassEntity.addAllFields(createFields(json, generateFiled, parentClass, "\t"));
		new FieldsUi(generateClassEntity, fileInfo).setVisible(true);
	}

	private void handleNormal(JSONObject json, List<String> generateFiled, JavaEntity parentClass) {
		generateClassEntity.addAllFields(createFields(json, generateFiled, parentClass, "\t"));
		new DataWriter(generateClassEntity, fileInfo).execute();
		// WriteCommandAction.runWriteCommandAction(project, new Runnable() {
		// @Override
		// public void run() {
		// if (targetClass == null) {
		// try {
		// targetClass = PsiClassUtil.getPsiClass(file, project,
		// generateClassName);
		// } catch (Throwable throwable) {
		// handlePathError(throwable);
		// }
		// }
		// if (targetClass != null) {
		// generateClassEntity.setPsiClass(targetClass);
		// try {
		// generateClassEntity.addAllFields(createFields(json, generateFiled,
		// generateClassEntity));
		// operator.setVisible(false);
		// DataWriter dataWriter = new DataWriter(file, project, targetClass);
		// dataWriter.execute(generateClassEntity);
		// Config.getInstant().saveCurrentPackPath(packageName);
		// operator.dispose();
		// } catch (Exception e) {
		// throw e;
		// }
		// }
		// }
		// });
	}

	private List<IField> createFields(JSONObject json, List<String> fieldList, JavaEntity parentClass, String tab) {

		List<IField> fieldEntityList = new ArrayList<IField>();
		List<String> listEntityList = new ArrayList<String>();
		// 是否写入注释
		boolean writeExtra = Config.getInstant().isGenerateComments();

		for (int i = 0; i < fieldList.size(); i++) {
			String key = fieldList.get(i);
			Object value = json.get(key);
			if (value instanceof JSONArray) {
				listEntityList.add(key);
				continue;
			}
			JavaFieldEntity fieldEntity = createField(parentClass, key, value);
			fieldEntityList.add(fieldEntity);
			if (writeExtra) {
				writeExtra = false;
				parentClass.setExtra(Tools.createCommentString(json, fieldList, tab));
			}
		}

		for (int i = 0; i < listEntityList.size(); i++) {
			String key = listEntityList.get(i);
			Object type = json.get(key);
			JavaFieldEntity fieldEntity = createField(parentClass, key, type);
			fieldEntityList.add(fieldEntity);
		}

		return fieldEntityList;
	}

	private List<String> collectGenerateFiled(JSONObject json) {
		Set<String> keySet = json.keySet();
		List<String> fieldList = new ArrayList<String>();
		for (String key : keySet) {
			if (!existDeclareField(key, json)) {
				fieldList.add(key);
			}
		}
		return fieldList;
	}

	private JavaFieldEntity createField(JavaEntity parentClass, String key, Object type) {
		// 过滤 不符合规则的key
		String fieldName = CheckUtil.getInstant().handleArg(key);
		if (Config.getInstant().isUseSerializedName()) {
			fieldName = Tools.captureStringLeaveUnderscore(convertSerializedName(fieldName));
		}
		fieldName = handleDeclareFieldName(fieldName, "");

		JavaFieldEntity fieldEntity = typeByValue(parentClass, key, type);
		fieldEntity.setFieldName(fieldName);
		return fieldEntity;
	}

	private String convertSerializedName(String fieldName) {
		if (Config.getInstant().isUseFieldNamePrefix()
				&& !TextUtils.isEmpty(Config.getInstant().getFiledNamePreFixStr())) {
			fieldName = Config.getInstant().getFiledNamePreFixStr() + "_" + fieldName;
		}
		return fieldName;
	}

	private JavaFieldEntity typeByValue(JavaEntity parentClass, String key, Object type) {
		JavaFieldEntity result;
		if (type instanceof JSONObject) {
			JavaEntity classEntity = existDeclareClass((JSONObject) type);
			if (classEntity == null) {
				JavaFieldEntity fieldEntity = new JavaFieldEntity();
				String subClassName = createSubClassName(key, type);
				JavaEntity innerClassEntity = createInnerClass(subClassName, (JSONObject) type, parentClass);
				fieldEntity.setKey(key);
				fieldEntity.setType(innerClassEntity.getClassName());
				fieldEntity.setTargetClass(innerClassEntity);
				result = fieldEntity;
			} else {
				JavaFieldEntity fieldEntity = new JavaFieldEntity();
				fieldEntity.setKey(key);
				fieldEntity.setType(classEntity.getClassName());
				fieldEntity.setTargetClass(classEntity);
				result = fieldEntity;
			}
		} else if (type instanceof JSONArray) {
			result = handleJSONArray(parentClass, (JSONArray) type, key, 1);
		} else {
			JavaFieldEntity fieldEntity = new JavaFieldEntity();
			fieldEntity.setKey(key);
			fieldEntity.setType(DataType.typeOfObject(type).getValue());
			result = fieldEntity;
			if (type != null) {
				result.setValue(type.toString());
			}
		}
		result.setKey(key);
		return result;
	}

	private JavaFieldEntity handleJSONArray(JavaEntity parentClass, JSONArray jsonArray, String key, int deep) {

		JavaFieldEntity fieldEntity;
		if (jsonArray.length() > 0) {
			Object item = jsonArray.get(0);
			if (item instanceof JSONObject) {
				item = getJsonObject(jsonArray);
			}
			fieldEntity = listTypeByValue(parentClass, key, item, deep);
		} else {
			fieldEntity = new IterableFieldEntity();
			fieldEntity.setKey(key);
			fieldEntity.setType("?");
			((IterableFieldEntity) fieldEntity).setDeep(deep);
		}
		return fieldEntity;
	}

	private JavaEntity existDeclareClass(JSONObject jsonObject) {
		for (JavaEntity classEntity : declareClass.values()) {
			Iterator<String> keys = jsonObject.keys();
			boolean had = false;
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = jsonObject.get(key);
				had = false;
				for (IField fieldEntity : classEntity.getFields()) {
					if (fieldEntity.getKey().equals(key) && DataType.isSameDataType(
							DataType.typeOfString(fieldEntity.getType()), DataType.typeOfObject(value))) {
						had = true;
						break;
					}
				}
				if (!had) {
					break;
				}
			}
			if (had) {
				return classEntity;
			}
		}
		return null;
	}

	private boolean existDeclareField(String key, JSONObject json) {
		JavaFieldEntity fieldEntity = declareFields.get(key);
		if (fieldEntity == null) {
			return false;
		}
		return fieldEntity.isSameType(json.get(key));
	}

	private JSONObject parseJSONObject(String jsonStr) throws JSONException {
		if (jsonStr.startsWith("{")) {
			return new JSONObject(jsonStr);
		} else if (jsonStr.startsWith("[")) {
			JSONArray jsonArray = new JSONArray(jsonStr);
			if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
				return getJsonObject(jsonArray);
			}
		}
		return null;
	}

	private JSONObject getJsonObject(JSONArray jsonArray) {
		JSONObject resultJSON = jsonArray.getJSONObject(0);
		for (int i = 1; i < jsonArray.length(); i++) {
			Object value = jsonArray.get(i);
			if (!(value instanceof JSONObject)) {
				break;
			}
			JSONObject json = (JSONObject) value;
			for (String key : json.keySet()) {
				if (!resultJSON.keySet().contains(key)) {
					resultJSON.put(key, json.get(key));
				}
			}
		}
		return resultJSON;
	}

	/**
	 * @param className
	 * @param json
	 * @param parentClass
	 * @return
	 */
	private JavaEntity createInnerClass(String className, JSONObject json, JavaEntity parentClass) {
		JavaEntity subClassEntity = new JavaEntity();
		Set<String> set = json.keySet();
		List<String> list = new ArrayList<String>(set);
		List<IField> fields = createFields(json, list, subClassEntity, "\t\t");
		subClassEntity.addAllFields(fields);

		if (Config.getInstant().isSplitGenerate()) {
			subClassEntity.setPackName(fileInfo.packageName);
		} else {
			subClassEntity.setPackName(parentClass.getQualifiedName());
		}
		subClassEntity.setClassName(className);
		if (handleDeclareClassName(subClassEntity, "")) {
			CheckUtil.getInstant().addDeclareClassName(subClassEntity.getQualifiedName());
		}
		if (Config.getInstant().isReuseEntity()) {
			declareClass.put(subClassEntity.getQualifiedName(), subClassEntity);
		}
		parentClass.addInnerClass(subClassEntity);
		return subClassEntity;
	}

	private String createSubClassName(String key, Object o) {
		String name = "";
		if (o instanceof JSONObject) {
			if (TextUtils.isEmpty(key)) {
				return key;
			}
			String[] strings = key.split("_");
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < strings.length; i++) {
				stringBuilder.append(Tools.captureName(strings[i]));
			}
			name = stringBuilder.toString() + Config.getInstant().getSuffixStr();
		}
		return name;

	}

	private boolean handleDeclareClassName(JavaEntity classEntity, String appendName) {
		classEntity.setClassName(classEntity.getClassName() + appendName);
		if (CheckUtil.getInstant().containsDeclareClassName(classEntity.getQualifiedName())) {
			return handleDeclareClassName(classEntity, "X");
		}
		return true;
	}

	private String handleDeclareFieldName(String fieldName, String appendName) {
		fieldName += appendName;
		if (CheckUtil.getInstant().containsDeclareFieldName(fieldName)) {
			return handleDeclareFieldName(fieldName, "X");
		}
		return fieldName;
	}

	private JavaFieldEntity listTypeByValue(JavaEntity parentClass, String key, Object type, int deep) {

		JavaFieldEntity item = null;
		if (type instanceof JSONObject) {
			JavaEntity classEntity = existDeclareClass((JSONObject) type);
			if (classEntity == null) {
				IterableFieldEntity iterableFieldEntity = new IterableFieldEntity();
				JavaEntity innerClassEntity = createInnerClass(createSubClassName(key, type), (JSONObject) type,
						parentClass);
				iterableFieldEntity.setKey(key);
				iterableFieldEntity.setDeep(deep);
				iterableFieldEntity.setTargetClass(innerClassEntity);
				item = iterableFieldEntity;
			} else {
				IterableFieldEntity fieldEntity = new IterableFieldEntity();
				fieldEntity.setKey(key);
				fieldEntity.setTargetClass(classEntity);
				fieldEntity.setType(classEntity.getQualifiedName());
				fieldEntity.setDeep(deep);
				item = fieldEntity;
			}

		} else if (type instanceof JSONArray) {
			JavaFieldEntity fieldEntity = handleJSONArray(parentClass, (JSONArray) type, key, ++deep);
			fieldEntity.setKey(key);
			item = fieldEntity;
		} else {
			IterableFieldEntity fieldEntity = new IterableFieldEntity();
			fieldEntity.setKey(key);
			fieldEntity.setType(type.getClass().getSimpleName());
			fieldEntity.setDeep(deep);
			item = fieldEntity;
		}
		return item;
	}

	/**
	 * 过滤掉// 和/** 注释
	 *
	 * @param str
	 * @return
	 */
	public String removeComment(String str) {
		String temp = str.replaceAll("/\\*" + "[\\S\\s]*?" + "\\*/", "");
		return temp.replaceAll("//[\\S\\s]*?\n", "");
	}
}
