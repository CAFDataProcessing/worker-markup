# Markup Worker Container FS

This is a prepackaged Docker image containing the Markup Worker (worker-markup) that uses a File System based data store.

More information on the functioning of the Markup Worker is available [here](../README.md).

## Container Deployment and Configuration

The Markup Worker can be deployed using the compose file [here](https://github.com/CAFDataProcessing/data-processing-service-deploy). For instructions on how to deploy using the compose file see the [Data Processing Getting Started Guide](https://pages.github.hpe.com/caf/data-processing-service/pages/en-us/Getting-Started).

Details on configuration of the Markup Worker can be found [here](../markup-worker.md#configuration).

### Environment Variable Configuration

The worker container supports reading its configuration solely from environment variables. To enable this mode do not pass the `CAF_APPNAME` and `CAF_CONFIG_PATH` environment variables to the worker. This will cause it to use the default configuration files embedded in the container which check for environment variables. A listing of the RabbitMQ and Storage properties is available [here](https://github.com/WorkerFramework/worker-framework/tree/develop/worker-default-configs).

The Markup Worker specific configuration that can be controlled through the default configuration file is described below;

#### MarkupWorkerConfiguration

| Property | Checked Environment Variables | Default               |
|----------|-------------------------------|-----------------------|
| outputQueue   |  `CAF_WORKER_OUTPUT_QUEUE`                                                      | worker-out  |
|              |   `CAF_WORKER_BASE_QUEUE_NAME` with '-out' appended to the value if present     |             |
|              |  `CAF_WORKER_NAME` with '-out' appended to the value if present                 |             |
|  threads   |   `CAF_MARKUP_WORKER_THREADS`                                         |   2       |
|             |   `CAF_WORKER_THREADS`                                             |          |
| addEmailHeadersDuringMarkup   |  `CAF_MARKUP_WORKER_ADD_EMAIL_HEADERS_DURING_MARKUP`    | false  |

During markup of an email the markup worker can make email header information available for use in hash and output configurations. This can be enabled by setting addEmailHeadersDuringMarkup to true via the environment variable.


## Feature Testing

The testing for the Markup Worker is defined in [testcases](testcases).
