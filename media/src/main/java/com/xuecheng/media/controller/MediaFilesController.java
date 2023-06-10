package com.xuecheng.media.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.dto.QueryMediaParamsDto;
import com.xuecheng.media.dto.UploadFileResultDto;
import com.xuecheng.media.entity.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("上传文件")
    @PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload) throws IOException {
        // 返回类型不直接使用MediaFiles是为了程序的可拓展性，如果之后前端要求返回更多字段方便拓展
        Long companyId = 1232141425L;
        MediaFiles mediaFiles = new MediaFiles();
        mediaFiles.setFileSize(upload.getSize());
        mediaFiles.setFileType("001001");
        mediaFiles.setFilename(upload.getOriginalFilename());
        File tempFile = File.createTempFile("minio", ".temp");
        upload.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();
        UploadFileResultDto dto = mediaFileService.uploadFile(companyId, mediaFiles, absolutePath);
        File f = new File(absolutePath);
        if (f.delete()) {
            System.out.println("删除成功");
        } else {
            System.out.println("删除失败");
        }
        return dto;
    }
}
