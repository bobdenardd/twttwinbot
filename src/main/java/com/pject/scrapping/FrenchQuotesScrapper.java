package com.pject.scrapping;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.PrintWriter;

/**
 * FrenchQuotesScrapper - Short description of the class
 *
 * @author Camille
 *         Last: 25/09/2015 21:36
 * @version $Id$
 */
public class FrenchQuotesScrapper {

    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        int offset = 20000;
        PrintWriter writer = new PrintWriter("citations.txt");
        for(int i = 1; i < offset; i++) {
            CloseableHttpResponse response1 = null;
            try {
                HttpGet httpGet = new HttpGet("http://www.citation-celebre.com/citations/" + i);
                response1 = httpclient.execute(httpGet);

                System.out.println(response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                // do something useful with the response body
                String everything = EntityUtils.toString(entity1);
                String citation = everything.substring(0, Math.min(everything.length(), 2000)).replaceAll("\r", "").replaceAll("\n", "").replaceAll(".*og:description", "").replaceFirst("content=\"", "").replaceAll("&.aquo;", "").replaceAll("\"", "").replaceFirst("/>.*", "").trim();
                if (citation.length() <= 140) {

                    writer.println(citation + "\n");
                    writer.flush();
                    System.out.println(i + " citation --> " + citation);

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
