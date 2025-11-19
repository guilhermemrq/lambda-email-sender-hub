package br.com.fiap.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Inject;

public class FeedbackReaderLambda implements RequestHandler<Object, String> {
    @Inject
    private QueueService queueService;

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Iniciando processamento da fila de feedbacks...");
        queueService.processFeedbackQueue();
        return "Processamento concluído.";
    }
}