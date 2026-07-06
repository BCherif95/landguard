package com.laboussole.infrastructure.notification;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

/**
 * Integration test of the Twilio HTTP adapter: exercises the real request
 * building (URL, basic auth, form body) against a mock HTTP server bound to
 * the adapter's RestClient.
 */
class TwilioSmsAdapterTest {

    private static final TwilioProperties PROPERTIES = new TwilioProperties(
            "ACtest123", "secret-token", "+15005550006", "https://api.twilio.com");

    @Test
    void postsFormEncodedMessageWithBasicAuthToTwilio() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new TwilioSmsAdapter(PROPERTIES, builder);

        server.expect(requestTo("https://api.twilio.com/2010-04-01/Accounts/ACtest123/Messages.json"))
                .andExpect(method(POST))
                // Basic base64("ACtest123:secret-token")
                .andExpect(header("Authorization", "Basic QUN0ZXN0MTIzOnNlY3JldC10b2tlbg=="))
                .andExpect(content().formData(formData()))
                .andRespond(withSuccess("{\"sid\":\"SM1\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        assertThatCode(() -> adapter.send("+22370000000",
                "ALERTE LA BOUSSOLE : Activité suspecte détectée sur votre parcelle BSL-ML-2024-000123."
                        + " Consultez l'application ou appelez votre notaire."))
                .doesNotThrowAnyException();

        server.verify();
    }

    @Test
    void wrapsProviderErrorsInAlertDeliveryException() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new TwilioSmsAdapter(PROPERTIES, builder);

        server.expect(requestTo("https://api.twilio.com/2010-04-01/Accounts/ACtest123/Messages.json"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.send("+22370000000", "test"))
                .isInstanceOf(AlertDeliveryException.class)
                .hasMessageContaining("+22370000000");
    }

    @Test
    void failsLoudlyWhenCredentialsAreMissing() {
        var unconfigured = new TwilioProperties("", "", "", "https://api.twilio.com");
        var adapter = new TwilioSmsAdapter(unconfigured, RestClient.builder());

        assertThatThrownBy(() -> adapter.send("+22370000000", "test"))
                .isInstanceOf(AlertDeliveryException.class)
                .hasMessageContaining("not configured");
    }

    private static org.springframework.util.MultiValueMap<String, String> formData() {
        var form = new org.springframework.util.LinkedMultiValueMap<String, String>();
        form.add("To", "+22370000000");
        form.add("From", "+15005550006");
        form.add("Body",
                "ALERTE LA BOUSSOLE : Activité suspecte détectée sur votre parcelle BSL-ML-2024-000123."
                        + " Consultez l'application ou appelez votre notaire.");
        return form;
    }
}
