package io.javatab.microservices.core.app.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class BaseController<E, D> {

    private final MongoTemplate mongo;
    private final Class<E> entityClass;
    private final EntityDtoMapper<E, D> mapper;

    protected BaseController(MongoTemplate mongo,
                             Class<E> entityClass,
                             EntityDtoMapper<E, D> mapper) {
        this.mongo = mongo;
        this.entityClass = entityClass;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<Page<D>> query(
            @RequestParam MultiValueMap<String, String> allParams,
            @RequestParam(name = "sort", required = false) List<String> sortParams,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
//        log.info("Filters: {}; Sort: {}; page={}, size={}", allParams, sortParams, page, size);

        // 1) strip out control params
        MultiValueMap<String, String> filterParams = new LinkedMultiValueMap<>(allParams);
        filterParams.remove("sort");
        filterParams.remove("page");
        filterParams.remove("size");

        // 2) build filter query
        List<FilterClause> clauses = FilterParser.parse(filterParams);
        Query baseQuery = QueryBuilder.build(clauses);

        // 3) recombine & apply sorting
        Sort sort = Sort.unsorted();
        if (sortParams != null && !sortParams.isEmpty()) {
            List<String> combined = new ArrayList<>();
            for (int i = 0; i < sortParams.size(); i++) {
                String tok = sortParams.get(i);
                if (tok.contains(",")) {
                    combined.add(tok);
                } else if (i + 1 < sortParams.size()
                        && List.of("asc","desc").contains(sortParams.get(i+1).toLowerCase())) {
                    combined.add(tok + "," + sortParams.get(i+1));
                    i++;
                } else {
                    throw new IllegalArgumentException("Invalid sort token: " + tok);
                }
            }
            List<Sort.Order> orders = combined.stream()
                    .map(this::toOrder)
                    .toList();
            sort = Sort.by(orders);
        }

        // 4) count total
        long total = mongo.count(baseQuery, entityClass);

        // 5) apply pagination & sorting
        Pageable pageable = PageRequest.of(page, size, sort);
        Query pageQuery = baseQuery.with(pageable);

        // 6) fetch page of entities
        List<E> ents = mongo.find(pageQuery, entityClass);

        // 7) map to DTOs
        List<D> dtos = ents.stream()
                .map(mapper::toDto)
                .toList();

        // 8) wrap in a Page and return
        Page<D> result = new PageImpl<>(dtos, pageable, total);
        return ResponseEntity.ok(result);
    }

    private Sort.Order toOrder(String token) {
        String[] p = token.split(",", 2);
        if (p.length != 2) {
            throw new IllegalArgumentException("Expected format `field,asc|desc`: " + token);
        }
        String prop = p[0].trim();
        String dir = p[1].trim().toLowerCase();
        Sort.Direction d = switch (dir) {
            case "asc"  -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default     -> throw new IllegalArgumentException("Must be 'asc' or 'desc': " + dir);
        };
        return new Sort.Order(d, prop);
    }
}
