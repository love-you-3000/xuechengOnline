package com.xuecheng.media.controller;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.entity.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @className: BigFilesController
 * @author: 朱江
 * @description: 大文件上传接口
 * @date: 2023/6/9
 **/
@RestController
@Api(value = "大文件上传接口", tags = "大文件上传接口")
public class BigFilesController {
    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    // todo md5码是整个文件的md5码，检查一次就行了，没必要传chunk进来查，
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception {

        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();
        RestResponse<Boolean> response = mediaFileService.uploadChunk(fileMd5, chunk, absolutePath);
        File f = new File(absolutePath);
        if (f.delete()) System.out.println("删除分块" + chunk + "成功");
        else System.out.println("删除分块" + chunk + "删除失败");
        return response;
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        MediaFiles mediaFiles = new MediaFiles();
        mediaFiles.setFilename(fileName);
        mediaFiles.setCompanyId(companyId);
        return mediaFileService.mergeChunks(companyId,fileMd5, chunkTotal,mediaFiles);

    }


}


