package com.boot.common.utils;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.boot.common.context.Func;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Yarns
 *	反射工具
 */
public class ReflectUtils {

	private static final Map<String, Set<Class<?>>> CLAZZ_CACHE = new HashMap<>();


	/**
	 * 是否抽象类
	 *
	 * @param clazz 类
	 * @return boolean
	 */
	public static boolean isAbstract(Class<?> clazz) {
		return Modifier.isAbstract(clazz.getModifiers());
	}

	/**
	 * 是否实现类
	 *
	 * @param baseClass 基础类
	 * @param clazz     类
	 * @return boolean
	 */
	public static boolean isImplClass(Class<?> baseClass, Class<?> clazz) {
		return baseClass.isAssignableFrom(clazz) && !clazz.isInterface() && !isAbstract(clazz);
	}

	public static Set<Class<?>> findClasses(String packageName, Func<Boolean, Class<?>> filter) {
		Set<Class<?>> clazzList = findClasses(packageName);
		if (CollectionUtils.isEmpty(clazzList)) {
			return new LinkedHashSet<>();
		}
		if (null == filter) {
			return new LinkedHashSet<>(clazzList);
		}
		Set<Class<?>> result = new LinkedHashSet<>();
		for (Class<?> clazz : clazzList) {
			Boolean invoke = filter.invoke(clazz);
			if (null != invoke && invoke) {
				result.add(clazz);
			}
		}
		return result;
	}

