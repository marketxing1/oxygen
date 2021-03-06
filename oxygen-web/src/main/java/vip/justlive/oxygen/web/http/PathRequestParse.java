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
package vip.justlive.oxygen.web.http;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

/**
 * path请求解析
 *
 * @author wubo
 */
public class PathRequestParse implements RequestParse {

  @Override
  public boolean supported(HttpServletRequest req) {
    return !Request.current().getAction().getPathVariables().isEmpty();
  }

  @Override
  public void handle(HttpServletRequest req) {
    Request request = Request.current();
    String path = request.getPath();
    Matcher matcher = Pattern.compile(request.getAction().getPath()).matcher(path);
    if (matcher.matches()) {
      Map<String, String> map = request.getPathVariables();
      List<String> pathVariables = request.getAction().getPathVariables();
      for (int i = 1, len = matcher.groupCount(); i <= len; i++) {
        map.put(pathVariables.get(i - 1), matcher.group(i));
      }
    }
  }
}
