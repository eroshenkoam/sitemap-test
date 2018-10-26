package io.eroshenkoam.sitemap.runnable;

import io.eroshenkoam.sitemap.dto.SitemapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Artem Eroshenko.
 */
public class StatusCodeCheck implements SafeRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCodeCheck.class.getSimpleName());

    private static final Integer RESPONSE_CODE_OK = 200;

    private final SitemapData data;

    public StatusCodeCheck(final SitemapData data) {
        this.data = data;
    }

    @Override
    public void runUnsafe() throws Throwable {
        LOGGER.debug("check {}", data.getUrl());
        final int code = ((HttpURLConnection) new URL(data.getUrl()).openConnection()).getResponseCode();
        if (code != RESPONSE_CODE_OK) {
            LOGGER.info("{}: {}", data.getUrl(), code);
        }
    }

    @Override
    public void onError(Throwable e) {
        LOGGER.info(String.format("%s: %s", data.getUrl(), e.getMessage()));
    }

}
