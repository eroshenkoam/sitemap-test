package io.eroshenkoam.sitemap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.eroshenkoam.sitemap.dto.SitemapData;
import io.eroshenkoam.sitemap.dto.SitemapUrl;
import io.eroshenkoam.sitemap.runnable.AmpCheck;
import io.eroshenkoam.sitemap.runnable.StatusCodeCheck;
import io.eroshenkoam.sitemap.runnable.UniqueCheck;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * @author Artem Eroshenko.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getSimpleName());

    private static final String SITEMAP_URL = "SITEMAP_URL";

    private static final String URLS_COUNT = "URLS_COUNT";
    private static final String POOL_SIZE = "POOL_SIZE";

    private static final String GZIP = "GZIP";

    public static void main(String[] args) throws Exception {
        setupHosttsValid();

        final List<SitemapData> parameters = getParameters();
        final ExecutorService pool = Executors.newFixedThreadPool(getPoolSize());
        parameters.forEach(data -> {
            pool.execute(new StatusCodeCheck(data));
            pool.execute(new UniqueCheck(data));
            pool.execute(new AmpCheck(data));
        });

        pool.shutdown();
        pool.awaitTermination(4, TimeUnit.HOURS);
        pool.shutdownNow();
    }


    private static <T> List<T> readListValue(final String url, Class<T> type) {
        LOGGER.info("Read url {}", url);
        try (InputStream stream = new URL(url).openStream()) {
            final InputStream nested = isGZIP() ? new GZIPInputStream(stream) : stream;
            return readListValue(nested, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> List<T> readListValue(final InputStream stream, Class<T> type) throws IOException {
        final XmlMapper mapper = new XmlMapper();
        return mapper.readValue(stream, mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    private static String getSitemapUrl() {
        return Optional.ofNullable(System.getenv(SITEMAP_URL))
                .orElseThrow(() -> new NullPointerException("sitemap url can't be null"));
    }

    private static List<SitemapData> getParameters() {
        final String sitemapUrl = getSitemapUrl();
        final List<SitemapData> parameters = new ArrayList<>();
        readListValue(sitemapUrl, SitemapUrl.class).forEach(sitemap -> {
            readListValue(sitemap.getLoc(), SitemapUrl.class).forEach(url -> {
                final SitemapData data = new SitemapData()
                        .setSitemap(sitemap.getLoc())
                        .setUrl(url.getLoc());
                parameters.add(data);
            });
        });
        return getUrlsCount().map(count -> parameters.subList(0, count)).orElse(parameters);
    }

    private static Optional<Integer> getUrlsCount() {
        return Optional.ofNullable(System.getenv(URLS_COUNT))
                .filter(StringUtils::isNumeric)
                .map(Integer::parseInt);
    }

    private static Integer getPoolSize() {
        return Optional.ofNullable(System.getenv(POOL_SIZE))
                .filter(StringUtils::isNumeric)
                .map(Integer::parseInt)
                .orElse(10);
    }

    private static Boolean isGZIP() {
        return Optional.ofNullable(System.getenv(GZIP))
                .map(Boolean::getBoolean)
                .orElse(false);
    }

    private static void setupHosttsValid() throws Exception {
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        final HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
}
