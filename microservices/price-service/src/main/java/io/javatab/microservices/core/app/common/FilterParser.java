package io.javatab.microservices.core.app.common;

import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterParser {
  /**
   * e.g. "ticker=eq:RIV" → FilterClause("ticker", eq, "RIV")
   * Supports multiple values per field.
   */
  public static List<FilterClause> parse(MultiValueMap<String,String> params) {
    List<FilterClause> clauses = new ArrayList<>();
    for (Map.Entry<String, List<String>> e : params.entrySet()) {
      String field = e.getKey();
      for (String token : e.getValue()) {
        String[] parts = token.split(":", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Bad filter: " + token);
        Operator op = Operator.of(parts[0]);
        clauses.add(new FilterClause(field, op, parts[1]));
      }
    }
    return clauses;
  }
}
