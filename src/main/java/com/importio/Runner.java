package com.importio;

import com.importio.extractors.FirstExtractor;
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

        HttpClient httpClient = null;
        try {
            String outputFilePath = args[0];
            httpClient = new HttpClient(new SslContextFactory());
            httpClient.start();

            FirstExtractor firstExtractor = new FirstExtractor(httpClient);
            firstExtractor.extract();

            firstExtractor.waitUntilFinished();
            logger.info("Extraction finished.");

            CsvFileWriter.writeDataToCsvFile(outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.stop();
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
