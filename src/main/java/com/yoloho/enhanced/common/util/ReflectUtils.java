package com.yoloho.enhanced.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 反射工具类
 * @author houlf
 */
public class ReflectUtils {

	/**
	 * 设置对象数值
	 * 
	 * @param field
	 * @param obj
	 * @param value
	 */
	public static void setValue(Field field, Object obj, Object value) {
		try {
			field.setAccessible(true);
			if (field.getType() == int.class) {
				field.setInt(obj, (int) value);
			} else if (field.getType() == long.class) {
				field.setLong(obj, (long) value);
			} else if (field.getType() == float.class) {
				field.setFloat(obj, (float) value);
			} else if (field.getType() == double.class) {
				field.setDouble(obj, (double) value);
			} else {
				field.set(obj, value);
			}
		} catch (Exception exp) {
			throw new RuntimeException(exp);
		}
	}

	/**
	 * Java反射获取数值
	 * 
	 * @param field
	 * @param obj
	 * @return
	 */
	public static Object getValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取字符串数值
	 * 
	 * @param field
	 * @param obj
	 * @return
	 */
	public static String getStringValue(Field field, Object obj) {
		try {
			field.setAccessible(true);
			Object value = field.get(obj);
			if (value != null) {
				return String.valueOf(value);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据Annotation获取Field
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> anno) {
		List<Field> listFields = new ArrayList<Field>();
		Set<String> nameSet = new HashSet<String>();
		Class<?> curClazz = clazz;
		while (curClazz != null) {
			Field[] fields = curClazz.getDeclaredFields();
			for (Field curField : fields) {
				if (nameSet.contains(curField.getName())) {
					continue;
				}
				nameSet.add(curField.getName());
				if (curField.getAnnotation(anno) != null) {
					listFields.add(curField);
				}
			}
		}
		return listFields;
	}

}