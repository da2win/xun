package com.da2win.xunwu.service.search.impl;

import com.da2win.xunwu.entity.House;
import com.da2win.xunwu.entity.HouseDetail;
import com.da2win.xunwu.entity.HouseTag;
import com.da2win.xunwu.repository.HouseDetailRepository;
import com.da2win.xunwu.repository.HouseRepository;
import com.da2win.xunwu.repository.HouseTagRepository;
import com.da2win.xunwu.service.search.HouseIndexKey;
import com.da2win.xunwu.service.search.HouseIndexTemplate;
import com.da2win.xunwu.service.search.ISearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Darwin
 * @date 2018/7/10
 */
@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean index(Long houseId) {
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            logger.error("Index house {} does not exist!", houseId);
            return false;
        }
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        if (houseDetail == null) {
            // TODO 异常情况
        }
        modelMapper.map(houseDetail, indexTemplate);

        List<HouseTag> tags = houseTagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(tag -> tagStrings.add(tag.getName()));
            indexTemplate.setTags(tagStrings);
        }

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        logger.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();

        boolean success;
        long totalHits = searchResponse.getHits().getTotalHits();
        if (totalHits == 0) {
            success = create(indexTemplate);
        } else if (totalHits == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);
        } else {
            success = deleteAndCreate(totalHits, indexTemplate);
        }
        if (success) {
            logger.debug("Index success with house " + houseId);
        }
        return success;
    }

    private boolean create(HouseIndexTemplate template) {
        try {
            IndexResponse indexResponse = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(template), XContentType.JSON)
                    .get();
            logger.debug("Create index with house:" + template.getHouseId());
            if (indexResponse.status() == RestStatus.CREATED) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + template.getHouseId());
            return false;
        }
    }

    private boolean update(String esId, HouseIndexTemplate template) {
        try {
            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                    .setDoc(objectMapper.writeValueAsBytes(template), XContentType.JSON)
                    .get();
            logger.debug("Update index with house:" + template.getHouseId());
            if (response.status() == RestStatus.OK) {
                return true;
            }
            return false;
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + template.getHouseId(), e);
            return false;
        }
    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate template) {
        DeleteByQueryRequestBuilder  builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, template.getHouseId()))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        if (deleted != totalHit) {
            logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
            return false;
        }
        return create(template);
    }

    @Override
    public void remove(Long houseId) {
        DeleteByQueryRequestBuilder  builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Delete total " + deleted);
    }
}
