package io.eroshenkoam.sitemap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.eroshenkoam.sitemap.dto.Url;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(Parameterized.class)
public class SitemapTest {

    private static final String SITEMAP_URL = "SITEMAP_URL";

    private final Map<String, String> unique = new HashMap<>();

    @Parameterized.Parameter(0)
    public String file;

    @Parameterized.Parameter(1)
    public String url;

    @Parameterized.Parameters(name = "{0}: {1}")
    public static Collection<Object[]> parameters() {
        final String[] files = new String[]{
                "sitemap-offers-0.xml",
                "sitemap-listing-0.xml",
                "sitemap-listing-1.xml",
                "sitemap-listing-2.xml",
                "sitemap-listing-3.xml",
                "sitemap-listing-4.xml",
                "sitemap-listing-5.xml",
                "sitemap-listing-6.xml",
                "sitemap-listing-7.xml",
                "sitemap-listing-8.xml",
                "sitemap-listing-9.xml",
                "sitemap-listing-10.xml",
                "sitemap-listing-11.xml",
                "sitemap-listing-12.xml",
                "sitemap-listing-13.xml"
        };
        final List<Object[]> parameters = new ArrayList<>();
        Arrays.stream(files).forEach(file -> {
            fromResource(file).forEach(url -> {
                parameters.add(new Object[]{file, url.getLoc()});
            });
        });
        return parameters;
    }

    @Test
    public void ampTest() {
        assertThat(url).doesNotContain("&amp");
    }

    @Test
    public void uniqueTest() {
        if (unique.containsKey(url)) {
            fail(String.format("Url %s already contains in file: %s", url, file));
        } else {
            unique.put(url, file);
        }
    }

    @Test(timeout = 10000L)
    public void responseCodeTest() throws Exception {
        assertThat(new URL(url).openConnection())
                .hasFieldOrPropertyWithValue("responseCode", 200);
    }

    private static String getResponseContent(String url ) throws Exception {
        final URLConnection connection = new URL(url).openConnection();
        byte[] bytes = IOUtils.toByteArray(connection);
        return new String(bytes, Charset.forName("UTF-8"));
    }

    private static String getSitemapUrl() {
        return Optional.ofNullable(System.getenv(SITEMAP_URL))
                .orElse("https://realty.yandex.ru/sitemap.xml");
    }

    private static List<Url> fromResource(final String name) {
        final XmlMapper mapper = new XmlMapper();
        try {
            return mapper.readValue(
                    ClassLoader.getSystemResourceAsStream(name),
                    mapper.getTypeFactory().constructCollectionType(List.class, Url.class)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
