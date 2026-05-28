package com.cmdbanalyzer.ui.impact;

import com.cmdbanalyzer.analyzer.impact.ImpactAnalysisResult;
import com.cmdbanalyzer.analyzer.impact.ImpactDirection;

import java.util.function.Consumer;

/**
 * Runs impact analysis for the UI, typically on a background executor.
 */
@FunctionalInterface
public interface ImpactAnalysisRequestHandler {

    void analyze(
            String ciId,
            ImpactDirection direction,
            int maxDepth,
            Consumer<ImpactAnalysisResult> onSucceeded,
            Consumer<Throwable> onFailed,
            Runnable onFinished
    );
}
