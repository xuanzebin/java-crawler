package com.github.hcsp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linkPool = new ArrayList<>();
        Set<String> completedPool = new HashSet<>();
        linkPool.add("https://sina.cn/");
        while (true) {
            String link = handleTheLink(linkPool.remove(0));
            boolean isValuableLink = link.contains("news.sina.cn") || link.contains("https://sina.cn");
            boolean isRepeat = completedPool.contains(link);

            if (isValuableLink && !isRepeat) {
                Document html = getTheHtmlAndParseIt(link);
                Elements aTags = html.select("a");
                aTags.stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                completedPool.add(link);
                getArticles(html);
            }
        }
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
        if (link.contains("\\")) {
            link = link.replaceAll("\\\\", "");
        }
        if (link.startsWith("//")) {
            link += "https:";
        }
        return link;
    }
}
