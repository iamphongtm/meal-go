package io.apikit.apicommon.autoconfigure;

import io.apikit.apicommon.problem.ProblemProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Spring Boot auto-configuration entry point for the API commons library.
 *
 * <p>Registers {@link ProblemProperties} for Servlet-based applications so
 * consuming services can configure RFC 9457 problem type URIs through
 * {@code apikit.problem.*}. Exception handling remains owned by each service,
 * allowing services to customize their response contract when necessary.</p>
 *
 * @author phongtm
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(ProblemProperties.class)
public class ApikitProblemAutoConfiguration {
}
