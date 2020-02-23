package com.github.hcsp;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchDataGenerator {
    public static void main(String[] args) {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);


        try (SqlSession sqlSession = sqlSessionFactory.openSession()) { // 批处理模式
            List<News> newsFromMySQL = sqlSession.selectList("com.github.hcsp.MockMapper.selectNews");
            for (int i = 0; i < 10; i++) {
                new Thread(() -> insertDataIntoES(newsFromMySQL)).start();
            }
        }
    }

    private static void insertDataIntoES(List<News> newsFromMySQL) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            // 单线程写入 200_0000 条数据
            for (int i = 0; i < 1000; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromMySQL) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> data = new HashMap<>();
                    data.put("link", news.getLink());
                    data.put("title", news.getTitle());
                    data.put("content", news.getContent().length() > 10 ? news.getContent().substring(0, 10) : news.getContent());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("updatedAt", news.getUpdatedAt());
                    request.source(data, XContentType.JSON);
                    bulkRequest.add(request);
                }
                BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("现在的线程是： " + Thread.currentThread() + " finishes " + i + " : " + response.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
