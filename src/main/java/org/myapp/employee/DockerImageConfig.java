package org.myapp.employee;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.docker")
public class DockerImageConfig {
    private String registry;
    private String image;
    private String tag;

    // Getters and setters
    public String getFullImageName() {
        return registry + "/" + image + ":" + tag;
    }
}
