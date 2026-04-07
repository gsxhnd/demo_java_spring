package com.example.esdemo.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.example.esdemo.entity.ProductDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Bool query: must match keyword in name, filter by price range, with highlight
     */
    public List<SearchHit<ProductDoc>> boolSearch(String keyword, Double minPrice, Double maxPrice) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.match(mt -> mt.field("name").query(keyword)))
                        .filter(f -> f.range(r -> r.number(n -> n
                                .field("price")
                                .gte(minPrice)
                                .lte(maxPrice))))
                ))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(List.of(new HighlightField("name"))),
                        ProductDoc.class
                ))
                .build();

        SearchHits<ProductDoc> hits = elasticsearchOperations.search(query, ProductDoc.class);
        return hits.getSearchHits();
    }

    /**
     * Aggregation: terms on brand + avg price
     */
    public Map<String, Object> aggregationStats() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withAggregation("brand_agg",
                        Aggregation.of(a -> a.terms(t -> t.field("brand").size(20))))
                .withAggregation("avg_price",
                        Aggregation.of(a -> a.avg(av -> av.field("price"))))
                .withMaxResults(0)
                .build();

        SearchHits<ProductDoc> hits = elasticsearchOperations.search(query, ProductDoc.class);

        Map<String, Object> result = new HashMap<>();
        if (hits.getAggregations() instanceof ElasticsearchAggregations aggs) {
            // brand terms
            var brandAgg = aggs.get("brand_agg");
            if (brandAgg != null) {
                var buckets = brandAgg.aggregation().getAggregate().sterms().buckets().array();
                Map<String, Long> brandCounts = new LinkedHashMap<>();
                buckets.forEach(b -> brandCounts.put(b.key().stringValue(), b.docCount()));
                result.put("brandCounts", brandCounts);
            }
            // avg price
            var avgPriceAgg = aggs.get("avg_price");
            if (avgPriceAgg != null) {
                result.put("avgPrice", avgPriceAgg.aggregation().getAggregate().avg().value());
            }
        }
        return result;
    }

    /**
     * Highlight search using ElasticsearchOperations
     */
    public List<Map<String, Object>> highlightSearch(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .fields("name", "description")
                        .query(keyword)))
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(List.of(
                                new HighlightField("name"),
                                new HighlightField("description")
                        )),
                        ProductDoc.class
                ))
                .build();

        SearchHits<ProductDoc> hits = elasticsearchOperations.search(query, ProductDoc.class);
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit<ProductDoc> hit : hits) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("product", hit.getContent());
            entry.put("highlights", hit.getHighlightFields());
            results.add(entry);
        }
        return results;
    }
}
