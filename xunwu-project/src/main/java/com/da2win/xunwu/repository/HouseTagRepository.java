package com.da2win.xunwu.repository;

import com.da2win.xunwu.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 * @author Darwin
 * @date 2018/7/4
 */
public interface HouseTagRepository extends CrudRepository<HouseTag, Long> {
    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);

    List<HouseTag> findAllByHouseId(Long id);
}
