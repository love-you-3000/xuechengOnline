package com.xuecheng.media.service.impl;

import com.xuecheng.media.entity.MediaFiles;
import com.xuecheng.media.entity.MediaProcess;
import com.xuecheng.media.entity.MediaProcessHistory;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;


    @Override
    public List<MediaProcess> selectByShardIndex(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectByShardIndex(shardTotal, shardIndex, count);
    }

    /**
     * @Author: 朱江
     * @Description: 更新任务状态
     * @Date: 9:22 2023/6/12
     **/
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess != null) {
            // 更新失败
            if (status.equals("3")) {
                mediaProcess.setStatus("3");
                mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
                mediaProcess.setErrormsg(errorMsg);
                mediaProcessMapper.updateById(mediaProcess);
            }
            // 更新成功
            else {
                MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
                mediaFiles.setUrl(url);
                mediaFilesMapper.updateById(mediaFiles);
                mediaProcess.setStatus("2");
                mediaProcess.setUrl(url);
                mediaProcess.setFinishDate(LocalDateTime.now());
                MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
                BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
                mediaProcessHistoryMapper.insert(mediaProcessHistory);
                mediaProcessMapper.deleteById(mediaProcess);
            }
        }
    }

    @Override
    public boolean startTask(Long id) {
        return mediaProcessMapper.startTask(id) > 0;
    }
}
