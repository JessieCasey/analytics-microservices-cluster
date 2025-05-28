package io.javatab.microservices.core.app.common;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.List;

public class QueryBuilder {
  public static Query build(List<FilterClause> clauses) {
    Criteria criteria = new Criteria();
    List<Criteria> sub = clauses.stream()
      .map(c -> switch (c.getOperation()) {
          case eq -> Criteria.where(c.getField()).is(convert(c));
          case ne -> Criteria.where(c.getField()).ne(convert(c));
          case gt -> Criteria.where(c.getField()).gt(convert(c));
          case gte -> Criteria.where(c.getField()).gte(convert(c));
          case lt -> Criteria.where(c.getField()).lt(convert(c));
          case lte -> Criteria.where(c.getField()).lte(convert(c));
      })
      .toList();

    if (!sub.isEmpty()) {
      criteria = new Criteria().andOperator(sub.toArray(new Criteria[0]));
    }
    return new Query(criteria);
  }

  private static Object convert(FilterClause c) {
    // you can extend this to parse dates, numbers, booleans, etc.
    if (c.getField().equals("date")) {
      return LocalDate.parse(c.getRawValue());
    }
    return c.getRawValue();
  }
}
