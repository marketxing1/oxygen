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
package vip.justlive.oxygen.web.handler;

import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.convert.DefaultConverterService;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.mapping.DataBinder;
import vip.justlive.oxygen.web.mapping.DataBinder.SCOPE;

/**
 * cookie参数处理
 *
 * @author wubo
 */
public class CookieParamHandler implements ParamHandler {

  @Override
  public boolean supported(DataBinder dataBinder) {
    return dataBinder.getScope() == SCOPE.COOKIE;
  }

  @Override
  public Object handle(DataBinder dataBinder) {
    Request request = Request.current();

    // 简单类型
    DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
    if (converterService.canConverter(String.class, dataBinder.getType())) {
      String value = request.getCookieValue(dataBinder.getName());
      if (value == null) {
        value = dataBinder.getDefaultValue();
      }
      return converterService.convert(value, dataBinder.getType());
    }

    Map<String, Object> map = new HashMap<>(16);
    request.getCookies().forEach((k, v) -> map.put(k, v.getValue()));

    // map
    if (Map.class.isAssignableFrom(dataBinder.getType())) {
      return map;
    }

    // 复杂类型 使用classloader判断非java内置类
    if (dataBinder.getType().getClassLoader() != null) {
      // 简单使用Json转换
      return new JSONObject(map).toJavaObject(dataBinder.getType());
    }
    return null;
  }
}
