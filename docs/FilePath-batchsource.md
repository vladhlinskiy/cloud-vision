# File Path Batch Source

Description
-----------
This source will read a directory, and instead of emitting records from files in the directory, it will emit all the
file names as records. It work for object stores as well.

Credentials
-----------
If the plugin is run on a Google Cloud Dataproc cluster, the service account key does not need to be
provided and can be set to 'auto-detect'.
Credentials will be automatically read from the cluster environment.

If the plugin is not run on a Dataproc cluster, the path to a service account key must be provided.
The service account key can be found on the Dashboard in the Cloud Platform Console.
Make sure the account key has permission to access Google Cloud Storage.
The service account key file needs to be available on every node in your cluster and
must be readable by all users running the job.

Configuration
-------------

**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata, etc.

**Service Account File Path**: Path on the local file system of the service account key used for
authorization. Can be set to 'auto-detect' when running on a Dataproc cluster.
When running on other clusters, the file must be present on every node in the cluster.

**Project ID**: Google Cloud Project ID, which uniquely identifies a project. It can be found on the Dashboard in the
Google Cloud Platform Console.

**Path:** The path to the directory where the files whose paths are to be emitted are located.

**Recursive:** Whether the plugin should recursively traverse the directory for subdirectories.

**Last Modified After:** A way to filter files to be returned based on their last modified timestamp. Timestamp string
must be in the ISO-8601 format without the timezone offset (always ends in Z).
Expected pattern: `yyyy-MM-dd'T'HH:mm:ss.SSSZ`, for example: `2019-10-02T13:12:55.123Z`.

**Split By:** Determines splitting mechanisms. Choose amongst default (uses the default splitting mechanism of file
input format), batch size (by number of files in a batch), directory (by each sub directory).

**Batch Size:** Specifies the number of files to process in a single batch. Only required when Split By is set to batch
size.
