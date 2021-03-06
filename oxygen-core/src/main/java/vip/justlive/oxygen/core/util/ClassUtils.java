/*
 * Copyright (C) 2018 justlive1
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.util;


import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * class工具
 *
 * @author wubo
 */
public class ClassUtils {

  ClassUtils() {
  }

  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE;
  private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE;

  static {
    Map<Class<?>, Class<?>> primToWrap = new HashMap<>(16);
    Map<Class<?>, Class<?>> wrapToPrim = new HashMap<>(16);

    add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
    add(primToWrap, wrapToPrim, byte.class, Byte.class);
    add(primToWrap, wrapToPrim, char.class, Character.class);
    add(primToWrap, wrapToPrim, double.class, Double.class);
    add(primToWrap, wrapToPrim, float.class, Float.class);
    add(primToWrap, wrapToPrim, int.class, Integer.class);
    add(primToWrap, wrapToPrim, long.class, Long.class);
    add(primToWrap, wrapToPrim, short.class, Short.class);
    add(primToWrap, wrapToPrim, void.class, Void.class);

    PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
    WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
  }

  private static void add(
      Map<Class<?>, Class<?>> forward,
      Map<Class<?>, Class<?>> backward,
      Class<?> key,
      Class<?> value) {
    forward.put(key, value);
    backward.put(value, key);
  }

  /**
   * 所有基本类型
   *
   * @return 基本类型集合
   */
  public static Set<Class<?>> allPrimitiveTypes() {
    return PRIMITIVE_TO_WRAPPER_TYPE.keySet();
  }

  /**
   * 基本类型包装类
   *
   * @return 基本类型包装类集合
   */
  public static Set<Class<?>> allWrapperTypes() {
    return WRAPPER_TO_PRIMITIVE_TYPE.keySet();
  }

  /**
   * 获取包装类型
   *
   * @param type 类型
   * @param <T> 泛型
   * @return 包装类
   */
  public static <T> Class<T> wrap(Class<T> type) {
    Checks.notNull(type);
    // cast is safe: long.class and Long.class are both of type Class<Long>
    @SuppressWarnings("unchecked")
    Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER_TYPE.get(type);
    return (wrapped == null) ? type : wrapped;
  }

  /**
   * 获取包装类的基本类型
   *
   * @param type 类型
   * @param <T> 泛型
   * @return 基本类型
   */
  public static <T> Class<T> unwrap(Class<T> type) {
    Checks.notNull(type);
    // cast is safe: long.class and Long.class are both of type Class<Long>
    @SuppressWarnings("unchecked")
    Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE_TYPE.get(type);
    return (unwrapped == null) ? type : unwrapped;
  }

  /**
   * 是否为基本类型或包装类
   *
   * @param type 类型
   * @return true则为基本类型或包装类
   */
  public static boolean isPrimitive(Class<?> type) {
    return PRIMITIVE_TO_WRAPPER_TYPE.containsKey(type) || WRAPPER_TO_PRIMITIVE_TYPE
        .containsKey(type);
  }

  /**
   * 获取默认类加载器
   *
   * @return classloader
   */
  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Exception ex) {
      // 线程上下文加载不了
    }
    if (cl == null) {
      // 使用类加载器
      cl = ClassUtils.class.getClassLoader();
      if (cl == null) {
        // 获取系统类加载器
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (Exception ex) {
          // ...
        }
      }
    }
    return cl;
  }

  /**
   * 获取Class实例
   *
   * @param name 类名
   * @return class
   */
  public static Class<?> forName(String name) {
    return forName(name, getDefaultClassLoader());
  }

  /**
   * 获取Class实例
   *
   * @param name 类名
   * @param classLoader 类加载器
   * @return class
   */
  public static Class<?> forName(String name, ClassLoader classLoader) {
    // "java.lang.String[]" style arrays
    if (name.endsWith(Constants.ARRAY_SUFFIX)) {
      String elementClassName = name.substring(0, name.length() - Constants.ARRAY_SUFFIX.length());
      Class<?> elementClass = forName(elementClassName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[Ljava.lang.String;" style arrays
    if (name.startsWith(Constants.NON_PRIMITIVE_ARRAY_PREFIX) && name
        .endsWith(Constants.SEMICOLON)) {
      String elementName = name
          .substring(Constants.NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    // "[[I" or "[[Ljava.lang.String;" style arrays
    if (name.startsWith(Constants.INTERNAL_ARRAY_PREFIX)) {
      String elementName = name.substring(Constants.INTERNAL_ARRAY_PREFIX.length());
      Class<?> elementClass = forName(elementName, classLoader);
      return Array.newInstance(elementClass, 0).getClass();
    }

    ClassLoader clToUse = classLoader;
    if (clToUse == null) {
      clToUse = getDefaultClassLoader();
    }
    try {
      return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
    } catch (ClassNotFoundException ex) {
      int lastDotIndex = name.lastIndexOf(Constants.DOT);
      if (lastDotIndex != -1) {
        String innerClassName =
            name.substring(0, lastDotIndex) + Constants.INNER_CLASS_SEPARATOR + name
                .substring(lastDotIndex + 1);
        try {
          return (clToUse != null ? clToUse.loadClass(innerClassName)
              : Class.forName(innerClassName));
        } catch (ClassNotFoundException ex2) {
          // Swallow - let original exception get through
        }
      }
      throw Exceptions.wrap(ex);
    }
  }

  /**
   * 类是否存在
   *
   * @param className 类名
   * @return true为存在
   */
  public static boolean isPresent(String className) {
    return isPresent(className, getDefaultClassLoader());
  }

  /**
   * 类是否存在
   *
   * @param className 类名
   * @param classLoader 类加载器
   * @return true为存在
   */
  public static boolean isPresent(String className, ClassLoader classLoader) {
    try {
      forName(className, classLoader);
      return true;
    } catch (Exception ex) {
      // class的依赖不存在.
      return false;
    }
  }

  /**
   * 验证类是否是cglib代理对象
   *
   * @param object the object to check
   * @return true为是代理对象
   */
  public static boolean isCglibProxy(Object object) {
    return isCglibProxyClass(object.getClass());
  }

  /**
   * 验证类是否是cglib生成类
   *
   * @param clazz the class to check
   * @return true为是代理类
   */
  public static boolean isCglibProxyClass(Class<?> clazz) {
    return (clazz != null && isCglibProxyClassName(clazz.getName()));
  }

  /**
   * 验证类名是否为cglib代理类名
   *
   * @param className the class name to check
   * @return true为是代理类
   */
  public static boolean isCglibProxyClassName(String className) {
    return (className != null && className.contains(Constants.CGLIB_CLASS_SEPARATOR));
  }

  /**
   * 获取cglib代理的真实类
   *
   * @param clazz 代理类
   * @return 类
   */
  public static Class<?> getCglibActualClass(Class<?> clazz) {
    Class<?> actualClass = clazz;
    while (isCglibProxyClass(actualClass)) {
      actualClass = actualClass.getSuperclass();
    }
    return actualClass;
  }

}
