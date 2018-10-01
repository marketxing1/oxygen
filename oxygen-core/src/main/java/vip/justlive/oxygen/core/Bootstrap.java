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
package vip.justlive.oxygen.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * 引导类
 * <br>
 * 用于加载程序启动需要的配置
 *
 * @author wubo
 */
@Slf4j
public final class Bootstrap {

  private static final List<Plugin> PLUGINS = new ArrayList<>(5);
  private static final AtomicBoolean STATE = new AtomicBoolean(false);
  private static final Thread SHUTDOWN_HOOK = new Thread(Bootstrap::doClose);

  Bootstrap() {
  }

  /**
   * 初始化配置
   *
   * @param locations 配置文件路径
   */
  public static void initConfig(String... locations) {
    ConfigFactory.loadProperties(locations);
    String overridePath = ConfigFactory.getProperty(Constants.CONFIG_OVERRIDE_PATH_KEY);
    if (overridePath != null) {
      ConfigFactory.loadProperties(overridePath.split(Constants.COMMA));
    }
  }

  /**
   * 添加自定义插件
   *
   * @param plugins 插件
   */
  public static void addCustomPlugin(Plugin... plugins) {
    if (STATE.get()) {
      throw new IllegalStateException("Bootstrap已启动");
    }
    PLUGINS.addAll(Arrays.asList(plugins));
  }

  /**
   * 启动Bootstrap
   */
  public static void start() {
    if (STATE.compareAndSet(false, true)) {
      log.info("starting bootstrap ...");
      initConfig();
      addSystemPlugin();
      initPlugins();
      registerShutdownHook();
      log.info("bootstrap has started ! have fun");
    }
  }

  /**
   * 关闭Bootstrap
   */
  public static synchronized void close() {
    doClose();
    Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 初始化配置
   * <br>
   * 使用默认地址进行加载，然后使用覆盖路径再次加载
   */
  private static void initConfig() {
    initConfig(Constants.CONFIG_PATHS);
  }

  /**
   * 添加系统插件类
   */
  private static void addSystemPlugin() {
    ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
    Iterator<Plugin> it = loader.iterator();
    while (it.hasNext()) {
      PLUGINS.add(it.next());
    }
  }

  /**
   * 初始化插件
   */
  private static void initPlugins() {
    Collections.sort(PLUGINS);
    for (Plugin plugin : PLUGINS) {
      plugin.start();
    }
  }

  /**
   * 注册shutdown钩子
   */
  private static synchronized void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
  }

  /**
   * 关闭Bootstrap
   */
  private static void doClose() {
    log.info("closing bootstrap ...");
    for (Plugin plugin : PLUGINS) {
      plugin.stop();
    }
    log.info("bootstrap closed ! bye bye");
  }
}
