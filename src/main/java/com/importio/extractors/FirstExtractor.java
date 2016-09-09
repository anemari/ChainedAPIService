package com.importio.extractors;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by anemari.
 */
public class FirstExtractor {
    private final int NUMBER_OF_PAGES = 10;
    private HttpClient httpClient;
    private ObjectMapper mapper;
    private SecondExtractor secondExtractor = new SecondExtractor();
    private Logger logger = Logger.getLogger(FirstExtractor.class);


    public FirstExtractor(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public void extract() {

        try {
            httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Extracting data ... please be patient.");

        for (int i = 1; i <= NUMBER_OF_PAGES; i++) {
            String pageNumber = "";
            if (i != 1) {
                pageNumber = "%26pageNumber%3D" + i;
            }
            String url = "https://extraction.import.io/query/extractor/d5cc9be0-8627-477c-a6cf-0c34c4c1a245?_apikey=3afa40e842c9402c986cc3221b426b894f11fee5fd57cca0ce79c405314ae90c01348102889dd2fb6f365c637aae7dd611668ec7b374c919e336331e10207aac794503ef10d6cc1051bf0565b35d2202&url=http%3A%2F%2Fwww.ikea.com%2Fgb%2Fen%2Fsearch%2F%3Fquery%3Dchair" + pageNumber;
            httpClient.newRequest(url)
                    .send(new BufferingResponseListener() {
                        public void onComplete(Result result) {
                            Response response = result.getResponse();
                        }

                        @Override
                        public void onContent(Response response, ByteBuffer content) {
                            super.onContent(response, content);
                            String contentString = new String(content.array(), Charset.forName("UTF-8"));
                            parseJsonAndCallSecondExtractor(contentString);
                        }
                    });

        }

    }

    private void parseJsonAndCallSecondExtractor(String contentString) {
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
                            String detailPageURL = getDetailPageURL(groupEntry);
                            String productName = getProductName(groupEntry);
                            String productPrice = getProductPrice(groupEntry);
                            secondExtractor.extractDataFromDetailPage(detailPageURL, productName, productPrice, httpClient);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private String getProductPrice(JsonNode groupEntry) {
        String valueToReturn = "";
        JsonNode productPrice = groupEntry.get("Product price");
        if (productPrice.isArray()) {
            for (JsonNode productPriceEntry : productPrice) {
                valueToReturn = productPriceEntry.get("text").textValue();
            }
        }
        return valueToReturn;
    }

    private String getProductName(JsonNode groupEntry) {
        String valueToReturn = "";
        JsonNode productName = groupEntry.get("Product name");
        if (productName.isArray()) {
            for (JsonNode productNameEntry : productName) {
                valueToReturn = productNameEntry.get("text").textValue();
            }
        }
        return valueToReturn;
    }

    private String getDetailPageURL(JsonNode groupEntry) {
        String valueToReturn = "";
        JsonNode detailPageURL = groupEntry.get("DetailPageURL");
        if (detailPageURL.isArray()) {
            for (JsonNode detailPageURLEntry : detailPageURL) {
                valueToReturn = detailPageURLEntry.get("href").textValue();
            }
        }
        return valueToReturn;
    }


}
