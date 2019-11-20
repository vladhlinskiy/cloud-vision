# Google Cloud Image Extractor Transform

Description
-----------
This plugin can be used in conjunction with the file path batch source to extract enrichments from each image based
on selected features.

Credentials
-----------
If the plugin is run on a Google Cloud Dataproc cluster, the service account key does not need to be
provided and can be set to 'auto-detect'.
Credentials will be automatically read from the cluster environment.

If the plugin is not run on a Dataproc cluster, the path to a service account key must be provided.
The service account key can be found on the Dashboard in the Cloud Platform Console.
Make sure the account key has permission to access Google Cloud Vision.
The service account key file needs to be available on every node in your cluster and
must be readable by all users running the job.

Properties
----------
TODO
