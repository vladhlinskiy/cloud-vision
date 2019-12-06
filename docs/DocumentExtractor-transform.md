# Google Cloud Document Extractor Transform

Description
-----------
This transform plugin can detect and transcribe text from small(up to 5 pages) PDF and TIFF files stored in Cloud 
Storage in an online manner.

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

**Service Account File Path**: Path on the local file system of the service account key used for
authorization. Can be set to 'auto-detect' when running on a Dataproc cluster.
When running on other clusters, the file must be present on every node in the cluster.

**Project ID**: Google Cloud Project ID, which uniquely identifies a project. It can be found on the Dashboard in the
Google Cloud Platform Console.

**Path Field**: Field in the input schema containing the path to the image.

**Output Field**: Field to store the extracted image features. If the specified output field name already exists in the
input record, it will be overwritten.

**Content Field**: Field in the input schema containing the file content, represented as a stream of bytes.

**Mime Type**: The type of the file. Currently only 'application/pdf', 'image/tiff' and 'image/gif' are supported.
Wildcards are not supported.

**Pages**: The pages in the file to perform image annotation.

**Features**: Features to extract from images.

**Language Hints**: Hints to detect the language of the text in the images.

**Aspect Ratios**: Ratio of the width to the height of the image. If not specified, the best possible crop is returned.

**Include Geo Results**: Whether to include results derived from the geo information in the image.
