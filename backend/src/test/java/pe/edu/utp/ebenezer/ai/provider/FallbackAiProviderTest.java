package pe.edu.utp.ebenezer.ai.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.edu.utp.ebenezer.ai.model.AiCompletion;
import pe.edu.utp.ebenezer.ai.model.AiCompletionRequest;
import pe.edu.utp.ebenezer.ai.model.AiMessage;
import pe.edu.utp.ebenezer.exception.AiUnavailableException;

class FallbackAiProviderTest {

    private static final AiCompletionRequest REQUEST =
            new AiCompletionRequest(List.of(AiMessage.user("¿Cuánto vendí hoy?")), List.of());

    private AiProvider openAi;
    private AiProvider openRouter;
    private FallbackAiProvider fallback;

    @BeforeEach
    void setUp() {
        openAi = mock(AiProvider.class);
        openRouter = mock(AiProvider.class);
        when(openAi.name()).thenReturn("openai");
        when(openRouter.name()).thenReturn("openrouter");
        fallback = new FallbackAiProvider(List.of(openAi, openRouter));
    }

    @Test
    void usesOpenAiWhenItAnswers() {
        when(openAi.isConfigured()).thenReturn(true);
        when(openAi.complete(any())).thenReturn(new AiCompletion("{}", List.of(), "openai"));

        AiCompletion completion = fallback.complete(REQUEST);

        assertThat(completion.provider()).isEqualTo("openai");
        verify(openRouter, never()).complete(any());
    }

    @Test
    void fallsBackToOpenRouterWhenOpenAiHasNoQuota() {
        when(openAi.isConfigured()).thenReturn(true);
        when(openRouter.isConfigured()).thenReturn(true);
        when(openAi.complete(any())).thenThrow(new AiProviderException("openai returned HTTP 429"));
        when(openRouter.complete(any())).thenReturn(new AiCompletion("{}", List.of(), "openrouter"));

        assertThat(fallback.complete(REQUEST).provider()).isEqualTo("openrouter");
    }

    @Test
    void skipsOpenAiWhenItHasNoKey() {
        when(openAi.isConfigured()).thenReturn(false);
        when(openRouter.isConfigured()).thenReturn(true);
        when(openRouter.complete(any())).thenReturn(new AiCompletion("{}", List.of(), "openrouter"));

        assertThat(fallback.complete(REQUEST).provider()).isEqualTo("openrouter");
        verify(openAi, never()).complete(any());
    }

    @Test
    void failsWhenEveryProviderFails() {
        when(openAi.isConfigured()).thenReturn(true);
        when(openRouter.isConfigured()).thenReturn(true);
        when(openAi.complete(any())).thenThrow(new AiProviderException("openai returned HTTP 500"));
        when(openRouter.complete(any())).thenThrow(new AiProviderException("openrouter timed out"));

        assertThatThrownBy(() -> fallback.complete(REQUEST)).isInstanceOf(AiUnavailableException.class);
    }

    @Test
    void failsWhenNoProviderIsConfigured() {
        assertThat(fallback.isConfigured()).isFalse();
        assertThatThrownBy(() -> fallback.complete(REQUEST))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("configured");
    }
}
