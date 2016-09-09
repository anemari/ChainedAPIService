package com.importio;

import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by anemari.
 */
public class CsvFileWriter {

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String HEADER = "Product name,Product price,Detail page url,Article Number,Product Images";
    private static Logger logger = Logger.getLogger(CsvFileWriter.class);


    public static void writeDataToCsvFile(String outputFilePath) {

        logger.info("Writing data to csv.");

        FileWriter fileWriter = null;
        try {
            fileWriter = getFileWriter(outputFilePath);
            Map<String, RowInFile> rowsMap = DataMap.rowsMap;
            for (String row : rowsMap.keySet()) {
                RowInFile line = rowsMap.get(row);
                String productName = line.getProductName();
                String productPrice = line.getProductPrice();
                String detailPageURL = line.getDetailPageURL();
                String articleNumber = line.getArticleNumber();
                ArrayList<String> productImages = line.getProductImages();

                productName = removeAccentsFromProductName(productName);
                fileWriter.append(productName);
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(productPrice);
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(detailPageURL);
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(articleNumber);
                fileWriter.append(COMMA_DELIMITER);
                String productImagesString = "";
                for (String productImage : productImages) {
                    productImagesString += productImage + "            ";
                }
                fileWriter.append(productImagesString);
                fileWriter.append(NEW_LINE_SEPARATOR);
            }

            logger.info("Csv generated!");

        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                logger.error("Error while flushing/closing fileWriter ", e);
            }

        }
    }

    private static String removeAccentsFromProductName(String productName) {
        productName = Normalizer.normalize(productName, Normalizer.Form.NFD);
        productName = productName.replaceAll("[^\\p{ASCII}]", "");
        return productName;
    }

    private static FileWriter getFileWriter(String outputFilePath) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFilePath);
        fileWriter.append(HEADER);
        fileWriter.append(NEW_LINE_SEPARATOR);
        return fileWriter;
    }

}
