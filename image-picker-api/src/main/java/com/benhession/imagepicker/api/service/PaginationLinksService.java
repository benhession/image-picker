package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.common.model.PageInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PaginationLinksService {

    public Link[] getPaginationLinks(PageInfo pageInfo, UriInfo uriInfo, Map<String, String> extraParams) {
        List<Link> links = new ArrayList<>();
        links.add(buildPageUri(uriInfo, 0, pageInfo.size(), extraParams, "first"));
        links.add(buildPageUri(uriInfo, pageInfo.lastPage(), pageInfo.size(), extraParams, "last"));
        links.add(buildPageUri(uriInfo, pageInfo.page(), pageInfo.size(), extraParams, "current"));

        if (pageInfo.page() + 1 <= pageInfo.lastPage()) {
            links.add(buildPageUri(uriInfo, pageInfo.page() + 1, pageInfo.size(), extraParams, "next"));
        }

        if (pageInfo.page() - 1 >= 0) {
            links.add(buildPageUri(uriInfo, pageInfo.page() - 1, pageInfo.size(), extraParams, "previous"));
        }

        return links.toArray(new Link[0]);
    }

    private Link buildPageUri(UriInfo uriInfo, int page, int size, Map<String, String> params, String rel) {
        var uriBuilder = UriBuilder.newInstance()
            .uri(uriInfo.getBaseUri())
            .path(uriInfo.getPath())
            .queryParam("page", page)
            .queryParam("size", size);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            uriBuilder.queryParam(entry.getKey(), entry.getValue());
        }

        return Link.fromUri(uriBuilder.build())
            .rel(rel)
            .build();
    }
}
