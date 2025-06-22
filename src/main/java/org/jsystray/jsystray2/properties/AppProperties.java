package org.jsystray.jsystray2.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<RunMavenProperties> runMaven;

    public List<RunMavenProperties> getRunMaven() {
        return runMaven;
    }

    public void setRunMaven(List<RunMavenProperties> runMaven) {
        this.runMaven = runMaven;
    }
}
