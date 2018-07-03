package com.da2win.xunwu.service.house;

import com.da2win.xunwu.ApplicationTests;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.AssertTrue;
import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Darwin on 2018/7/3.
 */
public class QiNiuServiceTest extends ApplicationTests {
    @Autowired
    private IQiNiuService qiNiuService;
    @Test
    public void uploadFile() {
        String filename = "F:\\LearningSpace\\Java\\xunwu\\xunwu-project\\tmp\\AMP01.jpg";
        File file = new File(filename);

        assertTrue(file.exists());

        try {
            Response response = qiNiuService.uploadFile(file);
            assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delete() {
        String key = "FgMUPgqaheOdAGZpJBrlKvHgtXSt";
        try {
            Response response = qiNiuService.delete(key);
            assertTrue(response.isOK());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

    }
}