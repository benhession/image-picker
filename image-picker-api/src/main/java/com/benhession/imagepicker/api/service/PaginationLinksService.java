package com.benhession.imagepicker.api.service;

import com.benhession.imagepicker.common.model.PageInfo;
import com.benhession.imagepicker.data.model.MongoSearchResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PaginationLinksService {

    public Link[] getPaginationLinks(PageInfo pageInfo, UriInfo uriInfo, Map<String, String> extraParams) {
        List<Link> links = new ArrayList<>();
        links.add(buildPageUri(uriInfo, 0, pageInfo.size(), extraParams, "first"));
        links.add(buildPageUri(uriInfo, pageInfo.lastPage(), pageInfo.size(), extraParams, "last"));
        links.add(buildCurrentUri(uriInfo));

        if (pageInfo.page() + 1 <= pageInfo.lastPage()) {
            links.add(buildPageUri(uriInfo, pageInfo.page() + 1, pageInfo.size(), extraParams, "next"));
        }

        if (pageInfo.page() - 1 >= 0) {
            links.add(buildPageUri(uriInfo, pageInfo.page() - 1, pageInfo.size(), extraParams, "previous"));
        }

        return links.toArray(new Link[0]);
    }

    public <T extends MongoSearchResult> Link[] getPaginationLinksForSearchResults(PageInfo pageInfo,
        UriInfo uriInfo, List<T> searchResults, Map<String, String> extraParams) {
        List<Link> links = new ArrayList<>();
        links.add(buildPageUri(uriInfo, 0, pageInfo.size(), extraParams, "first"));
        links.add(buildPageUri(uriInfo, pageInfo.lastPage(), pageInfo.size(), extraParams, "last"));
        links.add(buildCurrentUri(uriInfo));

        if (pageInfo.page() + 1 <= pageInfo.lastPage()) {
            Map<String, String> nextParams = new HashMap<>(extraParams);
            if (!searchResults.isEmpty()) {
                nextParams.put("searchAfter", searchResults.getLast().getPointOfReference());
            }
            links.add(buildPageUri(uriInfo, pageInfo.page() + 1, pageInfo.size(), nextParams, "next"));
        }

        if (pageInfo.page() - 1 >= 0) {
            Map<String, String> prevParams = new HashMap<>(extraParams);
            if (!searchResults.isEmpty()) {
                prevParams.put("searchBefore", searchResults.getFirst().getPointOfReference());
            }
            links.add(buildPageUri(uriInfo, pageInfo.page() - 1, pageInfo.size(), prevParams, "previous"));
        }

        return links.toArray(new Link[0]);
    }

    private Link buildPageUri(UriInfo uriInfo, int page, int size, Map<String, String> params, String rel) {
        var uriBuilder = UriBuilder.newInstance()
            .uri(uriInfo.getAbsolutePath())
            .scheme("https")
            .queryParam("page", page)
            .queryParam("size", size);
        params.forEach(uriBuilder::queryParam);

        return Link.fromUri(uriBuilder.build())
            .rel(rel)
            .build();
    }

    private Link buildCurrentUri(UriInfo uriInfo) {
        var uriBuilder = UriBuilder.newInstance()
            .uri(uriInfo.getAbsolutePath())
            .scheme("https");
        uriInfo.getQueryParameters()
            .forEach((key, value) -> uriBuilder.queryParam(key, String.join(",", value)));

        return Link.fromUri(uriBuilder.build())
            .rel("current")
            .build();
    }
}
