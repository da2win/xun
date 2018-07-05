package com.da2win.xunwu.repository;

import com.da2win.xunwu.entity.HouseSubscribe;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Darwin
 * @date 2018/7/5
 */
public interface HouseSubscribeRepository extends CrudRepository<HouseSubscribe, Long>{
    HouseSubscribe findByHouseIdAndUserId(Long id, Long loginUserId);
}
