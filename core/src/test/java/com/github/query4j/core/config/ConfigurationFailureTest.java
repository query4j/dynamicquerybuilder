package com.github.query4j.core.config;

import com.github.query4j.core.DynamicQueryException;
import com.github.query4j.core.QueryBuildException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for configuration failure modes including invalid
 * configurations, loading errors, and malformed configuration data.
 */
@DisplayName("Configuration Failure Mode Tests")
class ConfigurationFailureTest {

    @TempDir
    Path tempDir;
    
    private Map<String, String> originalSystemProperties;
    
    @BeforeEach
    void setUp() {
        // Save original system properties
        originalSystemProperties = new HashMap<>();
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                originalSystemProperties.put(keyStr, value.toString());
            }
        });
        
        // Clear any existing query4j properties
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                System.clearProperty(keyStr);
            }
        });
    }
    
    @AfterEach
    void tearDown() {
        // Clear test properties
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("query4j.")) {
                System.clearProperty(keyStr);
            }
        });
        
        // Restore original properties
        originalSystemProperties.forEach(System::setProperty);
    }

    @Nested
    @DisplayName("Invalid Configuration Values")
    class InvalidConfigurationValueTests {

        @Test
        @DisplayName("should handle negative timeout values")
        void shouldHandleNegativeTimeoutValues() throws IOException {
            Path configFile = tempDir.resolve("invalid-timeout.properties");
            String propertiesContent = "query4j.core.defaultQueryTimeoutMs=-1000\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should either reject negative values or normalize them
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Timeout should be normalized to a valid positive value
                assertTrue(config.getCore().getDefaultQueryTimeoutMs() > 0,
                    "Negative timeout should be normalized to positive value");
            });
        }

        @Test
        @DisplayName("should handle zero predicate depth")
        void shouldHandleZeroPredicateDepth() throws IOException {
            Path configFile = tempDir.resolve("zero-depth.properties");
            String propertiesContent = "query4j.core.maxPredicateDepth=0\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Zero depth should be normalized to minimum valid value
                assertTrue(config.getCore().getMaxPredicateDepth() > 0,
                    "Zero predicate depth should be normalized to positive value");
            });
        }

        @Test
        @DisplayName("should handle negative cache sizes")
        void shouldHandleNegativeCacheSizes() throws IOException {
            Path configFile = tempDir.resolve("negative-cache.properties");
            String propertiesContent = 
                "query4j.cache.enabled=true\n" +
                "query4j.cache.maxSize=-500\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Negative cache size should be normalized
                assertTrue(config.getCache().getMaxSize() > 0,
                    "Negative cache size should be normalized to positive value");
            });
        }

        @Test
        @DisplayName("should handle extremely large configuration values")
        void shouldHandleExtremelyLargeConfigurationValues() throws IOException {
            Path configFile = tempDir.resolve("extreme-values.properties");
            String propertiesContent = 
                "query4j.core.defaultQueryTimeoutMs=" + Long.MAX_VALUE + "\n" +
                "query4j.core.maxPredicateDepth=" + Integer.MAX_VALUE + "\n" +
                "query4j.cache.maxSize=" + Long.MAX_VALUE + "\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Values should be within reasonable bounds
                assertNotNull(config);
                assertTrue(config.getCore().getDefaultQueryTimeoutMs() > 0);
                assertTrue(config.getCore().getMaxPredicateDepth() > 0);
                assertTrue(config.getCache().getMaxSize() > 0);
            });
        }
    }

    @Nested
    @DisplayName("Malformed Configuration Files")
    class MalformedConfigurationFileTests {

        @Test
        @DisplayName("should handle properties file with invalid syntax")
        void shouldHandlePropertiesFileWithInvalidSyntax() throws IOException {
            Path configFile = tempDir.resolve("malformed.properties");
            String malformedContent = 
                "query4j.core.defaultQueryTimeoutMs=invalid_number\n" +
                "query4j.core.maxPredicateDepth=not_a_number\n" +
                "invalid_property_without_equals\n" +
                "query4j.cache.enabled=maybe\n";
            Files.write(configFile, malformedContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should handle malformed properties gracefully
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Should fall back to defaults for invalid values
                assertNotNull(config);
                assertTrue(config.getCore().getDefaultQueryTimeoutMs() > 0);
                assertTrue(config.getCore().getMaxPredicateDepth() > 0);
            });
        }

        @Test
        @DisplayName("should handle YAML file with invalid syntax")
        void shouldHandleYamlFileWithInvalidSyntax() throws IOException {
            Path configFile = tempDir.resolve("malformed.yml");
            String malformedYaml = 
                "query4j:\n" +
                "  core:\n" +
                "    defaultQueryTimeoutMs: [invalid yaml structure\n" +
                "  cache\n" +  // Missing colon
                "    enabled: true\n" +
                "invalid_yaml_without_indentation: value\n";
            Files.write(configFile, malformedYaml.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should either handle gracefully or throw meaningful exception
            assertThrows(DynamicQueryException.class, () -> {
                loader.loadFromFile(configFile).build();
            }, "Malformed YAML should throw meaningful exception");
        }

        @Test
        @DisplayName("should handle empty configuration file")
        void shouldHandleEmptyConfigurationFile() throws IOException {
            Path configFile = tempDir.resolve("empty.properties");
            Files.write(configFile, "".getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Should use all defaults
                assertNotNull(config);
                assertNotNull(config.getCore());
                assertNotNull(config.getCache());
            });
        }
    }

    @Nested
    @DisplayName("File System Errors")
    class FileSystemErrorTests {

        @Test
        @DisplayName("should handle nonexistent configuration file")
        void shouldHandleNonexistentConfigurationFile() {
            Path nonexistentFile = tempDir.resolve("nonexistent.properties");
            
            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should throw meaningful exception for missing file
            DynamicQueryException exception = assertThrows(DynamicQueryException.class, () -> {
                loader.loadFromFile(nonexistentFile).build();
            });
            
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("nonexistent.properties"));
        }

        @Test
        @DisplayName("should handle directory instead of file")
        void shouldHandleDirectoryInsteadOfFile() throws IOException {
            Path directory = tempDir.resolve("config-dir");
            Files.createDirectory(directory);
            
            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should throw meaningful exception
            DynamicQueryException exception = assertThrows(DynamicQueryException.class, () -> {
                loader.loadFromFile(directory).build();
            });
            
            assertNotNull(exception.getMessage());
            assertTrue(exception.getCause() != null || exception.getMessage().contains("directory"));
        }

        @Test
        @DisplayName("should handle unreadable file permissions")
        void shouldHandleUnreadableFilePermissions() throws IOException {
            Path configFile = tempDir.resolve("unreadable.properties");
            Files.write(configFile, "query4j.core.defaultQueryTimeoutMs=5000\n".getBytes());
            
            // Make file unreadable (may not work on all systems)
            configFile.toFile().setReadable(false);
            
            ConfigurationLoader loader = new ConfigurationLoader();
            
            // Should handle permission error gracefully
            try {
                DynamicQueryException exception = assertThrows(DynamicQueryException.class, () -> {
                    loader.loadFromFile(configFile).build();
                });
                
                assertNotNull(exception.getMessage());
            } finally {
                // Restore permissions for cleanup
                configFile.toFile().setReadable(true);
            }
        }
    }

    @Nested
    @DisplayName("System Property Conflicts")
    class SystemPropertyConflictTests {

        @Test
        @DisplayName("should handle conflicting system properties and file configurations")
        void shouldHandleConflictingSystemPropertiesAndFileConfigurations() throws IOException {
            // Set system property
            System.setProperty("query4j.core.defaultQueryTimeoutMs", "10000");
            
            // Create file with different value
            Path configFile = tempDir.resolve("conflicting.properties");
            String propertiesContent = "query4j.core.defaultQueryTimeoutMs=20000\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            Query4jConfig config = loader.loadFromFile(configFile).build();
            
            // System properties should take precedence (or vice versa - depends on implementation)
            long timeout = config.getCore().getDefaultQueryTimeoutMs();
            assertTrue(timeout == 10000L || timeout == 20000L, 
                "Should use either system property or file value consistently");
        }

        @Test
        @DisplayName("should handle invalid system property values")
        void shouldHandleInvalidSystemPropertyValues() {
            // Set invalid system properties
            System.setProperty("query4j.core.defaultQueryTimeoutMs", "not_a_number");
            System.setProperty("query4j.core.maxPredicateDepth", "invalid");
            System.setProperty("query4j.cache.enabled", "maybe");

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.load().build();
                // Should fall back to defaults for invalid system properties
                assertNotNull(config);
                assertTrue(config.getCore().getDefaultQueryTimeoutMs() > 0);
                assertTrue(config.getCore().getMaxPredicateDepth() > 0);
            });
        }
    }

    @Nested
    @DisplayName("Configuration Validation")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("should validate configuration consistency")
        void shouldValidateConfigurationConsistency() throws IOException {
            // Create configuration with potentially inconsistent values
            Path configFile = tempDir.resolve("inconsistent.properties");
            String propertiesContent = 
                "query4j.core.defaultQueryTimeoutMs=1\n" +  // Very short timeout
                "query4j.core.maxPredicateDepth=1000\n" +   // Very deep predicates
                "query4j.cache.enabled=true\n" +
                "query4j.cache.maxSize=1\n";               // Very small cache
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Configuration should be internally consistent
                assertNotNull(config);
                
                // Values might be adjusted for consistency
                assertTrue(config.getCore().getDefaultQueryTimeoutMs() > 0);
                assertTrue(config.getCore().getMaxPredicateDepth() > 0);
                if (config.getCache().isEnabled()) {
                    assertTrue(config.getCache().getMaxSize() > 0);
                }
            });
        }

        @Test
        @DisplayName("should handle unknown configuration properties")
        void shouldHandleUnknownConfigurationProperties() throws IOException {
            Path configFile = tempDir.resolve("unknown-props.properties");
            String propertiesContent = 
                "query4j.core.defaultQueryTimeoutMs=5000\n" +
                "query4j.unknown.property=somevalue\n" +
                "query4j.cache.unknownSetting=true\n" +
                "completely.unrelated.property=value\n";
            Files.write(configFile, propertiesContent.getBytes());

            ConfigurationLoader loader = new ConfigurationLoader();
            
            assertDoesNotThrow(() -> {
                Query4jConfig config = loader.loadFromFile(configFile).build();
                // Should ignore unknown properties and use known ones
                assertNotNull(config);
                assertEquals(5000L, config.getCore().getDefaultQueryTimeoutMs());
            });
        }
    }

    @Nested
    @DisplayName("Configuration Factory Failures")
    class ConfigurationFactoryFailureTests {

        @Test
        @DisplayName("should handle factory method failures")
        void shouldHandleFactoryMethodFailures() {
            // Test factory methods with invalid inputs using CoreConfig and CacheConfig builders
            // Invalid configurations should build but fail during validation
            assertThrows(IllegalStateException.class, () -> {
                CoreConfig config = CoreConfig.builder()
                    .defaultQueryTimeoutMs(-1L)
                    .maxPredicateDepth(-1)
                    .build();
                config.validate(); // Should fail here
            }, "Invalid core config parameters should be rejected during validation");
                
            assertThrows(IllegalStateException.class, () -> {
                CacheConfig config = CacheConfig.builder()
                    .enabled(false)
                    .maxSize(-1L)
                    .defaultTtlSeconds(-1L)
                    .build();
                config.validate(); // Should fail here
            }, "Invalid cache config parameters should be rejected during validation");
        }

        @Test
        @DisplayName("should validate builder state")
        void shouldValidateBuilderState() {
            Query4jConfig.Query4jConfigBuilder builder = Query4jConfig.builder();
            
            // Test building with invalid state
            assertDoesNotThrow(() -> {
                Query4jConfig config = builder.build();
                // Should provide defaults even with empty builder
                assertNotNull(config);
                assertNotNull(config.getCore());
                assertNotNull(config.getCache());
            });
        }
    }
}