package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
    boolean isTheLinkAlreadyProcessed(String link) throws SQLException;

    void deleteProcessedLink(String link) throws SQLException;

    void insertAlreadyProcessedLinkIntoDatabase(String link) throws SQLException;

    void insertToBeProcessedLinkIntoDatabase(String href) throws SQLException;

    String getLinkFromDatabase(String sql) throws SQLException;

    void updateNewsToDatabase(String link, String title, String content) throws SQLException;
}
