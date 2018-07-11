package com.da2win.xunwu.service.search.impl;

import com.da2win.xunwu.base.HouseSort;
import com.da2win.xunwu.base.RentValueBlock;
import com.da2win.xunwu.entity.House;
import com.da2win.xunwu.entity.HouseDetail;
import com.da2win.xunwu.entity.HouseTag;
import com.da2win.xunwu.repository.HouseDetailRepository;
import com.da2win.xunwu.repository.HouseRepository;
import com.da2win.xunwu.repository.HouseTagRepository;
import com.da2win.xunwu.service.ServiceMultiResult;
import com.da2win.xunwu.service.search.HouseIndexKey;
import com.da2win.xunwu.service.search.HouseIndexMessage;
import com.da2win.xunwu.service.search.HouseIndexTemplate;
import com.da2win.xunwu.service.search.ISearchService;
import com.da2win.xunwu.web.form.RentSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private static final String INDEX_TOPIC = "house_build";

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

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);
            switch (message.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdate(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    logger.warn("Not support message content " + content);
            }
        } catch (IOException e) {
            logger.error("Cannot parse json for " + content, e);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Delete total " + deleted);
        if (deleted <= 0) {
            this.remove(houseId, message.getRetry() + 1);
        }
    }

    private void createOrUpdate(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        House house = houseRepository.findOne(houseId);
        if (house == null) {
            logger.error("Index house {} does not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
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
    }

    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over three for house: " + houseId + ", Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
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
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
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
        this.remove(houseId, 0);
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        boolBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName())
        );
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolBuilder.filter(rangeQueryBuilder);
        }

        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            logger.debug("PASS - 1");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                logger.debug("PASS - 2");
                rangeQuery.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                logger.debug("PASS - 3");
                rangeQuery.gte(price.getMin());
            }
            boolBuilder.filter(rangeQuery);
            logger.debug("PASS - 4");
        }

        if (rentSearch.getDirection() > 0 ) {
            boolBuilder.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }

        if (rentSearch.getRentWay() >  -1) {
            boolBuilder.filter(
                QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }

        boolBuilder.must(
                QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                        HouseIndexKey.TITLE, HouseIndexKey.TRAFFIC,
                        HouseIndexKey.DISTRICT, HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME, HouseIndexKey.SUBWAY_STATION_NAME
                ));
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolBuilder)
                .addSort(HouseSort.getSortKey(rentSearch.getOrderBy()),
                        SortOrder.fromString(rentSearch.getOrderDirection())
                )
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize());
        logger.debug(requestBuilder.toString());

        List<Long> houseIds = new ArrayList<>();
        SearchResponse response = requestBuilder.get();
        if (response.status() != RestStatus.OK) {
            logger.warn("Search status is not ok for " + requestBuilder);
            new ServiceMultiResult<>(0, houseIds);
        }

        for (SearchHit searchHit : response.getHits()) {
            houseIds.add(
                    Longs.tryParse(String.valueOf(searchHit.getSource().get(HouseIndexKey.HOUSE_ID)))
            );
        }
        return new ServiceMultiResult(response.getHits().totalHits, houseIds);
    }

    private void remove(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for house: " + houseId + ", please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Cannot encode json for ");
        }
    }

}
