package io.eroshenkoam.sitemap.runnable;

import io.eroshenkoam.sitemap.dto.SitemapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Artem Eroshenko.
 */
public class AmpCheck implements SafeRunnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(AmpCheck.class.getSimpleName());

    private final SitemapData data;

    public AmpCheck (final SitemapData data) {
        this.data = data;
    }

    @Override
    public void runUnsafe() throws Throwable {
        LOGGER.debug("check {}", data.getUrl());
        if (data.getUrl().contains("&amp")) {
            LOGGER.info("{}: contains &amp", data.getUrl());
        }
    }

    @Override
    public void onError(Throwable e) {
        LOGGER.info("{}: {}", data.getUrl(), e.getMessage());
    }

}
