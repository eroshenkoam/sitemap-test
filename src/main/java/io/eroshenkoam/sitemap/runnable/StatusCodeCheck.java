package io.eroshenkoam.sitemap.runnable;

import io.eroshenkoam.sitemap.dto.SitemapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static org.awaitility.Awaitility.await;

/**
 * @author Artem Eroshenko.
 */
public class StatusCodeCheck implements SafeRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCodeCheck.class.getSimpleName());

    private static final Integer RESPONSE_CODE_OK = 200;

    private static final Integer RETRY_COUNT = 5;

    private final SitemapData data;

    public StatusCodeCheck(final SitemapData data) {
        this.data = data;
    }

    @Override
    public void runUnsafe() throws Throwable {
        LOGGER.debug("check {}", data.getUrl());
        int code = getStatusCode(data.getUrl());
        if (code != RESPONSE_CODE_OK) {
            LOGGER.info("{}: {}", data.getUrl(), code);
        }
    }

    @Override
    public void onError(Throwable e) {
        LOGGER.info(String.format("%s: %s", data.getUrl(), e.getMessage()));
    }

    private static int getStatusCode(final String url) throws Throwable {
        int current = 0;
        int code;
        do {
            code = ((HttpURLConnection) new URL(url).openConnection()).getResponseCode();
            if (current > 0) {
                Thread.sleep(1000);
            }
        } while (code != RESPONSE_CODE_OK && current++ < RETRY_COUNT);
        return code;
    }
}
