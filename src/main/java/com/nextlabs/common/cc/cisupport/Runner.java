package com.nextlabs.common.cc.cisupport;

import com.nextlabs.common.cc.cisupport.tasks.RestartCCTask;
import com.nextlabs.common.cc.cisupport.tasks.UploadTask;

/**
 * Main class to run.
 *
 * @author Sachindra Dasun
 */
public class Runner {

    public static void main(String[] args) throws Exception {
        if (System.getProperty("task") == null) {
            System.out.println("Task name is required");
        }
        TaskName selectedTask = TaskName.valueOf(System.getProperty("task").toUpperCase());
        Task task = null;
        switch (selectedTask) {
            case UPLOAD: {
                task = new UploadTask();
                break;
            }
            case RESTART_CC: {
                task = new RestartCCTask();
                break;
            }
        }
        if (task != null) {
            task.run();
        } else {
            System.out.println("The specified task is not implemented");
        }
    }
}
