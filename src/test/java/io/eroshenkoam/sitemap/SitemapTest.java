package io.eroshenkoam.sitemap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.googlecode.junittoolbox.ParallelParameterized;
import io.eroshenkoam.sitemap.dto.Sitemap;
import io.eroshenkoam.sitemap.dto.Url;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.fail;

@RunWith(ParallelParameterized.class)
public class SitemapTest {

    private static final String SITEMAP_URL = "SITEMAP_URL";
    private static final String MAX_URL_COUNT = "MAX_URL_COUNT";

    private final Map<String, String> unique = new ConcurrentHashMap<>();

    @Parameterized.Parameter(0)
    public String sitemap;

    @Parameterized.Parameter(1)
    public String url;

    @Parameterized.Parameters(name = "{0}: {1}")
    public static Collection<Object[]> parameters() {
        final String sitemapUrl = getSitemapUrl();
        final List<Object[]> parameters = new ArrayList<>();
        readListValue(sitemapUrl, Sitemap.class).forEach(sitemap -> {
            readListValue(sitemap.getLoc(), Url.class).forEach(url -> {
                parameters.add(new Object[]{sitemap.getLoc(), url.getLoc()});
            });
        });
        final Optional<Integer> maxUrlCount = getMaxUrlCount();
        return maxUrlCount.map(count -> parameters.subList(0, count)).orElse(parameters);
    }

    @Test
    public void ampTest() {
        if (url.contains("&amp")) {
            fail(String.format("[amp] %s contains &amp", url));
        }
    }

    @Test
    public void uniqueTest() {
        if (unique.containsKey(url)) {
            fail(String.format("[unique] %s already present in file: %s", url, sitemap));
        } else {
            unique.put(url, sitemap);
        }
    }

    @Test(timeout = 60000L)
    public void responseCodeTest() throws Exception {
        final int code = ((HttpURLConnection) new URL(url).openConnection()).getResponseCode();
        if (code != 200) {
            fail(String.format("[code] %s: %s", url, code));
        }
    }

    private static String getSitemapUrl() {
        return Optional.ofNullable(System.getenv(SITEMAP_URL))
                .orElseThrow(() -> new NullPointerException("sitemap url can't be null"));
    }

    private static Optional<Integer> getMaxUrlCount() {
        return Optional.ofNullable(System.getenv(MAX_URL_COUNT))
                .filter(StringUtils::isNumeric)
                .map(Integer::parseInt);
    }

    private static <T> List<T> readListValue(final String url, Class<T> type) {
        try (InputStream stream = new URL(url).openStream()) {
            return readListValue(new GZIPInputStream(stream), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static <T> List<T> readListValue(final InputStream stream, Class<T> type) throws IOException {
        final XmlMapper mapper = new XmlMapper();
        return mapper.readValue(stream, mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

}
