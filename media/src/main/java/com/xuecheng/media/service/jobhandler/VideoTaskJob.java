package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.entity.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @className: VideoTaskJob
 * @author: 朱江
 * @description: 视频处理任务
 * @date: 2023/6/12
 **/

@Slf4j
@Component
public class VideoTaskJob {
    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaProcessService mediaProcessService;

    @Autowired
    MediaFileService mediaFileService;

    //ffmpeg的路径
    @Value("${videoProcess.ffmpegPath}")
    private String ffmpeg_path;


    /**
     * @Author: 朱江
     * @Description: 视频处理任务逻辑
     * @Date: 9:29 2023/6/12
     **/
    @XxlJob("videoJobHandler")
    public void videoJobHandler() {
        // 分片参数
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();
        // 确定CPU核心数
        int CpuCoreCount = Runtime.getRuntime().availableProcessors();
        // 查询待处理的任务
        List<MediaProcess> toProcess = mediaProcessService.selectByShardIndex(shardTotal, shardIndex, CpuCoreCount);
        // 待处理任务数
        int size = toProcess.size();
        log.info("分片id:{} 取到的视频任务数:{}",shardIndex,size);
        if(size == 0) return;
        // 创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        toProcess.forEach(mediaProcess -> {
            String md5 = mediaProcess.getFileId();
            threadPool.execute(() -> {
                // 任务执行逻辑
                Long taskId = mediaProcess.getId();
                if (mediaProcessService.startTask(taskId)) {
                    //源avi视频的路径
                    String video_path = mediaProcess.getFilePath();
                    // 从minio下载源文件
                    File file = mediaFileService.downloadFileFromMinIO(mediaProcess.getBucket(), video_path);
                    if (file == null) {
                        log.debug("下载视频出错, 任务id:{}, bucket:{}, objectName:{} ", taskId, mediaProcess.getBucket(), video_path);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", md5, null, "下载视频到本地失败！");
                        return;
                    }
                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件失败！");
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", md5, null, "创建临时文件失败！");
                    }
                    // ==============》执行文件转码《====================
                    //转换后mp4文件的名称:md5 + .mp4
                    String mp4_name = md5 + ".mp4";
                    //转换后mp4文件的路径
                    String mp4_path = null;
                    if (tempFile != null) {
                        mp4_path = tempFile.getAbsolutePath();
                    }
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("文件转码失败！");
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", md5, null, "文件转码失败！");
                        return;
                    }
                    //
                    boolean uploadRes = mediaFileService.uploadFileToMinio(mediaProcess.getBucket(), "mp4", mp4_name, mp4_path);
                    if (!uploadRes) {
                        log.debug("上传转码后文件失败！, taskId:{}", taskId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", md5, null, "上传转码后文件失败！");
                        return;
                    }
                    String savePath = md5.charAt(0) + "/" + md5.charAt(1) + "/" + md5 + ".mp4";
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", md5, savePath, null);
                } else {
                    log.debug("抢占任务失败, 任务id:{}", taskId);
                }
            });
        });
    }
}
