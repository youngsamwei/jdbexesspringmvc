package cn.sdkd.ccse.jdbexes.neo4j;

import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring JavaConfig configuration class to setup a Spring container and infrastructure components.
 */

@Configuration
@EnableNeo4jRepositories(basePackages = {"cn.sdkd.ccse.jdbexes.neo4j.repositories"})
@EnableTransactionManagement
public class GraphDBConfiguration extends Neo4jConfiguration {
   private final Logger logger = LoggerFactory.getLogger(GraphDBConfiguration.class);

   public GraphDBConfiguration() {
      logger.info("***************************** ---------- GraphDBConfiguration -----------************");
   }

   @Bean
   public org.neo4j.ogm.config.Configuration getConfiguration() {
      org.neo4j.ogm.config.Configuration config =
               new org.neo4j.ogm.config.Configuration();
      // TODO: Temporary uses the embedded driver. We need to switch to http
      // driver. Then we can horizontally scale neo4j
      config.driverConfiguration()
            .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver")
            .setURI("file:/F:/neo4j/sdn.jdbexes.graph.db/");
      return config;
   }

   @Override
   @Bean
   public SessionFactory getSessionFactory() {
      // Return the session factory which also includes the persistent entities
      return new SessionFactory(getConfiguration(), "cn.sdkd.ccse.jdbexes.neo4j.entities");
   }
}