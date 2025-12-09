package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.manager.CosManager;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Upload template (supports image or video files).
 */
@Slf4j
public abstract class PictureUploadTemplate {

    private static final List<String> IMAGE_SUFFIX_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * Upload file to COS.
     *
     * @param inputSource      file source (MultipartFile or URL)
     * @param uploadPathPrefix upload path prefix
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. validate
        validPicture(inputSource);
        // 2. build upload path
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginFilename(inputSource);
        String fileSuffix = FileUtil.getSuffix(originalFilename);
        String lowerSuffix = fileSuffix == null ? "" : fileSuffix.toLowerCase();
        boolean isImage = IMAGE_SUFFIX_LIST.contains(lowerSuffix);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            // 3. create temp file
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource, file);
            // 4. upload
            if (isImage) {
                PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
                ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
                ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
                List<CIObject> objectList = processResults.getObjectList();
                if (CollUtil.isNotEmpty(objectList)) {
                    CIObject compressedCiObject = objectList.get(0);
                    CIObject thumbnailCiObject = objectList.size() > 1 ? objectList.get(1) : compressedCiObject;
                    return buildResult(originalFilename, compressedCiObject, thumbnailCiObject, imageInfo);
                }
                return buildResult(originalFilename, file, uploadPath, imageInfo);
            } else {
                cosManager.putObject(uploadPath, file);
                return buildFileResult(originalFilename, file, uploadPath);
            }
        } catch (Exception e) {
            log.error("上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 5. cleanup
            this.deleteTempFile(file);
        }
    }

    /**
     * validate input source (file or url)
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * get original filename
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * process source into local temp file
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * build result for image with compressed and thumbnail
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject,
                                            ImageInfo imageInfo) {
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * build result for image without thumbnail
     */
    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }

    /**
     * build result for non-image file
     */
    private UploadPictureResult buildFileResult(String originalFilename, File file, String uploadPath) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(0);
        uploadPictureResult.setPicHeight(0);
        uploadPictureResult.setPicScale(null);
        uploadPictureResult.setPicFormat(FileUtil.getSuffix(originalFilename));
        uploadPictureResult.setPicColor(null);
        uploadPictureResult.setThumbnailUrl(null);
        return uploadPictureResult;
    }

    /**
     * delete temp file
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
