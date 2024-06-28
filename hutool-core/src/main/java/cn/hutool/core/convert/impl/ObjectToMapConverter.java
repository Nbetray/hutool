package cn.hutool.core.convert.impl;

import cn.hutool.core.convert.AbstractConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ObjectToMapConverter extends AbstractConverter<Map<String, Object>> {

	@Override
	protected Map<String, Object> convertInternal(Object value) {
		if (value == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<>();
		Class<?> clazz = value.getClass();

		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					Object fieldValue = field.get(value);
					if (isPrimitiveOrWrapper(field.getType())) {
						map.put(field.getName(), fieldValue);
					} else {
						map.put(field.getName(), convertInternal(fieldValue));
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			clazz = clazz.getSuperclass();
		}
		return map;
	}

	public <T> T contraConvert(Map<String, Object> map, Class<T> clazz){
		return mapToObject(map, clazz);
	}

	private boolean isPrimitiveOrWrapper(Class<?> type) {
		return type.isPrimitive() ||
			type == Boolean.class ||
			type == Byte.class ||
			type == Character.class ||
			type == Short.class ||
			type == Integer.class ||
			type == Long.class ||
			type == Float.class ||
			type == Double.class ||
			type == String.class;
	}

	private <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
		if (map == null) {
			return null;
		}
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			T obj = constructor.newInstance();
			populateObjectFields(map, obj, clazz);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private <T> void populateObjectFields(Map<String, Object> map, T obj, Class<?> clazz) throws IllegalAccessException {
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object fieldValue = map.get(field.getName());

				if (fieldValue instanceof Map) {
					fieldValue = mapToObject((Map<String, Object>) fieldValue, field.getType());
				}

				field.set(obj, fieldValue);
			}
			clazz = clazz.getSuperclass();
		}
	}
}