	/**
	 * 根据包名获取包下面所有的类名
	 *
	 * @param packageName 包名
	 * @return set
	 */
	public static Set<Class<?>> findClasses(String packageName) {
		if (null == packageName) {
			packageName = "";
		}
		if (CLAZZ_CACHE.containsKey(packageName)) {
			return CLAZZ_CACHE.get(packageName);
		}
		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<>();
		String packageDirName = packageName.replace('.', '/');
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findClassesInPackageByFile(packageName, filePath, true, classes);
				} else if ("jar".equals(protocol)) {
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection()).getJarFile();
						// 从此jar包得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						findClassesInPackageByJar(packageName, entries, packageDirName, true, classes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		CLAZZ_CACHE.putIfAbsent(packageName, classes);
		return classes;
	}

	/**
	 * 以jar的形式来获取包下的所有Class
	 *
	 * @param packageName    package
	 * @param entries        entries
	 * @param packageDirName dir
	 * @param recursive      recursive
	 * @param classes        classes
	 */
	private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName,
												  final boolean recursive, Set<Class<?>> classes) {
		// 同样的进行循环迭代
		while (entries.hasMoreElements()) {
			// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
			JarEntry entry = entries.nextElement();
			String name = entry.getName();
			// 如果是以/开头的
			if (name.charAt(0) == '/') {
				// 获取后面的字符串
				name = name.substring(1);
			}
			// 如果前半部分和定义的包名相同
			if (name.startsWith(packageDirName)) {
				int idx = name.lastIndexOf('/');
				// 如果以"/"结尾 是一个包
				if (idx != -1) {
					// 获取包名 把"/"替换成"."
					packageName = name.substring(0, idx).replace('/', '.');
				}
				// 如果可以迭代下去 并且是一个包
				if ((idx != -1) || recursive) {
					// 如果是一个.class文件 而且不是目录
					if (name.endsWith(".class") && !entry.isDirectory()) {
						Class<?> clazz = createClass(packageName, name, true);
						if (null != clazz) {
							classes.add(clazz);
						}
					}
				}
			}
		}
	}



	/**
	 * 以文件的形式来获取包下的所有Class
	 *
	 * @param packageName package
	 * @param packagePath path
	 * @param recursive   recursive
	 * @param classes     classes
	 */
	private static void findClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
		if (null == dirFiles) {
			return;
		}
		// 循环所有文件
		for (File file : dirFiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				String dirPackage = file.getName();
				if (StringUtils.isNotEmpty(packageName)) {
					dirPackage = packageName.concat(".").concat(dirPackage);
				}
				findClassesInPackageByFile(dirPackage, file.getAbsolutePath(), recursive, classes);
			} else {
				Class<?> clazz = createClass(packageName, file.getName(), false);
				if (null != clazz) {
					classes.add(clazz);
				}
			}
		}
	}



	private static Class<?> createClass(String packageName, String name, boolean isJar) {
		if (StringUtils.isEmpty(packageName) || StringUtils.isEmpty(name)) {
			return null;
		}
		try {
			String className = packageName.concat(".").concat(FileUtil.mainName(name));
			if (isJar) {
				return Class.forName(className);
			} else {
				return Thread.currentThread().getContextClassLoader().loadClass(className);
			}
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * 获取obj对象fieldName的Field
	 * @param obj
	 * @param fieldName
	 * @return
	 */
	public static Field getFieldByFieldName(Object obj, String fieldName) {
		for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass
				.getSuperclass()) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
			}
		}
		return null;
	}

	/**
	 * 直接调用对象方法, 而忽略修饰符(private, protected)
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	public static Object invokeMethod(Object object, String methodName, Class<?> [] parameterTypes,
									  Object [] parameters) throws InvocationTargetException{
		Method method = getDeclaredMethod(object, methodName, parameterTypes);
		if(method == null){
			throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");
		}
		method.setAccessible(true);
		try {
			return method.invoke(object, parameters);
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
	 * @param object
	 * @param fieldName
	 * @param value
	 */
	public static void setFieldValue(Object object, String fieldName, Object value){
		Field field = getDeclaredField(object, fieldName);
		if (field == null){
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
		}
		makeAccessible(field);
		try {
			field.set(object, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
	 * @param object
	 * @param fieldName
	 * @return
	 */
	public static Object getFieldValue(Object object, String fieldName){
		Field field = getDeclaredField(object, fieldName);
		if (field == null){
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
		}
		makeAccessible(field);
		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 拷贝对象属性（不包含值为空的属性）
	 * @param dest
	 * @param source
	 * @throws Exception
	 */
	public static void copyProperties(Object dest, Object source){
		if(dest == null || source == null){
			return;
		}
		for (Field f : source.getClass().getDeclaredFields()) {
			if(! Modifier.isFinal(f.getModifiers())){
				Object value = ReflectUtils.getFieldValue(source, f.getName());
				if(value != null && ReflectUtils.getDeclaredField(dest, f.getName()) != null){
					ReflectUtils.setFieldValue(dest, f.getName(), value);
				}
			}
		}
	}

	/**
	 * 获取一个类的所有属性
	 * @param beanClass
	 * @return
	 */
	public static PropertyDescriptor[] getProperties(Class<?> beanClass) {
		HashMap<String , PropertyDescriptor> propertyMap  = new HashMap<String , PropertyDescriptor>();
		addProperties(beanClass, propertyMap);
		PropertyDescriptor[] array = new PropertyDescriptor[propertyMap.size()];
		propertyMap.values().toArray(array);
		return array;
	}


	private static void addProperties(Class<?> beanClass, HashMap<String , PropertyDescriptor> propertyMap){
		for (Method method : beanClass.getMethods()) {
			String methodName = method.getName();
			if(!methodName.startsWith("get") && !methodName.startsWith("set")){
				continue;
			}
			String upperCaseName = methodName.substring(3);
			if("Class".equals(upperCaseName)){
				continue;
			}
			char[] chars  = upperCaseName.toCharArray();
			chars[0] = Character.toLowerCase(chars[0]);
			String propertyName = new String(chars);
			try {
				PropertyDescriptor property = propertyMap.get(propertyName);
				if(property == null){
					property = new PropertyDescriptor(propertyName , beanClass , null , null);
					propertyMap.put(propertyName, property);
				}
				if(methodName.startsWith("get")){
					if(property.getReadMethod() == null){
						property.setReadMethod(method);
					}
				}else{
					if(property.getWriteMethod() == null){
						property.setWriteMethod(method);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		Class<?> superclass = beanClass.getSuperclass();
		if(superclass != null && superclass != Object.class){
			addProperties(superclass, propertyMap);
		}
	}

	/**
	 * 根据属性获取属性
	 * @param beanClass
	 * @param name
	 * @return
	 */
	public static Field findField(Class<?> beanClass, String name) {
		HashMap<String , Field> fieldMap  = new HashMap<String , Field>();
		addFields(beanClass, fieldMap);
		return fieldMap.get(name);
	}



	/**
	 * 获取包装类型
	 * @param type
	 * @return
	 */
	public static Class<?> getBoxedType(Class<?> type){
		if(type == char.class){
			return Character.class;
		}
		if(type == boolean.class){
			return Boolean.class;
		}
		if(type == double.class){
			return Double.class;
		}
		if(type == long.class){
			return Long.class;
		}
		if(type == int.class){
			return Integer.class;
		}
		if(type == short.class){
			return Short.class;
		}
		if(type == byte.class){
			return Byte.class;
		}
		return type;
	}

	/**
	 * 获取拆箱类型
	 * @param type
	 * @return
	 */
	public static Class<?> getUnboxedType(Class<?> type){
		if(type == Character.class){
			return char.class;
		}
		if(type == Boolean.class){
			return boolean.class;
		}
		if(type == Double.class){
			return double.class;
		}
		if(type == Long.class){
			return long.class;
		}
		if(type == Integer.class){
			return int.class;
		}
		if(type == Short.class){
			return short.class;
		}
		if(type == Byte.class){
			return byte.class;
		}
		return type;
	}

	/**
	 * 判断一个类是否是否复杂类型
	 * @param type
	 * @return
	 */
	public static boolean isComplexType(Class<?> type) {
		if (type.isPrimitive()) {
			return false;
		}
		if(getUnboxedType(type).isPrimitive()){
			return false;
		}
		if (type == String.class) {
			return false;
		}
		if (type == Date.class) {
			return false;
		}
		if (type.isArray()) {
			return false;
		}
		return !type.isEnum();
	}

	/**
	 * 判断一个属性是否是否复杂类型
	 * @param field
	 * @return
	 */
	public static boolean isComplexType(Field field) {
		if(field.getType() == String.class){
			return false;
		}
		if(field.getType() == Long.class){
			return false;
		}
		if(field.getType() == Date.class){
			return false;
		}
		if(field.getType() == Integer.class){
			return false;
		}
		if(field.getType() == Double.class){
			return false;
		}
		if(field.getType() == Float.class){
			return false;
		}
		if(field.getType() == Short.class){
			return false;
		}
		if(field.getType() == BigDecimal.class){
			return false;
		}
		if(field.getType() == Boolean.class){
			return false;
		}
		if(field.getType().isArray()){
			return false;
		}
		return !field.getType().isEnum();
	}

	/**
	 * 讲一个对象转成字符串
	 * @param object
	 * @return
	 */
	public static String toString(Object object) {
		if(!isComplexType(object.getClass())){
			return object.toString();
		}
		StringBuilder buffer = new StringBuilder();
		BeanWrapperImpl wrapper = new BeanWrapperImpl(object);
		for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()){
			if("getClass".equals(pd.getReadMethod().getName())){
				continue;
			}
			Object propertyValue = wrapper.getPropertyValue(pd.getName());
			if(propertyValue == null){
				continue;
			}
			buffer.append(toString(propertyValue));
		}
		return buffer.toString();
	}

	/**
	 * 获取类申明的所有属性
	 * @param beanClass
	 * @return
	 */
	public static Field[] getAllDeclaredFields(Class<?> beanClass){
		HashMap<String , Field> fieldMap  = new HashMap<String , Field>();
		addFields(beanClass, fieldMap);
		Field[] array = new Field[fieldMap.size()];
		fieldMap.values().toArray(array);
		return array;
	}

	private static void addFields(Class<?> beanClass, HashMap<String , Field> fieldMap){
		for (Field field : beanClass.getDeclaredFields()) {
			if(!fieldMap.containsKey(field.getName())){
				fieldMap.put(field.getName() , field);
			}
		}
		Class<?> superclass = beanClass.getSuperclass();
		if(superclass != null && superclass != Object.class){
			addFields(superclass, fieldMap);
		}
	}

	/**
	 * 获取obj对象fieldName的属性值
	 * @param obj
	 * @param fieldName
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getValueByFieldName(Object obj, String fieldName)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field = getFieldByFieldName(obj, fieldName);
		Object value = null;
		if(field!=null){
			if (field.isAccessible()) {
				value = field.get(obj);
			} else {
				field.setAccessible(true);
				value = field.get(obj);
				field.setAccessible(false);
			}
		}
		return value;
	}

	/**
	 * 设置obj对象fieldName的属性值
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setValueByFieldName(Object obj, String fieldName,
			Object value) throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field field = obj.getClass().getDeclaredField(fieldName);
		if (field.isAccessible()) {
			field.set(obj, value);
		} else {
			field.setAccessible(true);
			field.set(obj, value);
			field.setAccessible(false);
		}
	}

	/**
	 * 判断一个类是否实现了某个接口
	 * @param target
	 * @param interfaceClass
	 * @return
	 */
	public static boolean isImplement(Class<?> target, Class<?> interfaceClass){
		for (Class<?> i : target.getInterfaces()){
			if(i == interfaceClass){
				return true;
			}
			if(i.getInterfaces().length > 0){
				isImplement(i, interfaceClass);
			}
		}
		Class<?> superclass = target.getSuperclass();
		if(superclass != null){
			return isImplement(superclass, interfaceClass);
		}
		return false;
	}


	/**
	 * 将反射时的 "检查异常" 转换为 "运行时异常"
	 * @return
	 */
	public static IllegalArgumentException convertToUncheckedException(Exception ex){
		if(ex instanceof IllegalAccessException || ex instanceof IllegalArgumentException
				|| ex instanceof NoSuchMethodException){
			throw new IllegalArgumentException("反射异常", ex);
		}else{
			throw new IllegalArgumentException(ex);
		}
	}


	/**
	 * 通过反射, 获得定义 Class 时声明的父类的泛型参数的类型
	 * 如: public EmployeeDao extends BaseDao<Employee, String>
	 * @param clazz
	 * @param index
	 * @return
	 */
	public static Class<?> getSuperClassGenricType(Class<?> clazz, int index){
		Type genType = clazz.getGenericSuperclass();
		if(!(genType instanceof ParameterizedType)){
			return Object.class;
		}
		Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
		if(index >= params.length || index < 0){
			return Object.class;
		}
		if(!(params[index] instanceof Class)){
			return Object.class;
		}
		return (Class<?>) params[index];
	}

	/**
	 * 循环向上转型, 获取对象的 DeclaredMethod
	 * @param object
	 * @param methodName
	 * @param parameterTypes
	 * @return
	 */
	public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes){
		for(Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()){
			try {
				return superClass.getDeclaredMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				//Method 不在当前类定义, 继续向上转型
			}
		}
		return null;
	}

	/**
	 * 使 私有的Field变为可访问
	 * @param field
	 */
	public static void makeAccessible(Field field){
		if(!Modifier.isPublic(field.getModifiers())){
			field.setAccessible(true);
		}
	}

	/**
	 * 循环向上转型, 获取对象的 DeclaredField
	 * @param object
	 * @param filedName
	 * @return
	 */
	public static Field getDeclaredField(Object object, String filedName){
		for(Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()){
			try {
				return superClass.getDeclaredField(filedName);
			} catch (NoSuchFieldException e) {
				//Field 不在当前类定义, 继续向上转型
			}
		}
		return null;
	}
}
