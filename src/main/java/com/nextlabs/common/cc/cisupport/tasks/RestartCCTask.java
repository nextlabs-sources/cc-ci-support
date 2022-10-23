package com.nextlabs.common.cc.cisupport.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import com.nextlabs.common.cc.cisupport.Task;
import com.nextlabs.common.cc.cisupport.dto.Environment;
import com.nextlabs.common.cc.cisupport.dto.EnvironmentGroup;

/**
 * Perform CC restart on provided environments.
 *
 * @author Sachindra Dasun
 */
public class RestartCCTask implements Task {

    private String environmentGroup;
    private String file;

    @Override
    public void run() throws IOException {
        if (initParameters()) {
            List<Environment> selectedEnvironments = getSelectedEnvironments();
            restart(selectedEnvironments);
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

    private void restart(List<Environment> environments) {
        environments.forEach(environment -> {
            ChannelExec channel = null;
            Session session = null;
            try {
                System.out.printf("STARTED\t\t: Running CC restart command on %s\n", environment.getHost());
                long startTime = System.currentTimeMillis();
                session = new JSch().getSession("root",
                        environment.getHost(), environment.getPort());
                session.setPassword(environment.getRootPassword());
                Properties properties = new Properties();
                properties.put("StrictHostKeyChecking", "no");
                session.setConfig(properties);
                session.connect();
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand("service CompliantEnterpriseServer restart");
                channel.setErrStream(System.err);
                InputStream inputStream = channel.getInputStream();
                channel.connect();
                try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                    try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                }
                System.out.printf("COMPLETED\t: Running CC restart command on %s in %d ms\n", environment.getHost(),
                        (System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                System.out.printf("ERROR\t\t: Running CC restart command on %s\n", environment.getHost());
                e.printStackTrace();
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        });
    }
}
