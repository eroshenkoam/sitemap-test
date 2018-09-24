package io.eroshenkoam.sitemap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.googlecode.junittoolbox.ParallelParameterized;
import io.eroshenkoam.sitemap.dto.Sitemap;
import io.eroshenkoam.sitemap.dto.Url;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(ParallelParameterized.class)
public class SitemapTest {

    private static final String SITEMAP_URL = "SITEMAP_URL";
    private static final String MAX_URL_COUNT = "MAX_URL_COUNT";

    private final Map<String, String> unique = new HashMap<>();

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
        final Integer maxUrlCount = getMaxUrlCount();
        return parameters.subList(0, maxUrlCount);
    }

    @Test
    public void ampTest() {
        assertThat(url).doesNotContain("&amp");
    }

    @Test
    public void uniqueTest() {
        if (unique.containsKey(url)) {
            fail(String.format("Url %s already contains in file: %s", url, sitemap));
        } else {
            unique.put(url, sitemap);
        }
    }

    @Test(timeout = 60000L)
    public void responseCodeTest() throws Exception {
        assertThat(new URL(url).openConnection())
                .hasFieldOrPropertyWithValue("responseCode", 200);
    }

    private static String getSitemapUrl() {
        return Optional.ofNullable(System.getenv(SITEMAP_URL))
                .orElse("https://realty.yandex.ru/sitemap.xml");
    }

    private static Integer getMaxUrlCount() {
        return Optional.ofNullable(System.getenv(MAX_URL_COUNT))
                .map(Integer::parseInt)
                .orElse(100);
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
