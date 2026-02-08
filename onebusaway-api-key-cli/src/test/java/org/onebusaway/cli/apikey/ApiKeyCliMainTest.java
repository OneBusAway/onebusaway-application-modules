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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;

/**
 * Unit tests for ApiKeyCliMain.
 */
public class ApiKeyCliMainTest {

    private ApiKeyCliMain cli;
    private UserService userService;
    private UserPropertiesService userPropertiesService;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @Before
    public void setUp() {
        cli = new ApiKeyCliMain();
        userService = mock(UserService.class);
        userPropertiesService = mock(UserPropertiesService.class);
        cli.setUserService(userService);
        cli.setUserPropertiesService(userPropertiesService);

        // Capture stdout and stderr
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // ==================== Key Validation Tests ====================

    @Test
    public void testValidateKeyValue_ValidAlphanumeric() {
        cli.validateKeyValue("abc123XYZ");
        // No exception means pass
    }

    @Test
    public void testValidateKeyValue_ValidWithDashes() {
        cli.validateKeyValue("my-api-key");
    }

    @Test
    public void testValidateKeyValue_ValidWithUnderscores() {
        cli.validateKeyValue("my_api_key");
    }

    @Test
    public void testValidateKeyValue_ValidWithDots() {
        cli.validateKeyValue("my.api.key");
    }

    @Test
    public void testValidateKeyValue_ValidMixedCharacters() {
        cli.validateKeyValue("my-api_key.v1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_ExceedsMaxLength() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= ApiKeyCliMain.MAX_KEY_LENGTH; i++) {
            sb.append("a");
        }
        cli.validateKeyValue(sb.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_Empty() {
        cli.validateKeyValue("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_Null() {
        cli.validateKeyValue(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_WithSpaces() {
        cli.validateKeyValue("my api key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_WithAtSymbol() {
        cli.validateKeyValue("user@domain");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_WithHashSymbol() {
        cli.validateKeyValue("key#123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateKeyValue_WithSlash() {
        cli.validateKeyValue("key/path");
    }

    // ==================== MinApiReqInt Validation Tests ====================

    @Test
    public void testParseAndValidateMinApiReqInt_ValidPositive() {
        long result = cli.parseAndValidateMinApiReqInt("100");
        assertEquals(100L, result);
    }

    @Test
    public void testParseAndValidateMinApiReqInt_Zero() {
        long result = cli.parseAndValidateMinApiReqInt("0");
        assertEquals(0L, result);
    }

    @Test
    public void testParseAndValidateMinApiReqInt_LargeNumber() {
        long result = cli.parseAndValidateMinApiReqInt("999999999");
        assertEquals(999999999L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidateMinApiReqInt_Negative() {
        cli.parseAndValidateMinApiReqInt("-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidateMinApiReqInt_NonNumeric() {
        cli.parseAndValidateMinApiReqInt("abc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidateMinApiReqInt_Empty() {
        cli.parseAndValidateMinApiReqInt("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidateMinApiReqInt_DecimalNumber() {
        cli.parseAndValidateMinApiReqInt("100.5");
    }

    // ==================== CLI Parsing Tests ====================

    @Test
    public void testRun_NoArguments_PrintsUsage() throws Exception {
        cli.run(new String[]{});
        String output = outContent.toString();
        assertTrue(output.contains("OneBusAway API Key CLI"));
        assertTrue(output.contains("Usage:"));
    }

    @Test
    public void testRun_HelpCommand_PrintsUsage() throws Exception {
        cli.run(new String[]{"help"});
        String output = outContent.toString();
        assertTrue(output.contains("OneBusAway API Key CLI"));
    }

    @Test
    public void testRun_HelpFlag_PrintsUsage() throws Exception {
        cli.run(new String[]{"--help"});
        String output = outContent.toString();
        assertTrue(output.contains("OneBusAway API Key CLI"));
    }

    @Test
    public void testRun_ShortHelpFlag_PrintsUsage() throws Exception {
        cli.run(new String[]{"-h"});
        String output = outContent.toString();
        assertTrue(output.contains("OneBusAway API Key CLI"));
    }

    @Test
    public void testRun_UnknownCommand_ThrowsException() {
        try {
            cli.run(new String[]{"unknowncommand", "--config", "/tmp/data-sources.xml"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Unknown command"));
        }
    }

    @Test
    public void testRun_MissingConfig_ThrowsException() {
        try {
            cli.run(new String[]{"list"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Missing required --config"));
        }
    }

    // ==================== Create Command Tests ====================

    @Test
    public void testCreate_NewKey_Success() throws Exception {
        String keyValue = "test-key-123";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);
        when(userService.getOrCreateUserForIndexKey(any(UserIndexKey.class), eq("test-key-123"), eq(false)))
            .thenReturn(mockUserIndex);

        cli.run(new String[]{"create", "--config", "/tmp/data-sources.xml", "--key", keyValue});

        verify(userService).getOrCreateUserForIndexKey(any(UserIndexKey.class), eq("test-key-123"), eq(false));
        verify(userPropertiesService).authorizeApi(eq(mockUser), anyLong());
        verify(userPropertiesService).updateApiKeyContactInfo(eq(mockUser), anyString(), anyString(), anyString(), anyString());
        assertTrue(outContent.toString().contains("API key created successfully"));
    }

    @Test
    public void testCreate_WithContactInfo_Success() throws Exception {
        String keyValue = "test-key-456";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);
        when(userService.getOrCreateUserForIndexKey(any(UserIndexKey.class), eq("test-key-456"), eq(false)))
            .thenReturn(mockUserIndex);

        cli.run(new String[]{
            "create", "--config", "/tmp/data-sources.xml",
            "--key", keyValue,
            "--name", "John Doe",
            "--email", "john@example.com",
            "--company", "ACME Corp",
            "--details", "Test user",
            "--minApiReqInt", "200"
        });

        verify(userPropertiesService).authorizeApi(eq(mockUser), eq(200L));
        verify(userPropertiesService).updateApiKeyContactInfo(eq(mockUser), eq("John Doe"), eq("ACME Corp"), eq("john@example.com"), eq("Test user"));
    }

    @Test
    public void testCreate_ExistingKey_ThrowsException() {
        String keyValue = "existing-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);

        try {
            cli.run(new String[]{"create", "--config", "/tmp/data-sources.xml", "--key", keyValue});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("already exists"));
        }

        verify(userService, never()).getOrCreateUserForIndexKey(any(), any(), anyBoolean());
    }

    @Test
    public void testCreate_InvalidKeyFormat_ThrowsException() {
        try {
            cli.run(new String[]{"create", "--config", "/tmp/data-sources.xml", "--key", "invalid key!"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("invalid characters"));
        }
    }

    // ==================== List Command Tests ====================

    @Test
    public void testList_WithKeys_PrintsAll() throws Exception {
        when(userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY))
            .thenReturn(Arrays.asList("key1", "key2", "key3"));

        cli.run(new String[]{"list", "--config", "/tmp/data-sources.xml"});

        String output = outContent.toString();
        assertTrue(output.contains("API Keys (3 total)"));
        assertTrue(output.contains("key1"));
        assertTrue(output.contains("key2"));
        assertTrue(output.contains("key3"));
    }

    @Test
    public void testList_NoKeys_PrintsMessage() throws Exception {
        when(userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY))
            .thenReturn(Collections.emptyList());

        cli.run(new String[]{"list", "--config", "/tmp/data-sources.xml"});

        assertTrue(outContent.toString().contains("No API keys found"));
    }

    // ==================== Get Command Tests ====================

    @Test
    public void testGet_ExistingKey_PrintsDetails() throws Exception {
        String keyValue = "my-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();
        UserBean mockBean = new UserBean();
        mockBean.setContactName("Jane Doe");
        mockBean.setContactCompany("Widget Inc");
        mockBean.setContactEmail("jane@widget.com");
        mockBean.setContactDetails("Production key");
        mockBean.setMinApiRequestInterval(500L);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);
        when(userService.getUserAsBean(mockUser)).thenReturn(mockBean);

        cli.run(new String[]{"get", "--config", "/tmp/data-sources.xml", "--key", keyValue});

        String output = outContent.toString();
        assertTrue(output.contains("API Key Details"));
        assertTrue(output.contains("Key:"));
        assertTrue(output.contains("Jane Doe"));
        assertTrue(output.contains("Widget Inc"));
        assertTrue(output.contains("jane@widget.com"));
        assertTrue(output.contains("500 ms"));
    }

    @Test
    public void testGet_NonExistentKey_ThrowsException() {
        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);

        try {
            cli.run(new String[]{"get", "--config", "/tmp/data-sources.xml", "--key", "nonexistent"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void testGet_MissingKey_ThrowsException() {
        try {
            cli.run(new String[]{"get", "--config", "/tmp/data-sources.xml"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("--key is required"));
        }
    }

    // ==================== Update Command Tests ====================

    @Test
    public void testUpdate_ExistingKey_Success() throws Exception {
        String keyValue = "update-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();
        UserBean mockBean = new UserBean();
        mockBean.setContactName("Old Name");
        mockBean.setContactCompany("Old Company");
        mockBean.setContactEmail("old@email.com");
        mockBean.setContactDetails("Old details");
        mockBean.setMinApiRequestInterval(100L);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);
        when(userService.getUserAsBean(mockUser)).thenReturn(mockBean);

        cli.run(new String[]{
            "update", "--config", "/tmp/data-sources.xml",
            "--key", keyValue,
            "--name", "New Name",
            "--email", "new@email.com"
        });

        // Should preserve old values for unspecified options
        verify(userPropertiesService).updateApiKeyContactInfo(
            eq(mockUser), eq("New Name"), eq("Old Company"), eq("new@email.com"), eq("Old details")
        );
        assertTrue(outContent.toString().contains("API key updated successfully"));
    }

    @Test
    public void testUpdate_NonExistentKey_ThrowsException() {
        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);

        try {
            cli.run(new String[]{"update", "--config", "/tmp/data-sources.xml", "--key", "nonexistent"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void testUpdate_MissingKey_ThrowsException() {
        try {
            cli.run(new String[]{"update", "--config", "/tmp/data-sources.xml"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("--key is required"));
        }
    }

    // ==================== Delete Command Tests ====================

    @Test
    public void testDelete_ExistingKey_OnlyIndex_DeletesUser() throws Exception {
        String keyValue = "delete-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);

        cli.run(new String[]{"delete", "--config", "/tmp/data-sources.xml", "--key", keyValue});

        verify(userService).removeUserIndexForUser(eq(mockUser), any(UserIndexKey.class));
        verify(userService).deleteUser(mockUser);
        assertTrue(outContent.toString().contains("API key deleted successfully"));
    }

    @Test
    public void testDelete_ExistingKey_MultipleIndices_KeepsUser() throws Exception {
        String keyValue = "delete-key";
        User mockUser = mock(User.class);
        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex mockUserIndex = mock(UserIndex.class);
        when(mockUserIndex.getId()).thenReturn(indexKey);
        when(mockUserIndex.getUser()).thenReturn(mockUser);

        // Create another index for the same user
        UserIndex anotherIndex = mock(UserIndex.class);
        UserIndexKey anotherKey = new UserIndexKey(UserIndexTypes.API_KEY, "another-key");
        when(anotherIndex.getId()).thenReturn(anotherKey);

        Set<UserIndex> indices = new HashSet<>();
        indices.add(mockUserIndex);
        indices.add(anotherIndex);
        when(mockUser.getUserIndices()).thenReturn(indices);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);

        cli.run(new String[]{"delete", "--config", "/tmp/data-sources.xml", "--key", keyValue});

        verify(userService).removeUserIndexForUser(eq(mockUser), any(UserIndexKey.class));
        verify(userService, never()).deleteUser(any(User.class));
    }

    @Test
    public void testDelete_NonExistentKey_ThrowsException() {
        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);

        try {
            cli.run(new String[]{"delete", "--config", "/tmp/data-sources.xml", "--key", "nonexistent"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void testDelete_MissingKey_ThrowsException() {
        try {
            cli.run(new String[]{"delete", "--config", "/tmp/data-sources.xml"});
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("--key is required"));
        }
    }

    // ==================== JSON Output Tests ====================

    @Test
    public void testCreate_JsonOutput_Success() throws Exception {
        String keyValue = "json-test-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(null);
        when(userService.getOrCreateUserForIndexKey(any(UserIndexKey.class), eq("json-test-key"), eq(false)))
            .thenReturn(mockUserIndex);

        cli.run(new String[]{
            "create", "--config", "/tmp/data-sources.xml",
            "--key", keyValue,
            "--name", "Test User",
            "--email", "test@example.com",
            "--json"
        });

        String output = outContent.toString();
        assertTrue(output.contains("\"success\" : true"));
        assertTrue(output.contains("\"key\" : \"json-test-key\""));
        assertTrue(output.contains("\"contactName\" : \"Test User\""));
        assertTrue(output.contains("\"contactEmail\" : \"test@example.com\""));
    }

    @Test
    public void testList_JsonOutput_WithKeys() throws Exception {
        when(userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY))
            .thenReturn(Arrays.asList("key1", "key2", "key3"));

        cli.run(new String[]{"list", "--config", "/tmp/data-sources.xml", "--json"});

        String output = outContent.toString();
        assertTrue(output.contains("\"total\" : 3"));
        assertTrue(output.contains("\"keys\""));
        assertTrue(output.contains("\"key1\""));
        assertTrue(output.contains("\"key2\""));
        assertTrue(output.contains("\"key3\""));
    }

    @Test
    public void testList_JsonOutput_Empty() throws Exception {
        when(userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY))
            .thenReturn(Collections.emptyList());

        cli.run(new String[]{"list", "--config", "/tmp/data-sources.xml", "--json"});

        String output = outContent.toString();
        assertTrue(output.contains("\"total\" : 0"));
        assertTrue(output.contains("\"keys\" : [ ]"));
    }

    @Test
    public void testGet_JsonOutput_ExistingKey() throws Exception {
        String keyValue = "my-json-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();
        UserBean mockBean = new UserBean();
        mockBean.setContactName("Jane Doe");
        mockBean.setContactCompany("Widget Inc");
        mockBean.setContactEmail("jane@widget.com");
        mockBean.setContactDetails("Production key");
        mockBean.setMinApiRequestInterval(500L);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);
        when(userService.getUserAsBean(mockUser)).thenReturn(mockBean);

        cli.run(new String[]{"get", "--config", "/tmp/data-sources.xml", "--key", keyValue, "--json"});

        String output = outContent.toString();
        assertTrue(output.contains("\"key\" : \"my-json-key\""));
        assertTrue(output.contains("\"contactName\" : \"Jane Doe\""));
        assertTrue(output.contains("\"contactCompany\" : \"Widget Inc\""));
        assertTrue(output.contains("\"contactEmail\" : \"jane@widget.com\""));
        assertTrue(output.contains("\"minApiRequestInterval\" : 500"));
    }

    @Test
    public void testUpdate_JsonOutput_Success() throws Exception {
        String keyValue = "update-json-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();
        UserBean mockBean = new UserBean();
        mockBean.setContactName("Old Name");
        mockBean.setContactCompany("Old Company");
        mockBean.setContactEmail("old@email.com");
        mockBean.setContactDetails("Old details");
        mockBean.setMinApiRequestInterval(100L);

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);
        when(userService.getUserAsBean(mockUser)).thenReturn(mockBean);

        cli.run(new String[]{
            "update", "--config", "/tmp/data-sources.xml",
            "--key", keyValue,
            "--name", "New Name",
            "--json"
        });

        String output = outContent.toString();
        assertTrue(output.contains("\"success\" : true"));
        assertTrue(output.contains("\"key\" : \"update-json-key\""));
        assertTrue(output.contains("\"contactName\" : \"New Name\""));
    }

    @Test
    public void testDelete_JsonOutput_Success() throws Exception {
        String keyValue = "delete-json-key";
        UserIndex mockUserIndex = createMockUserIndex(keyValue);
        User mockUser = mockUserIndex.getUser();

        when(userService.getUserIndexForId(any(UserIndexKey.class))).thenReturn(mockUserIndex);

        cli.run(new String[]{"delete", "--config", "/tmp/data-sources.xml", "--key", keyValue, "--json"});

        String output = outContent.toString();
        assertTrue(output.contains("\"success\" : true"));
        assertTrue(output.contains("\"message\" : \"API key deleted successfully\""));
        assertTrue(output.contains("\"key\" : \"delete-json-key\""));
    }

    @Test
    public void testList_JsonOutput_ShortFlag() throws Exception {
        when(userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY))
            .thenReturn(Arrays.asList("key1"));

        cli.run(new String[]{"list", "--config", "/tmp/data-sources.xml", "-j"});

        String output = outContent.toString();
        assertTrue(output.contains("\"total\" : 1"));
        assertTrue(output.contains("\"keys\""));
    }

    // ==================== Helper Methods ====================

    private UserIndex createMockUserIndex(String keyValue) {
        User mockUser = mock(User.class);
        UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, keyValue);
        UserIndex mockUserIndex = mock(UserIndex.class);
        when(mockUserIndex.getId()).thenReturn(indexKey);
        when(mockUserIndex.getUser()).thenReturn(mockUser);

        Set<UserIndex> indices = new HashSet<>();
        indices.add(mockUserIndex);
        when(mockUser.getUserIndices()).thenReturn(indices);

        return mockUserIndex;
    }
}
