package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class CustomOperationCustomizer implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    ApiChangeLogs apiChangeLogs = handlerMethod.getMethodAnnotation(ApiChangeLogs.class);

    if (apiChangeLogs != null) {
      StringBuilder tableBuilder = new StringBuilder();

      tableBuilder.append("\n\n**변경 관리 이력:**\n");
      tableBuilder.append("<table>\n");
      tableBuilder.append("<thead>\n");
      tableBuilder.append("<tr><th>날짜</th><th>작성자</th><th>변경 내용</th></tr>\n");
      tableBuilder.append("</thead>\n");
      tableBuilder.append("<tbody>\n");

      for (ApiChangeLog log : apiChangeLogs.value()) {
        String description = log.description()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");

        tableBuilder.append(String.format(
            "<tr><td>%s</td><td>%s</td><td>%s</td></tr>\n",
            log.date(), log.author().getDisplayName(), description));
      }

      tableBuilder.append("</tbody>\n");
      tableBuilder.append("</table>\n");

      String originalDescription = operation.getDescription() != null ? operation.getDescription() : "";
      operation.setDescription(originalDescription + tableBuilder.toString());
    }

    return operation;
  }
}
