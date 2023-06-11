package com.xuecheng.media.service.impl;

import com.xuecheng.media.entity.MediaProcess;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className: MediaProcessServiceImpl
 * @author: 朱江
 * @description:
 * @date: 2023/6/11
 **/
@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Override
    public List<MediaProcess> selectByShardIndex(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectByShardIndex(shardTotal,shardIndex,count);
    }
}
