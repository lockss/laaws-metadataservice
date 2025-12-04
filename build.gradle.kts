/*
 * LAAWS Metadata Service
 *
 * LOCKSS Metadata Service providing REST API for metadata management.
 */

plugins {
    id("lockss-spring-boot-conventions")
}

group = "org.lockss.laaws"
version = "2.8.0-SNAPSHOT"
description = "LOCKSS Metadata Service"

dependencies {
    // Internal dependencies
    api(project(":lockss-spring-bundle"))
    api(project(":laaws-metadataextractor-common"))

    // PostgreSQL
    api(libs.postgresql)

    // Apache Solr
    api(libs.solr.solrj)

    // Test dependencies
    testImplementation(platform(project(":lockss-pom-bundles:lockss-junit5-bundle")))
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.embedded.postgres)
    testImplementation(libs.solr.test.framework)
}

// Docker configuration
docker {
    imageName.set("laaws-metadataservice")
    restPort.set(24650)
    uiPort.set(24651)
}
