package com.importio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.importio.extractors.FirstExtractor;
import com.importio.extractors.SecondExtractor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Created by anemari.
 */
public class Runner {
    private static Logger logger = Logger.getLogger(Runner.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Provide output file path");
            System.exit(1);
        }

        String outputFilePath = args[0];

        HttpClient httpClient = new HttpClient(new SslContextFactory());
        ObjectMapper mapper = new ObjectMapper();
        FirstExtractor firstExtractor = new FirstExtractor(httpClient, mapper);
        firstExtractor.extract();

        try {
            SecondExtractor.doneSignal.await();
            logger.info("Extraction finished.");
            httpClient.stop();
            CsvFileWriter.writeDataToCsvFile(outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
