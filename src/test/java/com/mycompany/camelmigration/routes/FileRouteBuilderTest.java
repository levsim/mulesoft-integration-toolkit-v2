package com.mycompany.camelmigration.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@CamelSpringBootTest
// DirtiesContext to ensure properties are reset for each test if needed,
// especially when dealing with file paths and dynamic properties.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST)
class FileRouteBuilderTest {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @TempDir
    static Path sharedTempDir; // static so it's shared for @DynamicPropertySource

    static String inputDir;
    static String outputDir;
    static String processedDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        inputDir = sharedTempDir.resolve("input").toAbsolutePath().toString();
        outputDir = sharedTempDir.resolve("output").toAbsolutePath().toString();
        processedDir = sharedTempDir.resolve("processed").toAbsolutePath().toString();

        registry.add("file.input.directory", () -> inputDir);
        registry.add("file.output.directory", () -> outputDir);
        registry.add("file.processed.directory", () -> processedDir);
        
        // Create directories once properties are set for Camel context to pick up
        new File(inputDir).mkdirs();
        new File(outputDir).mkdirs();
        new File(processedDir).mkdirs();
    }
    
    @BeforeEach
    void setUp() throws Exception {
        // Ensure routes are started (CamelSpringBootTest usually handles this, but good for clarity)
        // camelContext.getRouteController().startAllRoutes();
    }

    @AfterEach
    void tearDown() {
        // Optional: Clean up files if needed, though @TempDir handles the base folder
    }

    @Test
    void testWriteFileEndpoint() throws Exception {
        String fileContent = "Hello Camel from testWriteFileEndpoint!";
        String response = producerTemplate.requestBody(
            "platform-http:/api/writefile",
            fileContent,
            String.class
        );

        assertEquals("{\"status\":\"File written successfully\"}", response);

        // Verify file creation in outputDir
        File outputDirectory = new File(outputDir);
        assertTrue(outputDirectory.exists() && outputDirectory.isDirectory(), "Output directory should exist.");
        File[] files = outputDirectory.listFiles((dir, name) -> name.startsWith("output-"));
        assertNotNull(files, "File list should not be null.");
        assertTrue(files.length > 0, "At least one output file should be created.");
        
        // Check content of the first file found (assuming only one is created by this test run)
        boolean foundAndVerified = false;
        for (File file : files) {
            if (Files.readString(file.toPath()).equals(fileContent)) {
                foundAndVerified = true;
                break;
            }
        }
        assertTrue(foundAndVerified, "Written file content does not match expected.");
    }

    @Test
    void testFileConsumerRoute() throws Exception {
        // Replace the final log endpoint with a mock to verify message processing
        AdviceWith.adviceWith(camelContext, "file-consumer", a -> {
            a.weaveByToString(".*log:fileProcessingOutput.*").replace().to("mock:fileProcessingOutput");
        });

        MockEndpoint mockProcessedEndpoint = camelContext.getEndpoint("mock:fileProcessingOutput", MockEndpoint.class);
        mockProcessedEndpoint.expectedMessageCount(1);
        mockProcessedEndpoint.expectedBodiesReceived("HELLO FROM TEST FILE CONSUMER!"); // Uppercase version

        // Create a test file in the input directory
        String testFileName = "test-input.txt";
        Path testFilePath = Paths.get(inputDir, testFileName);
        Files.writeString(testFilePath, "Hello from test file consumer!");

        // Wait for Camel to pick up and process the file
        mockProcessedEndpoint.assertIsSatisfied(10000); // Increased timeout for file polling

        // Verify the original file was moved
        Path processedFilePath = Paths.get(processedDir, testFileName);
        assertTrue(Files.notExists(testFilePath), "Original file should have been moved from input.");
        assertTrue(Files.exists(processedFilePath), "File should exist in the processed directory.");
        assertEquals("Hello from test file consumer!", Files.readString(processedFilePath), "Content of moved file should be original.");
    }
}
