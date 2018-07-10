package com.da2win.xunwu.service.search;

import com.da2win.xunwu.ApplicationTests;
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
        boolean success = searchService.index(targetHouseId);
        Assert.assertTrue(success);
    }

    @Test
    public void remove() throws Exception {
        Long targetHouseId = 15L;
        searchService.remove(targetHouseId);
    }

}