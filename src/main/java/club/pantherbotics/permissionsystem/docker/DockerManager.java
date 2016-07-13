package club.pantherbotics.permissionsystem.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by root on 7/13/16.
 */
public class DockerManager {
    private static DockerManager ourInstance = new DockerManager();

    public static DockerManager i() {
        return ourInstance;
    }

    final DockerClient dockerClient;

    private DockerManager() {
        dockerClient = new DefaultDockerClient("unix:///var/run/docker.sock");

//        try {
//            dockerClient.pull("centos:7", message -> System.out.print("\r" + message.id() + " | " + message.progress()));
//            System.out.println("\rFinished pulling base image!");
//        } catch (DockerException | InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    public void startBaseContainer(String username) {
        try {
//            final HostConfig hostConfig = HostConfig.builder()
//                    .
//                            .appendBinds("/local/path:/remote/path")
//                            .appendBinds(Bind.from("/another/local/path")
//                                    .to("/another/remote/path")
//                                    .readOnly(true)
//                                    .build())

            // TODO FIX EVERYTHING! >_>
            new File("/pantherbotics/data/" + username + "/").mkdirs();
            final ContainerCreation container = dockerClient.createContainer(ContainerConfig.builder().volumes(username).cmd("/usr/sbin/init").image("centos:7").build());
            dockerClient.startContainer(container.id());
            System.out.println(dockerClient.inspectContainer(container.id()));
            final String[] command = {"bash", "-c", "ls", "/"};
            final String execId = dockerClient.execCreate(
                    container.id(), command, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());
            final LogStream output = dockerClient.execStart(execId);
            final String execOutput = output.readFully();
            System.out.println(execOutput);
            dockerClient.killContainer(container.id());
            dockerClient.removeContainer(container.id());

        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
