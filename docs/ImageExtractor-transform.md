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

**Service Account File Path**: Path on the local file system of the service account key used for
authorization. Can be set to 'auto-detect' when running on a Dataproc cluster.
When running on other clusters, the file must be present on every node in the cluster.

**Project ID**: Google Cloud Project ID, which uniquely identifies a project. It can be found on the Dashboard in the
Google Cloud Platform Console.

**Path Field**: Field in the input schema containing the path to the image.

**Output Field**: Field to store the extracted image features. If the specified output field name already exists in the
input record, it will be overwritten.

**Features**: Features to extract from images.

**Language Hints**: Hints to detect the language of the text in the images.
