package com.da2win.xunwu.repository;

import com.da2win.xunwu.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 * @author Darwin
 * @date 2018/7/4
 */
public interface HousePictureRepository extends CrudRepository<HousePicture, Long> {
    List<HousePicture> findAllByHouseId(Long id);
}
