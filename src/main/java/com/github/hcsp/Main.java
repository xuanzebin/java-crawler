package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        while (true) {
            Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/xuanzebin3/Desktop/repos-java/java-crawler/crawler", USER_NAME, PASSWORD);

            List<String> linkPool = getLinksFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED");

            String link = linkPool.remove(0);
            deleteProcessedLink(connection, link);

            link = handleTheLink(link);

            if (checkLinkIsUsefulOrNot(link, connection)) {
                Document html = getTheHtmlAndParseIt(link);

                findTheHrefFromATagsAndInsertIntoDatabase(connection, linkPool, html);
                InsertAlreadyProcessedLinkIntoDatabase(connection, link);
                getArticles(html);
            }
        }
    }

    private static boolean isTheLinkAlreadyProcessed(Connection connection, String link) throws SQLException {
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

    private static void deleteProcessedLink(Connection connection, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TO_BE_PROCESSED where link = ?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    private static void InsertAlreadyProcessedLinkIntoDatabase(Connection connection, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED (link) values (?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    private static void findTheHrefFromATagsAndInsertIntoDatabase(Connection connection, List<String> linkPool, Document html) throws SQLException {
        Elements aTags = html.select("a");
        for (Element aTag : aTags) {
            String href = aTag.attr("href");
            linkPool.add(href);
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TO_BE_PROCESSED (link) values (?)")) {
                preparedStatement.setString(1, href);
                preparedStatement.executeUpdate();
            }

        }
    }

    private static boolean checkLinkIsUsefulOrNot(String link, Connection connection) throws SQLException {
        boolean isValuableLink = link.contains("news.sina.cn") || link.contains("https://sina.cn");
        boolean isRepeat = isTheLinkAlreadyProcessed(connection, link);
        return isValuableLink && !isRepeat;
    }

    private static List<String> getLinksFromDatabase(Connection connection, String sql) throws SQLException {
        List<String> linkPool = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            while (resultSet.next()) {
                linkPool.add(resultSet.getString(1));
            }
        }
        return linkPool;
    }

    public static void getArticles(Document html) {
        Elements article = html.select("article");
        if (article.size() > 0) {
            String title = article.get(0).child(0).text();
            System.out.println(title);
        }
    }

    public static Document getTheHtmlAndParseIt(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity1));
        }

    }

    public static String handleTheLink(String link) {
        link = link.replaceAll("\\\\", "");
        if (link.startsWith("//")) {
            link += "https:";
        }
        return link;
    }
}
