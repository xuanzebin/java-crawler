package com.github.hcsp;

import org.apache.commons.httpclient.URI;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        List<String> linkPool = new ArrayList<>();
        Set<String> completedPool = new HashSet<>();
        linkPool.add("https://sina.cn/");
        while (true) {
            String link = linkPool.remove(0);
            if (link.startsWith("//")) {
                link += "https:";
            }
            if (link.contains("\\/")) {
                link.replaceAll("\\\\", "");
            }
            boolean isValuableLink = link.contains("news.sina.cn") || link.contains("https://sina.cn");
            boolean isRepeat = completedPool.contains(link);
            if (!isValuableLink || isRepeat) {
                continue;
            } else {
//                System.out.println(link);
//                URI uri = new URI(link, false, "UTF-8");
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(link);
                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    completedPool.add(link);
                    System.out.println(response1.getStatusLine());
                    HttpEntity entity1 = response1.getEntity();
                    Document html = Jsoup.parse(EntityUtils.toString(entity1));
                    Elements aTags = html.select("a");
                    for (Element aTag : aTags) {
                        String aTagLink = aTag.attr("href");
                        linkPool.add(aTagLink);
                    }
                    Elements article = html.select("article");
                    if (article.size() > 0) {
                        String title = article.get(0).child(0).text();
                        System.out.println(title);
                    }
                }
            }
        }
    }
}
