package com.da2win.xunwu.service.search;

import com.da2win.xunwu.ApplicationTests;
import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.web.form.RentSearch;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by Darwin on 2018/7/10.
 */
public class SearchServiceTest extends ApplicationTests {
    @Autowired
    private ISearchService searchService;

    @Test
    public void index() throws Exception {
        Long targetHouseId = 15L;
        searchService.index(targetHouseId);
    }

    @Test
    public void remove() throws Exception {
        Long targetHouseId = 15L;
        searchService.remove(targetHouseId);
    }

    @Test
    public void query() throws Exception {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(10);
        ServiceMultiResult<Long> serviceMultiResult = searchService.query(rentSearch);
        Assert.assertEquals(9, serviceMultiResult.getTotal());
    }
}