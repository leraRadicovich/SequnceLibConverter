package org.example.config;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import org.springframework.stereotype.Component;

@PWA(name = "PlantUML Converter", shortName = "UML Tool")
@Component
public class AppShellConfig implements AppShellConfigurator {
}
