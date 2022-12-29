package org.camunda.bpm.external.balance;

import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

public class BalanceAccoutApplication {
	private final static Logger LOGGER = Logger.getLogger(BalanceAccoutApplication.class.getName());

	public static void main(String[] args) {		

		ExternalTaskClient client = ExternalTaskClient.create().baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(10000) // long polling timeout
				.build();


		
		client.subscribe("approve").lockDuration(10000).handler((externalTask, externalTaskService) -> {
			String operation = externalTask.getVariable("operation");
			Integer value = externalTask.getVariable("amount");
			VariableMap variables = Variables.createVariables();
			variables.put("inputValue", "1000");
			if(value == 10000) {
				externalTaskService.handleFailure(externalTask.getId(),
						"500 - CPF null", 
						"Sem A Variavel CPF", 
						0, 
						0,
						externalTask.getAllVariables(), 
						variables);
			}else {
				externalTaskService.complete(externalTask, variables); 
			}
			
		
		}).open();
		
		ExternalTaskClient client2 = ExternalTaskClient.create().baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(10000) // long polling timeout
				.build();
		
		client2.subscribe("update").lockDuration(10000).handler((externalTask, externalTaskService) -> {
			String operation = externalTask.getVariable("operation");
			Integer value = externalTask.getVariable("amount");
			String inputValue = externalTask.getVariable("inputValue");

			LOGGER.info("Operation: " + operation + " with value: " + value);
			LOGGER.info("inputValue: " + inputValue);

			VariableMap variables = Variables.createVariables();
			

			externalTaskService.complete(externalTask,variables);
			
		}).open();
		
		ExternalTaskClient client1 = ExternalTaskClient.create().baseUrl("http://localhost:8080/engine-rest")
				.asyncResponseTimeout(10000) // long polling timeout
				.build();
		 
		client1.subscribe("listeners").lockDuration(10000).handler((externalTask, externalTaskService) -> {
			VariableMap variables = Variables.createVariables();
			System.out.println("Finalizado o processo de formalização");

			externalTaskService.complete(externalTask,variables);
			
		}).open();
	}
}