package com.xiaojie.clients;

import com.xiaojie.models.Product;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * Created by hadoop on 17-6-24.
 */
@FeignClient("recommendation")
public interface RecommendationService {
    @RequestMapping(method = RequestMethod.GET, value = "/products/search/findProductsByUser?id={id}")
    PagedResources<Product> findProductsByUser(@PathVariable("id") String id);
    @RequestMapping(method = RequestMethod.GET, value = "/getSimilarMovies")
    String getSimilarMovies(@RequestParam(value = "id") String id);
}
