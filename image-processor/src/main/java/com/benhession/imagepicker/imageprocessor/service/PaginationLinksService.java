package com.benhession.imagepicker.imageprocessor.service;

import com.benhession.imagepicker.imageprocessor.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PaginationLinksService {

    public Link[] getPaginationLinks(PageInfo pageInfo, UriInfo uriInfo) {
        List<Link> links = new ArrayList<>();
        URI baseUri = uriInfo.getBaseUri();
        links.add(buildPageUri(baseUri, 0, pageInfo.size(), "first"));
        links.add(buildPageUri(baseUri, pageInfo.lastPage(), pageInfo.size(), "last"));
        links.add(buildPageUri(baseUri, pageInfo.page(), pageInfo.size(), "current"));

        if (pageInfo.page() + 1 <= pageInfo.lastPage()) {
            links.add(buildPageUri(baseUri, pageInfo.page() + 1, pageInfo.size(), "next"));
        }

        if (pageInfo.page() - 1 >= 0) {
            links.add(buildPageUri(baseUri, pageInfo.page() - 1, pageInfo.size(), "previous"));
        }

        return links.toArray(new Link[0]);
    }

    private Link buildPageUri(URI baseUri, int page, int size, String rel) {
        return Link.fromUri(
            UriBuilder.newInstance()
              .uri(baseUri)
              .queryParam("page", page)
              .queryParam("size", size)
              .build())
          .rel(rel)
          .build();
    }
}
