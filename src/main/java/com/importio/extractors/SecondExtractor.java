package com.importio.extractors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importio.DataMap;
import com.importio.RowInFile;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by anemari.
 */
public class SecondExtractor {
    private ObjectMapper mapper;
    private HttpClient httpClient;
    private CountDownLatch requestLatch;
    private Logger logger = Logger.getLogger(SecondExtractor.class);

    public SecondExtractor(ObjectMapper mapper, HttpClient httpClient, CountDownLatch requestLatch) {
        this.mapper = mapper;
        this.httpClient = httpClient;
        this.requestLatch = requestLatch;
    }

    public void extractDataFromDetailPage(final String detailPageURL, final String productName, final String productPrice) {
        String productURL = detailPageURL.replaceAll("/", "%2F");
        String basicURL = "https://extraction.import.io/query/extractor/e8e072fc-cbb0-4ed7-b8a7-ecde602af3a9?_apikey=3afa40e842c9402c986cc3221b426b894f11fee5fd57cca0ce79c405314ae90c01348102889dd2fb6f365c637aae7dd611668ec7b374c919e336331e10207aac794503ef10d6cc1051bf0565b35d2202&url=";
        String urlForRequest = basicURL + productURL;
        httpClient.newRequest(urlForRequest)
                .send(new BufferingResponseListener() {
                    public void onComplete(Result result) {
                        Response response = result.getResponse();
                    }

                    @Override
                    public void onContent(Response response, ByteBuffer content) {
                        super.onContent(response, content);
                        String contentString = new String(content.array(), Charset.forName("UTF-8"));
                        parseJsonAndSaveRowObject(contentString, detailPageURL, productName, productPrice);
                        requestLatch.countDown();
                    }

                    @Override
                    public void onFailure(Response response, Throwable failure) {
                        super.onFailure(response, failure);
                        requestLatch.countDown();
                        logger.error(failure);
                    }
                });

    }

    private void parseJsonAndSaveRowObject(String contentString, String detailPageURL, String productName, String productPrice) {
        try {
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentString);
            JsonNode actualObj = mapper.readTree(parser);
            JsonNode extractorData = actualObj.get("extractorData");
            JsonNode data = extractorData.get("data");
            if (data.isArray()) {
                for (JsonNode dataEntry : data) {
                    JsonNode group = dataEntry.get("group");
                    if (group.isArray()) {
                        for (JsonNode groupEntry : group) {
                            ArrayList<String> thumbnails = getThumbnails(groupEntry);
                            String articleNumber = getArticleNumber(groupEntry);
                            createRowObject(productName, productPrice, detailPageURL, articleNumber, thumbnails);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void createRowObject(String productName, String productPrice, String detailPageURL, String articleNumber, ArrayList<String> thumbnails) {
        RowInFile rowInFile = new RowInFile();
        rowInFile.setProductImages(thumbnails);
        rowInFile.setArticleNumber(articleNumber);
        rowInFile.setDetailPageURL(detailPageURL);
        rowInFile.setProductName(productName);
        rowInFile.setProductPrice(productPrice);
        DataMap.rowsMap.put(articleNumber, rowInFile);
    }

    private String getArticleNumber(JsonNode groupEntry) {
        String valueToReturn = "";
        JsonNode articleNumber = groupEntry.get("Article number");
        if (articleNumber.isArray()) {
            for (JsonNode articleNumberEntry : articleNumber) {
                valueToReturn = articleNumberEntry.get("text").textValue();
            }
        }

        return valueToReturn;
    }

    private ArrayList<String> getThumbnails(JsonNode groupEntry) {
        ArrayList<String> thumbnailsList = new ArrayList<String>();
        JsonNode thumbnails = groupEntry.get("Thumbnail");
        if (thumbnails != null) {
            if (thumbnails.isArray()) {
                for (JsonNode thumbnailEntry : thumbnails) {
                    thumbnailsList.add(thumbnailEntry.get("text").textValue());
                }
            }
        } else {
            thumbnailsList.add("");
        }

        return thumbnailsList;
    }

}
