package org.jsystray.jsystray2.config;

import org.jsystray.jsystray2.properties.AppProperties;
import org.jsystray.jsystray2.properties.RunMavenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.jsystray.jsystray2")
@EnableConfigurationProperties({AppProperties.class})
public class AppConfig {
}
