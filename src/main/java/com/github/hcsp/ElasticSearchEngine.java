package com.github.hcsp;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticSearchEngine {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("------------------zj");
            System.out.println("请输入你要查询的关键词： ");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String keyword = bufferedReader.readLine();
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
                SearchRequest searchRequest = new SearchRequest();
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "title", "content"));
                searchRequest.source(searchSourceBuilder);
                SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
                response.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
