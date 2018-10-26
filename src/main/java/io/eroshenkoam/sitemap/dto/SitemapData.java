package io.eroshenkoam.sitemap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Artem Eroshenko.
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SitemapData {

    private String sitemap;
    private String url;

}
