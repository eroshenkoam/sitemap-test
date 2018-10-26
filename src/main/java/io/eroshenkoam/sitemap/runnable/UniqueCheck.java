package io.eroshenkoam.sitemap.runnable;

import io.eroshenkoam.sitemap.dto.SitemapData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Artem Eroshenko.
 */
public class UniqueCheck implements SafeRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueCheck.class.getSimpleName());

    private final Map<String, String> unique = new ConcurrentHashMap<>();

    private final SitemapData data;

    public UniqueCheck(final SitemapData data) {
        this.data = data;
    }

    @Override
    public void runUnsafe() throws Throwable {
        LOGGER.debug("check {}", data.getUrl());
        if (unique.containsKey(data.getUrl())) {
            LOGGER.info("{} already present in file {}", data.getUrl(), data.getSitemap());
        } else {
            unique.put(data.getUrl(), data.getSitemap());
        }
    }

    @Override
    public void onError(Throwable e) {
        LOGGER.info(String.format("%s: %s", data.getUrl(), e.getMessage()));
    }

}
