package io.eroshenkoam.sitemap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SitemapUrl {

    private String loc;

}
