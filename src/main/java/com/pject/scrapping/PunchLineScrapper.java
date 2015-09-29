package com.pject.scrapping;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.PrintWriter;

/**
 * PunchLineScrapper - Short description of the class
 *
 * @author Camille
 *         Last: 29/09/2015 17:08
 * @version $Id$
 */
public class PunchLineScrapper {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        int offset = 106;
        PrintWriter writer = new PrintWriter("punchlines.txt");
        for(int i = 1; i < offset; i++) {
            CloseableHttpResponse response1 = null;
            try {
                HttpGet httpGet = new HttpGet("http://www.punchline.fr/page/" + i + "/?gdsr_sort=thumbs");
                response1 = httpclient.execute(httpGet);

                System.out.println(response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                // do something useful with the response body
                String everything = EntityUtils.toString(entity1);
                Document doc = Jsoup.parse(everything);
                for(Element element : doc.select("h2.post-title")) {
                    try {
                        String citation = element.select("a").get(0).text();
                        citation = citation.replace("&nbsp;", "");
                        if(citation.length() <= 140) {
                            System.out.println(citation);
                            writer.println(citation);
                            writer.flush();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }


                // and ensure it is fully consumed
                EntityUtils.consume(entity1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(response1 != null) response1.close();
            }
        }
        writer.close();
    }

}
