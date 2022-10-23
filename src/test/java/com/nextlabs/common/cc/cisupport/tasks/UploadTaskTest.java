package com.nextlabs.common.cc.cisupport.tasks;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for file upload.
 *
 * @author Sachindra Dasun
 */
public class UploadTaskTest {

    @Before
    public void setUp() {
        System.setProperty("file", "src/test/resources/test_upload.txt");
        System.setProperty("environmentGroup", "Test");
    }

    @Test
    public void testUpload() throws Exception {
        UploadTask uploadTask = new UploadTask();
        uploadTask.run();
    }

    @Test
    public void testRestartCC() throws Exception {
        RestartCCTask uploadTask = new RestartCCTask();
        uploadTask.run();
    }

}