package com.nextlabs.common.cc.cisupport.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import com.nextlabs.common.cc.cisupport.Task;
import com.nextlabs.common.cc.cisupport.dto.Environment;
import com.nextlabs.common.cc.cisupport.dto.EnvironmentGroup;

/**
 * Perform uploading files to provided environments.
 *
 * @author Sachindra Dasun
 */
public class UploadTask implements Task {

    private String environmentGroup;
    private String file;

    @Override
    public void run() throws IOException {
        if (initParameters()) {
            List<Environment> selectedEnvironments = getSelectedEnvironments();
            upload(selectedEnvironments);
        }
    }

    private boolean initParameters() {
        environmentGroup = System.getProperty("environmentGroup");
        if (StringUtils.isEmpty(environmentGroup)) {
            System.out.println("Parameter 'environmentGroup' is required");
            return false;
        }
        file = System.getProperty("file");
        if (StringUtils.isEmpty(file)) {
            System.out.println("Parameter 'file' is required");
            return false;
        }
        return true;
    }

    private List<Environment> getSelectedEnvironments() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<EnvironmentGroup> environmentGroups = objectMapper.readValue(getClass().getClassLoader()
                .getResourceAsStream("environments.json"), new TypeReference<List<EnvironmentGroup>>() {
        });
        List<Environment> environments = new ArrayList<>();
        environmentGroups.stream()
                .filter(group -> environmentGroup.equals(group.getName()))
                .forEach(group -> environments.addAll(group.getEnvironments()));
        return environments;
    }

    private void upload(List<Environment> environments) {
        environments.forEach(environment -> {
            Channel channel = null;
            ChannelSftp sftp = null;
            Session session = null;
            try {
                System.out.printf("STARTED\t\t: Uploading %s to %s on %s using SFTP\n", file,
                        environment.getPath(), environment.getHost());
                long startTime = System.currentTimeMillis();
                session = new JSch().getSession(environment.getUsername(),
                        environment.getHost(), environment.getPort());
                session.setPassword(environment.getPassword());
                Properties properties = new Properties();
                properties.put("StrictHostKeyChecking", "no");
                session.setConfig(properties);
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                sftp = (ChannelSftp) channel;
                sftp.cd(environment.getPath());
                sftp.put(file, ".");
                System.out.printf("COMPLETED\t: Uploading %s to %s on %s using SFTP in %d ms\n",
                        file, environment.getPath(), environment.getHost(), (System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                System.out.printf("ERROR\t\t: Uploading %s to %s on %s using SFTP\n", file,
                        environment.getPath(), environment.getHost());
                e.printStackTrace();
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (sftp != null) {
                    sftp.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        });
    }
}
