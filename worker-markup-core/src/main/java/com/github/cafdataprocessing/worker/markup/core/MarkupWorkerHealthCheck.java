/*
 * Copyright 2017-2024 Open Text.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.worker.markup.core;

import com.hpe.caf.api.HealthReporter;
import com.hpe.caf.api.HealthResult;

/**
 * Health check for the MarkupWorker, health is displayed on the Marathon GUI.
 */
public class MarkupWorkerHealthCheck implements HealthReporter
{
    public MarkupWorkerHealthCheck()
    {
    }

    /**
     * The health check checks if all the external components that the worker depends on are available. The health result is displayed on
     * Marathon GUI.
     *
     * @return - HealthResult
     */
    @Override
    public HealthResult healthCheck()
    {
        // Check that all worker components are available. If the worker depends on an external service, check that the service is accessible here.
        // In this scenario, the MarkupWorker does not depend on any external components. It will always return HealthResult.RESULT_HEALTHY.
        // If a service was not available, catch any exceptions, log a warning and return a UNHEALTHY health result.

        return HealthResult.RESULT_HEALTHY;
    }
}
