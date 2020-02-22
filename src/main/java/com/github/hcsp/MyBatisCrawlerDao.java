package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

public class MyBatisCrawlerDao implements CrawlerDao {
    SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public boolean isTheLinkAlreadyProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.hcsp.MyMapper.checkTheProcessedLink", link);
            return count!= 0;
        }
    }

    @Override
    public String getLinkAndDeleteItFromDatabase() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.hcsp.MyMapper.selectNextAvailableLink");
            if (link != null) {
                session.delete("com.github.hcsp.MyMapper.deleteLinkWhichIsSelected", link);
            }
            return link;
        }
    }

    @Override
    public void insertAlreadyProcessedLinkIntoDatabase(String link) throws SQLException {
        HashMap<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_ALREADY_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertToBeProcessedLinkIntoDatabase(String link) throws SQLException {
        HashMap<String, String> param = new HashMap<>();
        param.put("tableName", "LINKS_TO_BE_PROCESSED");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertLink", param);
        }
    }

    @Override
    public void updateNewsToDatabase(String link, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertNews", new News(link, title, content));
        }
    }
}
