package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    private Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        String PASSWORD = "root";
        String USER_NAME = "root";
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/xuanzebin3/Desktop/repos-java/java-crawler/crawler", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLinkAndDeleteItFromDatabase() throws SQLException {
        String link = getLinkFromDatabase();
        if (link != null) {
            deleteProcessedLink(link);
            return link;
        }
        return null;
    }

    public boolean isTheLinkAlreadyProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where link = ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    public void deleteProcessedLink(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where link = ?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    public void insertAlreadyProcessedLinkIntoDatabase(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED (link) values (?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    public void insertToBeProcessedLinkIntoDatabase(String href) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (link) values (?)")) {
            preparedStatement.setString(1, href);
            preparedStatement.executeUpdate();
        }
    }

    public String getLinkFromDatabase() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_TO_BE_PROCESSED LIMIT 1");
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public void updateNewsToDatabase(String link, String title, String content) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (link, title, content, created_at, update_at) VALUES (?,?,?, now(), now())")) {
            preparedStatement.setString(1, link);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
        }
    }
}
