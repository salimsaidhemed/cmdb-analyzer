package com.cmdbanalyzer.service;

import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.parser.ParseResult;
import com.cmdbanalyzer.parser.WorkbookParser;
import com.cmdbanalyzer.parser.poi.PoiWorkbookParser;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Application service for importing CMDB workbooks.
 */
public class CmdbImportService {

    private final WorkbookParser workbookParser;

    public CmdbImportService() {
        this(new PoiWorkbookParser());
    }

    public CmdbImportService(WorkbookParser workbookParser) {
        this.workbookParser = Objects.requireNonNull(workbookParser, "workbookParser must not be null");
    }

    public ParseResult<CmdbWorkbook> importWorkbook(Path workbookPath) {
        return workbookParser.parse(workbookPath);
    }
}
