package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    public static void main(String[] args) {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        mockLotsOfDataIntoDatabase(sqlSessionFactory, 2000);
    }

    private static void mockLotsOfDataIntoDatabase(SqlSessionFactory sqlSessionFactory, int number) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) { // 批处理模式
            List<News> news = sqlSession.selectList("com.github.hcsp.MockMapper.selectNews");
            int count = number - news.size();
            try {
                while (count-- > 0) {
                    int random = new Random().nextInt(news.size());
                    News copyNews = new News(news.get(random));
                    Instant time = copyNews.getCreatedAt();
                    copyNews.setCreatedAt(time.minusSeconds(new Random().nextInt(3600 * 24 * 365)));
                    copyNews.setUpdatedAt(time.minusSeconds(new Random().nextInt(3600 * 24 * 365)));
                    sqlSession.insert("com.github.hcsp.MockMapper.insertNews", copyNews);

                    System.out.println("剩余： " + count);
                    if (count % 2000 == 0) {
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
