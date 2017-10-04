/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
({
    "workerName": "worker-markup",
    "workerVersion": "${project.version}",
    "outputQueue": getenv("CAF_WORKER_OUTPUT_QUEUE")
    || (getenv("CAF_WORKER_BASE_QUEUE_NAME") || getenv("CAF_WORKER_NAME") || "worker") + "-out",
    "threads": getenv("CAF_MARKUP_WORKER_THREADS") || getenv("CAF_WORKER_THREADS") || 1,
    "addEmailHeadersDuringMarkup":  getenv("CAF_MARKUP_WORKER_ADD_EMAIL_HEADERS_DURING_MARKUP") || false,
    "emailHeaderMappings": {
        "From": ["Von", "De", "Van", "Fra", "Från"],
        "Subject": ["Betreff", "Asunto", "Objet"],
        "To": ["Sent to", "Recipient", "An", "Para", "À"],
        "Sent": ["Date", "Date_Sent", "DateSent", "Gesendet", "Fecha", "Envoyé", "Datum", "Skickat", "Sendt"]
    },
    "condensedHeaderMultiLangMappings": {
        "On": ["Le", "W dniu", "Op", "Am", "På", "Den"],
        "Separator": ["użytkownik"],
        "Wrote": ["a écrit", "napisał", "schreef", "verzond", "geschreven", "schrieb", "skrev"]
    }
});