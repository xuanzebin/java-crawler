package com.github.hcsp;

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
import java.util.stream.Collectors;

public class Crawler extends Thread {
    private final CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            String link;
            while ((link = dao.getLinkAndDeleteItFromDatabase()) != null) {
                if (checkLinkIsUsefulOrNot(link)) {
                    Document html = getTheHtmlAndParseIt(link);

                    findTheHrefFromATagsAndInsertIntoDatabase(html);
                    dao.insertAlreadyProcessedLinkIntoDatabase(link);
                    getArticles(html, link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findTheHrefFromATagsAndInsertIntoDatabase(Document html) throws SQLException {
        Elements aTags = html.select("a");
        for (Element aTag : aTags) {
            String href = aTag.attr("href");
            if (href.startsWith("javascript")) {
                continue;
            }
            href = href.replaceAll("\\\\", "");
            href = href.replaceAll("\\|", "%7C");
            if (href.startsWith("//")) {
                href += "https:";
            }
            dao.insertToBeProcessedLinkIntoDatabase(href);
        }
    }

    private boolean checkLinkIsUsefulOrNot(String link) throws SQLException {
        boolean isValuableLink = link.contains("news.sina.cn") || link.contains("https://sina.cn");
        boolean isRepeat = dao.isTheLinkAlreadyProcessed(link);
        return isValuableLink && !isRepeat;
    }

    public void getArticles(Document html, String link) throws SQLException {
        Elements article = html.select("article");
        if (article.size() > 0) {
            String title = article.get(0).child(0).text();
            System.out.println(title);
            String content = html.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));

            dao.updateNewsToDatabase(link, title, content);
        }
    }

    public Document getTheHtmlAndParseIt(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity1));
        }
    }
}
