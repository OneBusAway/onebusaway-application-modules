/**
 * Copyright (C) 2024 OneBusAway
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.cli.apikey;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Command-line tool for managing OneBusAway API keys.
 *
 * Supports the following commands:
 * - create: Create a new API key
 * - list: List all API keys
 * - get: Get details for a specific key
 * - update: Update an existing key
 * - delete: Delete an API key
 */
public class ApiKeyCliMain {

    private static final String DEFAULT_MIN_API_REQ_INT = "100";
    static final int MAX_KEY_LENGTH = 128;
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.]+$");

    private ConfigurableApplicationContext context;
    private UserService userService;
    private UserPropertiesService userPropertiesService;

    /**
     * Sets the UserService (for testing).
     */
    void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Sets the UserPropertiesService (for testing).
     */
    void setUserPropertiesService(UserPropertiesService userPropertiesService) {
        this.userPropertiesService = userPropertiesService;
    }

    public static void main(String[] args) {
        ApiKeyCliMain cli = new ApiKeyCliMain();
        int exitCode = 0;
        try {
            cli.run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            exitCode = 1;
        } finally {
            cli.closeContext();
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public void run(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];
        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);

        Options options = buildOptions(command);

        if ("help".equals(command) || "--help".equals(command) || "-h".equals(command)) {
            printUsage();
            return;
        }

        CommandLine cli;
        try {
            cli = new GnuParser().parse(options, remainingArgs);
        } catch (ParseException e) {
            System.err.println("Error parsing arguments: " + e.getMessage());
            printCommandHelp(command, options);
            throw new IllegalArgumentException("Invalid arguments");
        }

        String configPath = cli.getOptionValue("config");
        if (configPath == null) {
            System.err.println("Error: --config is required");
            printCommandHelp(command, options);
            throw new IllegalArgumentException("Missing required --config option");
        }

        initializeContext(configPath);

        switch (command) {
            case "create":
                doCreate(cli);
                break;
            case "list":
                doList();
                break;
            case "get":
                doGet(cli);
                break;
            case "update":
                doUpdate(cli);
                break;
            case "delete":
                doDelete(cli);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                throw new IllegalArgumentException("Unknown command: " + command);
        }
    }

    private void closeContext() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                // Log but don't fail on close errors
                System.err.println("Warning: Error closing context: " + e.getMessage());
            }
        }
    }

    private Options buildOptions(String command) {
        Options options = new Options();

        Option configOption = new Option("c", "config", true, "Path to data-sources.xml (required)");
        configOption.setRequired(false); // We'll check manually for better error messages
        options.addOption(configOption);

        switch (command) {
            case "create":
                options.addOption(new Option("k", "key", true, "API key value (optional, UUID generated if not provided)"));
                options.addOption(new Option("n", "name", true, "Contact name"));
                options.addOption(new Option("o", "company", true, "Contact company"));
                options.addOption(new Option("e", "email", true, "Contact email"));
                options.addOption(new Option("d", "details", true, "Contact details"));
                options.addOption(new Option("m", "minApiReqInt", true, "Minimum API request interval in ms (default: " + DEFAULT_MIN_API_REQ_INT + ")"));
                break;
            case "get":
            case "delete":
                Option keyOption = new Option("k", "key", true, "API key value (required)");
                options.addOption(keyOption);
                break;
            case "update":
                options.addOption(new Option("k", "key", true, "API key value (required)"));
                options.addOption(new Option("n", "name", true, "Contact name"));
                options.addOption(new Option("o", "company", true, "Contact company"));
                options.addOption(new Option("e", "email", true, "Contact email"));
                options.addOption(new Option("d", "details", true, "Contact details"));
                options.addOption(new Option("m", "minApiReqInt", true, "Minimum API request interval in ms"));
                break;
            case "list":
                // No additional options
                break;
        }

        return options;
    }

    private void initializeContext(String configPath) {
        // Skip initialization if services are already set (for testing)
        if (userService != null && userPropertiesService != null) {
            return;
        }

        context = ContainerLibrary.createContext(
            "classpath:org/onebusaway/users/application-context.xml",
            "classpath:org/onebusaway/cli/apikey/application-context-cli.xml",
            "file:" + configPath
        );

        userService = context.getBean(UserService.class);
        userPropertiesService = context.getBean(UserPropertiesService.class);
    }

    /**
     * Validates an API key value.
     * Keys must be alphanumeric with dashes, underscores, and dots allowed.
     * Maximum length is 128 characters.
     */
    void validateKeyValue(String keyValue) {
        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("API key cannot be empty");
        }
        if (keyValue.length() > MAX_KEY_LENGTH) {
            throw new IllegalArgumentException("API key cannot exceed " + MAX_KEY_LENGTH + " characters");
        }
        if (!VALID_KEY_PATTERN.matcher(keyValue).matches()) {
            throw new IllegalArgumentException("API key contains invalid characters. Only alphanumeric characters, dashes, underscores, and dots are allowed.");
        }
    }

    /**
     * Validates the minimum API request interval.
     * Must be a non-negative number.
     */
    long parseAndValidateMinApiReqInt(String minApiReqIntStr) {
        long minApiReqInt;
        try {
            minApiReqInt = Long.parseLong(minApiReqIntStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("minApiReqInt must be a number");
        }
        if (minApiReqInt < 0) {
            throw new IllegalArgumentException("minApiReqInt cannot be negative");
        }
        return minApiReqInt;
    }

    private void doCreate(CommandLine cli) throws Exception {
        String keyValue = cli.getOptionValue("key");
        if (keyValue == null || keyValue.isEmpty()) {
            keyValue = UUID.randomUUID().toString();
        } else {
            // Trim whitespace and validate
            keyValue = keyValue.trim();
            validateKeyValue(keyValue);
        }

        String name = cli.getOptionValue("name", "");
        String company = cli.getOptionValue("company", "");
        String email = cli.getOptionValue("email", "");
        String details = cli.getOptionValue("details", "");
        String minApiReqIntStr = cli.getOptionValue("minApiReqInt", DEFAULT_MIN_API_REQ_INT);

        long minApiReqInt = parseAndValidateMinApiReqInt(minApiReqIntStr);

        // Check if key already exists
        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex existingIndex = userService.getUserIndexForId(indexKey);
        if (existingIndex != null) {
            throw new IllegalArgumentException("API key '" + keyValue + "' already exists");
        }

        // Create the key
        UserIndex userIndex = userService.getOrCreateUserForIndexKey(indexKey, "", true);
        userPropertiesService.authorizeApi(userIndex.getUser(), minApiReqInt);

        User user = userIndex.getUser();
        userPropertiesService.updateApiKeyContactInfo(user, name, company, email, details);

        // Clear cache
        userService.getMinApiRequestIntervalForKey(keyValue, true);

        System.out.println("API key created successfully: " + keyValue);
    }

    private void doList() throws Exception {
        List<String> apiKeys = userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);

        if (apiKeys.isEmpty()) {
            System.out.println("No API keys found");
            return;
        }

        System.out.println("API Keys (" + apiKeys.size() + " total):");
        System.out.println("----------------------------------------");
        for (String key : apiKeys) {
            System.out.println(key);
        }
    }

    private void doGet(CommandLine cli) throws Exception {
        String keyValue = cli.getOptionValue("key");
        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("--key is required");
        }

        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex userIndex = userService.getUserIndexForId(indexKey);

        if (userIndex == null) {
            throw new IllegalArgumentException("API key '" + keyValue + "' not found");
        }

        User user = userIndex.getUser();
        UserBean bean = userService.getUserAsBean(user);

        System.out.println("API Key Details:");
        System.out.println("----------------------------------------");
        System.out.println("Key:                  " + keyValue);
        System.out.println("Contact Name:         " + nullSafe(bean.getContactName()));
        System.out.println("Contact Company:      " + nullSafe(bean.getContactCompany()));
        System.out.println("Contact Email:        " + nullSafe(bean.getContactEmail()));
        System.out.println("Contact Details:      " + nullSafe(bean.getContactDetails()));
        System.out.println("Min API Req Interval: " + bean.getMinApiRequestInterval() + " ms");
    }

    private void doUpdate(CommandLine cli) throws Exception {
        String keyValue = cli.getOptionValue("key");
        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("--key is required");
        }

        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex userIndex = userService.getUserIndexForId(indexKey);

        if (userIndex == null) {
            throw new IllegalArgumentException("API key '" + keyValue + "' not found");
        }

        User user = userIndex.getUser();
        UserBean bean = userService.getUserAsBean(user);

        // Get current values and override with provided options
        String name = cli.hasOption("name") ? cli.getOptionValue("name") : bean.getContactName();
        String company = cli.hasOption("company") ? cli.getOptionValue("company") : bean.getContactCompany();
        String email = cli.hasOption("email") ? cli.getOptionValue("email") : bean.getContactEmail();
        String details = cli.hasOption("details") ? cli.getOptionValue("details") : bean.getContactDetails();

        long minApiReqInt = bean.getMinApiRequestInterval();
        if (cli.hasOption("minApiReqInt")) {
            minApiReqInt = parseAndValidateMinApiReqInt(cli.getOptionValue("minApiReqInt"));
        }

        // Update
        userPropertiesService.authorizeApi(user, minApiReqInt);
        userPropertiesService.updateApiKeyContactInfo(user, name, company, email, details);

        // Clear cache
        userService.getMinApiRequestIntervalForKey(keyValue, true);

        System.out.println("API key updated successfully: " + keyValue);
    }

    private void doDelete(CommandLine cli) throws Exception {
        String keyValue = cli.getOptionValue("key");
        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("--key is required");
        }

        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex userIndex = userService.getUserIndexForId(indexKey);

        if (userIndex == null) {
            throw new IllegalArgumentException("API key '" + keyValue + "' not found");
        }

        User user = userIndex.getUser();

        // Find and remove the user index
        UserIndex targetIndex = null;
        for (UserIndex index : user.getUserIndices()) {
            if (index.getId().getValue().equalsIgnoreCase(indexKey.getValue())) {
                targetIndex = index;
                break;
            }
        }

        if (targetIndex == null) {
            throw new IllegalArgumentException("API key '" + keyValue + "' not found (no exact match)");
        }

        // Get the count of indices before removal
        int indicesCountBefore = user.getUserIndices().size();

        userService.removeUserIndexForUser(user, targetIndex.getId());

        // Delete user if this was the only index
        if (indicesCountBefore == 1) {
            userService.deleteUser(user);
        }

        // Clear cache
        try {
            userService.getMinApiRequestIntervalForKey(keyValue, true);
        } catch (Exception e) {
            // Ignore - key is deleted
        }

        System.out.println("API key deleted successfully: " + keyValue);
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private void printUsage() {
        System.out.println("OneBusAway API Key CLI");
        System.out.println();
        System.out.println("Usage: java -jar onebusaway-api-key-cli.jar <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  create   Create a new API key");
        System.out.println("  list     List all API keys");
        System.out.println("  get      Get details for a specific key");
        System.out.println("  update   Update an existing key");
        System.out.println("  delete   Delete an API key");
        System.out.println("  help     Show this help message");
        System.out.println();
        System.out.println("Global Options:");
        System.out.println("  -c, --config <path>   Path to data-sources.xml (required)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar onebusaway-api-key-cli.jar create --config /path/to/data-sources.xml --key my-key --email user@example.com");
        System.out.println("  java -jar onebusaway-api-key-cli.jar list --config /path/to/data-sources.xml");
        System.out.println("  java -jar onebusaway-api-key-cli.jar get --config /path/to/data-sources.xml --key my-key");
        System.out.println("  java -jar onebusaway-api-key-cli.jar update --config /path/to/data-sources.xml --key my-key --email new@example.com");
        System.out.println("  java -jar onebusaway-api-key-cli.jar delete --config /path/to/data-sources.xml --key my-key");
    }

    private void printCommandHelp(String command, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar onebusaway-api-key-cli.jar " + command, options);
    }
}
