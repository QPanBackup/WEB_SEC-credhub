package org.cloudfoundry.credhub.controllers.autodocs.v1.keyusage

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
import org.cloudfoundry.credhub.keyusage.KeyUsageController
import org.cloudfoundry.credhub.services.EncryptionKeySet
import org.cloudfoundry.credhub.services.SpyCredentialVersionDataService
import org.cloudfoundry.credhub.testhelpers.CredHubRestDocs
import org.cloudfoundry.credhub.testhelpers.MockMvcFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.http.MediaType
import org.springframework.restdocs.JUnitRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.security.Security

class KeyUsageControllerTest {

    @Rule
    @JvmField
    val restDocumentation = JUnitRestDocumentation()

    lateinit var mockMvc: MockMvc
    lateinit var keyUsageHandler: SpyKeyUsageHandler

    @Before
    fun setUp() {


            val keyUsageController = KeyUsageController(keyUsageHandler)

        mockMvc = MockMvcFactory.newSpringRestDocMockMvc(keyUsageController, restDocumentation)

        if (Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleFipsProvider())
        }
    }

    @Test
    fun GET__keyusage__returns_map(){
        // language=json
        val responseBody = """
            {
              "active_key": 10,
              "inactive_keys": 2,
              "unknown_keys": 1
            }
        """.trimIndent()
        val objectMapper = ObjectMapper()
        val map = objectMapper.readValue(responseBody, Map::class.java) as Map<String, Long>
        keyUsageHandler.getKeyUsage__returns_results = map

        val mvcResult = mockMvc.perform(
                get(KeyUsageController.ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer [some-token]")
                ).andExpect(status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andDo(
                        MockMvcRestDocumentation.document(
                                CredHubRestDocs.DOCUMENT_IDENTIFIER
                        )
                ).andReturn()

        JSONAssert.assertEquals(mvcResult.response.contentAsString, responseBody, true)
    }
}
