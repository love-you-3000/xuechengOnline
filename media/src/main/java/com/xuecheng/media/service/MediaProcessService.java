package com.xuecheng.media.service;

import com.xuecheng.media.entity.MediaProcess;

import java.util.List;

/**
 * @className: MediaProcessService
 * @author: 朱江
 * @description:
 * @date: 2023/6/11
 **/

public interface MediaProcessService {
    List<MediaProcess> selectByShardIndex(int shardTotal, int shardIndex, int count);

}
