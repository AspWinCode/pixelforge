package studio.pixelforge.backend.storage;

import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpRequest;

// Для PutObject/UploadPart S3-модуль SDK сам добавляет заголовок
// "Expect: 100-continue" на уровне абстрактного запроса (см.
// StreamingRequestInterceptor) — это отдельный источник от Apache
// HTTP-клиента, который так же решает ждать continue на уровне сокета
// (отключается через ApacheHttpClient.Builder.expectContinueEnabled в
// S3Config). Наш MinIO не отвечает на continue вовремя, поэтому нужно
// убрать заголовок в обоих местах — здесь снимаем ту часть, что SDK
// проставляет сам, до того как запрос дойдёт до HTTP-клиента.
class DisableExpectContinueInterceptor implements ExecutionInterceptor {

    @Override
    public SdkHttpRequest modifyHttpRequest(Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
        return context.httpRequest().toBuilder().removeHeader("Expect").build();
    }
}
