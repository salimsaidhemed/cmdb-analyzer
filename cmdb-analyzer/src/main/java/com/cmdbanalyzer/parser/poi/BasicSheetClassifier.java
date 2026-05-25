package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.SheetType;
import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.HeaderDetector;
import com.cmdbanalyzer.parser.SheetClassifier;
import com.cmdbanalyzer.parser.SheetDescriptor;

import java.util.Locale;
import java.util.Map;

/**
 * Basic structural sheet classifier for known CMDB workbook shapes.
 */
public class BasicSheetClassifier implements SheetClassifier {

    private final HeaderDetector headerDetector;

    public BasicSheetClassifier() {
        this(new BasicHeaderDetector());
    }

    public BasicSheetClassifier(HeaderDetector headerDetector) {
        this.headerDetector = headerDetector;
    }

    @Override
    public SheetType classify(SheetDescriptor sheetDescriptor) {
        String sheetName = sheetDescriptor.sheetName() == null
                ? ""
                : sheetDescriptor.sheetName().toLowerCase(Locale.ROOT);
        HeaderDetectionResult headers = headerDetector.detectHeaders(sheetDescriptor);
        Map<String, Integer> headerMap = headers.headerMap();

        if (sheetName.contains("changelog") || headerMap.containsKey("version") && headerMap.containsKey("modification date")) {
            return SheetType.METADATA;
        }
        if (sheetName.contains("event") || sheetName.contains("binding")
                || headerMap.containsKey("node (source)") || headerMap.containsKey("configuration item")) {
            return SheetType.EVENT_BINDING;
        }
        if (sheetName.contains("lookup") || sheetName.contains("dashboard") || sheetName.contains("location")) {
            return SheetType.LOOKUP;
        }
        if (headerMap.containsKey("class") && headerMap.containsKey("name")
                && headerMap.containsKey("parent ci") && headerMap.containsKey("relationship")) {
            return SheetType.CI_BLOCK;
        }
        if (headerMap.containsKey("class") && headerMap.containsKey("name")) {
            return SheetType.FLAT_CI_LIST;
        }
        return SheetType.UNKNOWN;
    }
}
