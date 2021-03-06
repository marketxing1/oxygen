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
 * path参数处理
 *
 * @author wubo
 */
public class PathParamHandler implements ParamHandler {

  @Override
  public boolean supported(DataBinder dataBinder) {
    return dataBinder.getScope() == SCOPE.PATH;
  }

  @Override
  public Object handle(DataBinder dataBinder) {
    Request request = Request.current();

    // 简单类型
    DefaultConverterService converterService = DefaultConverterService.sharedConverterService();
    if (converterService.canConverter(String.class, dataBinder.getType())) {
      String value = request.getPathVariables()
          .getOrDefault(dataBinder.getName(), dataBinder.getDefaultValue());
      return converterService.convert(value, dataBinder.getType());
    }

    Map<String, Object> map = new HashMap<>(request.getPathVariables());

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
