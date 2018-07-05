package com.da2win.xunwu.repository;

import com.da2win.xunwu.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 * @author Darwin
 * @date 2018/7/4
 */
public interface HouseDetailRepository extends CrudRepository<HouseDetail, Long> {

    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);

    HouseDetail findByHouseId(Long id);
}
