package com.importio;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anemari.
 */
public class DataMap {

    public static Map<String, RowInFile> rowsMap = Collections.synchronizedMap(new HashMap<>());

}
