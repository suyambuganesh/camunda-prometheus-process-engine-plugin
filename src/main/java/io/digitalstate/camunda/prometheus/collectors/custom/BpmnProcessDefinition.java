package io.digitalstate.camunda.prometheus.collectors.custom;

import io.digitalstate.camunda.prometheus.collectors.SimpleGaugeMetric;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Example usage of creating a custom metric that starts at runtime and performs a timed query of the engine
 *  */

public class BpmnProcessDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(BpmnProcessDefinition.class);

    public BpmnProcessDefinition(ProcessEngine processEngine, long startDelayMills, long frequencyMills){
        String timerName = this.getClass().getName() + " timer";
        new Timer(timerName, true).schedule(new TimerTask() {
            @Override
            public void run() {
                collectAll(processEngine);
            }
        }, startDelayMills, frequencyMills);
    }

    /**
     * Collected Process Definition Instance counts using the Management Service's Process Definition Statistics Query
     * @param processEngine
     * @param engineName
     */
    public static void collectProcessDefinitionInstanceCount(ProcessEngine processEngine, String engineName){
        SimpleGaugeMetric counter = new SimpleGaugeMetric(
                "process_definition_stats_instance_count",
                "The number of activity instances started. This is also known as flow node instances (FNI).",
                Arrays.asList("process_definition_key","engine_name")
        );

        List<ProcessDefinitionStatistics> stats = processEngine.getManagementService().createProcessDefinitionStatisticsQuery().list();

        LOGGER.debug("Collecting Metric Count for Active Process Instances By Process Definition Key: " + stats.toString());

        stats.forEach( stat ->
                counter.setValue(stat.getInstances(),
                        Arrays.asList(stat.getKey(),
                                engineName)));

    }

    /**
     * Collects all collectors defined in this class
     * @param processEngine
     */
    public static void collectAll(ProcessEngine processEngine){
        String engineName = processEngine.getName();

        collectProcessDefinitionInstanceCount(processEngine, engineName);

    }
}