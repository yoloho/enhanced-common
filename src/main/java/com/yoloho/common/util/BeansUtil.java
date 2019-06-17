package com.yoloho.common.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * bean与map转化的util
 * 
 * @author wuzl
 * 
 */
public class BeansUtil {
    private static Logger logger = LoggerFactory.getLogger(BeansUtil.class.getSimpleName());
    private static List<Class<?>> primitiveClass = Lists.<Class<?>>newArrayList(byte.class, short.class, int.class, long.class,
            float.class, double.class);
    private static List<Class<?>> nonePrimitiveClass = Lists.<Class<?>>newArrayList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class);
	/**
	 * 复制bean
	 * 
	 * @param fromObj
	 * @param toObj
	 */
	public static void copyBean(Object fromObj, Object toObj) {
		/* 1.如果bean是null直接返回null */
		if (fromObj == null) {
			return;
		}
		try {
			/* 2.同过内省获取bean信息 */
			BeanInfo beanInfo = Introspector.getBeanInfo(fromObj.getClass());
			/* 3.获取bean属性的描述器 */
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			/* 4.遍历bean的属性 */
			for (PropertyDescriptor property : propertyDescriptors) {
				/* 4.1 获取属性名称 */
				String key = property.getName();
				/* 4.2 过滤class方法 */
				if (!key.equals("class")) {
					/* 4.2.1 获取get方法 */
					Method getter = property.getReadMethod();
					/* 4.2.2 获取值 */
					Object value = getter.invoke(fromObj);
					Method write = property.getWriteMethod();
					Method toObjSetMethod = null;
					try {
						toObjSetMethod = toObj.getClass().getMethod(
								write.getName(), write.getParameterTypes());
					} catch (NoSuchMethodException ex) {
					}
					//增加对于Long->long一类的处理
					if (toObjSetMethod == null) {
					    int idx = primitiveClass.indexOf(property.getPropertyType());
					    if (idx >= 0) {
					        //try Long-like class
					        try {
		                        toObjSetMethod = toObj.getClass().getMethod(
		                                write.getName(), nonePrimitiveClass.get(idx));
		                    } catch (NoSuchMethodException ex) {
		                    }
					    }
					}
					if (toObjSetMethod == null) {
                        int idx = nonePrimitiveClass.indexOf(property.getPropertyType());
                        if (idx >= 0) {
                            //try Long-like class
                            try {
                                toObjSetMethod = toObj.getClass().getMethod(
                                        write.getName(), primitiveClass.get(idx));
                            } catch (NoSuchMethodException ex) {
                            }
                        }
                    }
					if (toObjSetMethod != null) {
                        toObjSetMethod.invoke(toObj, value);
                    } else {
                        logger.debug("bean[{}]复制时出错,方法不存在忽略", write.getName());
                    }
				}

			}
		} catch (Exception e) {
			throw new RuntimeException("Copy bean from [" + fromObj.getClass().getName() + "] error: " + e.getMessage());
		}
	}

	/**
	 * Read specific property from bean
	 * 
	 * @param bean
	 * @param field
	 * @param clazz null value without checking the type
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static final <T> T getValueFromBean(Object bean, String field,
			Class<?> clazz, boolean checkType) {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			String classTarget = clazz.getSimpleName();
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();
				if (key.equals(field)) {
					Method getter = property.getReadMethod();
					Object value = getter.invoke(bean);
					Class<?> fieldClass = value.getClass();
					if (classTarget != null && !fieldClass.equals(clazz)) {
					    throw new RuntimeException(String.format("Type mismatch: %s is expected to %s but %s", 
                                field, classTarget, fieldClass.getName()));
					}
                    return (T)value;
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(String.format("Reading property %s from %s error: %s", field, bean
					.getClass().getName(), e.getMessage()));
		}
	}

}
