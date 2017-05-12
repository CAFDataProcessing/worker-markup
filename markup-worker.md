# Markup Worker API

The Markup Worker is an implementation of the [Abstract Worker](https://github.hpe.com/caf/worker-caf/blob/develop/src/main/java/com/hpe/caf/worker/AbstractWorker.java). It can be used to identify constructs within a document or e-mail. When used with e-mails it uses the [util-email-content-segregation](https://github.com/CAFDataProcessing/util-email-content-segregation) java module to call the Mailgun/Talon library via the Jep interpreter to perform email content segregation. The separated emails are marked up with XML and the worker returns a configurable set of data.

## Version Matrix and Dependencies

The following components are dependencies of `worker-markdown` version 1.0:

- [util-email-content-segregation](https://github.com/CAFDataProcessing/util-email-content-segregation) - version 1.1.0
- [zero-allocation-hashing](https://github.com/OpenHFT/Zero-Allocation-Hashing) - version 0.6
- [jaxen](http://jaxen.org/apidocs/index.html) - version 1.1.6
- [jdom2](http://www.jdom.org/docs/apidocs/) - version 2.0.6
- [worker-caf](https://github.com/WorkerFramework/worker-framework/tree/develop/worker-caf) - version 10.4
- [worker-markup-shared](worker-markup-shared) - version 1.0.0
- [caf-api](https://github.com/CAFapi/caf-common/tree/develop/caf-api) - version 11.2
- [util-moduleloader](https://github.com/CAFapi/caf-common/tree/develop/util-moduleloader) - version 1.1
- [commons-io](http://commons.apache.org/proper/commons-io/javadocs/api-2.4/index.html) - version 2.4
- [jackson-databind](https://github.com/FasterXML/jackson-databind) - version 2.6.4

The worker's image is built by [worker-markup-container](worker-markup-container-fs) and uses a base image [jep-talon-image](jep-talon-image).

## General Operation Overview

The Markup Worker reads JSON encoded input messages which contain metadata about the document to be processed. The metadata values are passed using `ReferencedData` objects which means they may be passed by value (i.e. directly in the message) or by reference (i.e. where the message contains a location in central storage which contains the value). The messages also contain additional information about the request, such as how the client would like hashes to be generated, and what information they would like to see returned.

When used with e-mails, the Markup Worker will split the email chain and mark up individual emails with `<email>` tags. It then generates `<header>` tags with headers identified in the email text such as `<To>`, `<From>`, `<Cc>`, etc. The remaining email text is placed within `<body>` tags.

The [hash configuration](#hashconfiguration) supplied with the worker [task](#markupworkertask) message determines individual hashes to perform, each of these can be named with the `name` property. For each hash, the client supplies a list of tag elements to add to the hash, the method of normalization to perform on each tag element's value (i.e. `REMOVE_WHITESPACE`) and the hash function to use to generate the hash (i.e. `XXHASH64`). A `scope` setting determines whether the hash is generated for each email or for the entire email chain. In the case of an `EMAIL_SPECIFIC` scope, the specified fields in each email are included in the hash and `<hash>` tags are added to each email. In the case of a `EMAIL_THREAD` scope, the `<hash>` tags are added under the `<root>` element and include the specified fields for the entire email chain.

The client also supplies a list of [OutputFields](#outputfield) in the task message. These include a field name to be output and an XPath expression to retrieve a value from the XML document. The worker evaluates the XPath expressions configuration and returns a list of name-value pairs consisting of the field name and the results of each XPath expression execution. For sample XPath expressions see [here](#sample-xpath-expressions).

## Configuration

The Markup Worker uses the standard `CAF-API` system of `ConfigurationSource`. The worker specific configuration is [MarkupWorkerConfiguration](https://github.com/CAFDataProcessing/worker-markup/blob/develop/worker-markup/src/main/java/com/hpe/caf/worker/markup/MarkupWorkerConfiguration.java) which has the options:

- `outputQueue`: the output queue to return results to RabbitMQ.
- `threads`: the number of threads to use in the worker.

## Input Task Format

There are additional configurations to be supplied by the user on a per-task basis. These are passed to the Worker in the JSON message. A description of the worker's task message is shown below, along with its constituent HashConfiguration and OutputField components.

#### MarkupWorkerTask

|    Component          |     Description    |
| --------------------- | ------------------ |
| `sourceData` | A `Multimap<String, ReferencedData>` containing the document metadata. If the `isEmail` flag is set then the `CONTENT` key is expected to contain the e-mail chain. |
| `hashConfiguration` | The configuration used for hashing the XML tags. For more detail see [HashConfiguration](#hashconfiguration) |
| `outputFields` | The fields to output and the XPath expression to obtain the field's value from the XML. For more detail see [OutputField](#outputfield). |
| `isEmail` | A flag indicating whether the document is an e-mail thread. |

#### HashConfiguration

|    Component          |     Description    |
| --------------------- | ------------------ |
| `name` | The name of the hash to be included, which is added to the `<hash>` element as an attribute for identification purposes. |
| `scope` | The scope of the email chain to perform the hash, i.e. <br/>`EMAIL_SPECIFIC`:<br/>Include fields from individual emails in the hash and apply `<hash>` tags at email level.<br/>`EMAIL_THREAD`:<br/>Include the fields of the entire email thread in the hash and apply `<hash>` tags at a thread level.<br/>*Note: Also use this value for non-emails, to generate hash digests for an entire document.* |
| `fields` | A list of field objects, these represent tag elements to include in the hash. <br/> `name`: the name of the element tag as it appears in the XML. <br/> `normalizationType`: the type of normalization to be applied to the contents of this tag element i.e. `NONE`, `REMOVE_WHITESPACE`, `REMOVE_WHITESPACE_AND_LINKS`, `NAME_ONLY`. |
| `hashFunctions` | A list of hash functions to be performed on the fields above. i.e. `NONE` or `XXHASH64`. |

#### OutputField

|    Component          |     Description    |
| --------------------- | ------------------ |
| `field` | The field name to be returned by the worker in the output message. |
| `xPathExpression` | The XPath expression which will be evaluated against the marked up XML to obtain the value for the output field. |

If the desired output is the entire XML document, this can be retrieved by supplying the following output field:

````
"outputFields": [{
  "field": "XML",
  "xPathExpression": "."
}]
```

#### Sample Task Message

This is a sample task message sent to the input queue of the Markup Worker. In normal use the `"taskData"` would be Base64 encoded but here we have decoded it for exemplification purposes.

```
{
	"version": 3,
	"taskId": "SampleEmail.txt",
	"taskClassifier": "MarkupWorker",
	"taskApiVersion": 1,
	"taskData": {
		"sourceData": {
			"CONTENT": [{
				"reference": null,
				"data": "From: Za M <zaramckeown@gmail.com>\nSent: 27 September 2016 12:30:24\nTo: McKeown, Zara\nSubject: Re: FW: From a mixture of email clients\n\nThank you!\n\nFrom: Rogan, Adam Pau\nSent: Fri, Oct 7, 2016 at 8:21 AM -0400\nTo: McKeown, Zara <zara.mckeown@hpe.com>\nSubject: RE: From a mixture of email clients\n\nHi back\n\nFrom: McKeown, Zara\nSent: 27 September 2016 12:20\nTo: Rogan, Adam Pau <adam.pau.rogan@hpe.com>\nSubject: From a mixture of email clients\n\nHi"
			}]
		},
		"hashConfiguration": [{
			"name": "Normalized",
			"scope": "EMAIL_SPECIFIC",
			"fields": [{
				"name": "To",
				"normalizationType": "NAME_ONLY"
			}, {
				"name": "From",
				"normalizationType": "NAME_ONLY"
			}, {
				"name": "Body",
				"normalizationType": "REMOVE_WHITESPACE_AND_LINKS"
			}],
			"hashFunctions": ["XXHASH64"]
		}],
		"outputFields": [{
			"field": "SECTION_SORT",
			"xPathExpression": "/root/email[1]/headers/Sent/@dateUtc"
		}, {
			"field": "SECTION_ID",
			"xPathExpression": "/root/email[1]/hash/digest/@value"
		}, {
			"field": "PARENT_ID",
			"xPathExpression": "/root/email[2]/hash/digest/@value"
		}, {
			"field": "ROOT_ID",
			"xPathExpression": "/root/email[last()]/hash/digest/@value"
		}, {
			"field": "MARKUP_WORKER_XML",
			"xPathExpression": "."
		}],
		"isEmail": true
	},
	"taskStatus": "NEW_TASK",
	"context": {
		"context": "integration-test"
	},
	"to": "markupworker-test-input-1",
	"tracking": null,
	"sourceInfo": null
}
```

## Output Message

The result class is [MarkupWorkerResult](https://github.com/CAFDataProcessing/worker-markup/blob/develop/worker-markup-shared/src/main/java/com/hpe/caf/worker/markup/MarkupWorkerResult.java) and is shown below.

|    Component          |     Description    |
| --------------------- | ------------------ |
| `workerStatus` | The worker specific return code depicting the processing result status. Any other value than `COMPLETED` means failure. The possible worker statuses are: <br/> - `COMPLETED`: the worker processed the task successfully. <br/> - `WORKER_FAILED`: the worker failed in an unexpected way. <br/>  |
| `fieldList` | A list of name-value pairs which specify the output fields and their corresponding values. These values were retieved by the XPath expression in the OutputField. <br/> `name`: the name of the field to output. <br/> `value`: the value returned from the XPath expression. |

#### Sample Output Message

This is a sample output message sent to the output queue from the Markup Worker. In normal use the `"taskData"` would be Base64 encoded but here we have decoded it for exemplification purposes.

```
{
	"version": 3,
	"taskId": "SampleEmail.txt",
	"taskClassifier": "MarkupWorker",
	"taskApiVersion": 1,
	"taskData": {
		"workerStatus": "COMPLETED",
		"fieldList": [{
			"name": "SECTION_SORT",
			"value": "2016-09-27T12:30:24Z"
		}, {
			"name": "SECTION_ID",
			"value": "ca17a060c9e2ff28"
		}, {
			"name": "PARENT_ID",
			"value": "a1425efb02868dfd"
		}, {
			"name": "ROOT_ID",
			"value": "969b26c06e65c4e9"
		}, {
			"name": "MARKUP_WORKER_XML",
			"value": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root><email><hash name=\"Normalized\"><config><fields><field><name>To</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>From</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>Body</name><normalizationType>REMOVE_WHITESPACE_AND_LINKS</normalizationType></field></fields></config><digest function=\"XXHASH64\" value=\"ca17a060c9e2ff28\" /></hash><headers><From>From: Za M &lt;zaramckeown@gmail.com&gt;&#xD;</From>\r\n<Sent dateUtc=\"2016-09-27T12:30:24Z\">Sent: 27 September 2016 12:30:24&#xD;</Sent>\r\n<To>To: McKeown, Zara&#xD;</To>\r\n<Subject>Subject: Re: FW: From a mixture of email clients&#xD;</Subject>\r\n</headers><body>&#xD;\r\nThank you!&#xD;\r\n&#xD;</body></email><email><hash name=\"Normalized\"><config><fields><field><name>To</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>From</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>Body</name><normalizationType>REMOVE_WHITESPACE_AND_LINKS</normalizationType></field></fields></config><digest function=\"XXHASH64\" value=\"a1425efb02868dfd\" /></hash><headers><From>From: Rogan, Adam Pau&#xD;</From>\r\n<Sent dateUtc=\"2016-10-07T12:21:00Z\">Sent: Fri, Oct 7, 2016 at 8:21 AM -0400&#xD;</Sent>\r\n<To>To: McKeown, Zara &lt;zara.mckeown@hpe.com&gt;&#xD;</To>\r\n<Subject>Subject: RE: From a mixture of email clients&#xD;</Subject>\r\n</headers><body>&#xD;\r\nHi back&#xD;\r\n&#xD;</body></email><email><hash name=\"Normalized\"><config><fields><field><name>To</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>From</name><normalizationType>NAME_ONLY</normalizationType></field><field><name>Body</name><normalizationType>REMOVE_WHITESPACE_AND_LINKS</normalizationType></field></fields></config><digest function=\"XXHASH64\" value=\"969b26c06e65c4e9\" /></hash><headers><From>From: McKeown, Zara&#xD;</From>\r\n<Sent dateUtc=\"2016-09-27T12:20:00Z\">Sent: 27 September 2016 12:20&#xD;</Sent>\r\n<To>To: Rogan, Adam Pau &lt;adam.pau.rogan@hpe.com&gt;&#xD;</To>\r\n<Subject>Subject: From a mixture of email clients&#xD;</Subject>\r\n</headers><body>&#xD;\r\nHi</body></email></root>\r\n"
		}]
	},
	"taskStatus": "RESULT_SUCCESS",
	"context": {
		"context": "integration-test"
	},
	"to": "markupworker-test-output-1",
	"tracking": null,
	"sourceInfo": {
		"name": "MarkupWorker",
		"version": "1.0.0"
	}
}
```

## Recommended Hash Configuration

The following hash configuration is recommended to generate hashes which can be used to identity related e-mails, especially replied-to and forwarded e-mails:

```
"hashConfiguration": [{
	"name": "Normalized",
	"scope": "EMAIL_SPECIFIC",
	"fields": [
		{ "name": "To",   "normalizationType": "NAME_ONLY" },
		{ "name": "From", "normalizationType": "NAME_ONLY" },
		{ "name": "Body", "normalizationType": "REMOVE_WHITESPACE_AND_LINKS" }
	],
	"hashFunctions": [
		"XXHASH64"
	]
}]
```

## Sample XPath Expressions

|                      XPath Expression                       |                  Returns                   |
| ----------------------------------------------------------- | ------------------------------------------ |
| `.` | the entire XML |
| `/root/email[1]/hash/digest/@value` | the `value` attribute of the hash of the first email under `<root>` tags |
| `/root/email[2]/hash/digest/@value` | the `value` attribute of the hash of the second email under `<root>` tags |
| `/root/email[last()]/hash/digest/@value` | the `value` attribute of the hash of the last email under `<root>` tags |
| `/root/email[1]/headers/Sent/text()` | the text of the `Sent` element of the first email |
| `/root/CAF_MAIL_MESSAGE_ID/text()` | the text of the `CAF_MAIL_MESSAGE_ID` element |

## Health Checks

This worker provides a basic health check by returning `HEALTHY` if it can communicate with Marathon.

## Resource Usage

The number of worker threads is configured using the `threads` setting in the [Markup Worker Configuration](#configuration).

Memory usage will vary significantly with the size of the input message.

## Failure Modes

There are three main places where this worker can fail:

- Configuration errors: these will manifest on startup and cause the worker not to start at all. Check the logs for clues, and double check your configuration files.
- `WORKER_FAILED`: Tasks coming from the worker with this worker status have failed during processing in some unexpected way. This could be due to a number of reasons:
  - no hash configuration has been specified, 
  - a failure to separate emails in the `Jep` python library, 
  - a failure to parse the XML into a document,
  - a failure to acquire data from datastore.

## Upgrade Procedures

These follow standard CAF Worker upgrade procedures. If the version of `worker-markup-shared` has not changed then an upgrade to `worker-markup` is an in-place upgrade.

If you need to do a rolling upgrade when `worker-markup-shared` has not changed then:
- Spin up containers of the new version of `worker-markup`.
- Replace old versions of producers of `MarkupWorkerTask` with new ones.
- Allow the queue with the old versions of `MarkupWorkerTask` to drain then shut down the old worker containers.

## Maintainers

The following people are contacts for developing and maintaining this module:

- Zara McKeown (Belfast UK, zara.mckeown@hpe.com)
- Adam Rogan (Belfast UK, adam.pau.rogan@hpe.com)
