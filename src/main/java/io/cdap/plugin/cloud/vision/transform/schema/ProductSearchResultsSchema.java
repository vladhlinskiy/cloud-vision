/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.cloud.vision.transform.schema;

import io.cdap.cdap.api.data.schema.Schema;

/**
 * Results for a product search request mapped to a record with following fields.
 */
public class ProductSearchResultsSchema {

  private ProductSearchResultsSchema() {
    throw new AssertionError("Should not instantiate static utility class.");
  }

  /**
   * Timestamp string of the index which provided these results. Products added to the product set and products removed
   * from the product set after this time are not reflected in the current results. A timestamp in RFC3339 UTC "Zulu"
   * format, accurate to nanoseconds. Example: "2014-10-02T15:01:23.045123456Z".
   */
  public static final String INDEX_TIME_FIELD_NAME = "indexTime";

  /**
   * List of results, one for each product match.
   */
  public static final String RESULTS_FIELD_NAME = "results";

  /**
   * List of results grouped by products detected in the query image. Each entry corresponds to one bounding polygon in
   * the query image, and contains the matching products specific to that region. There may be duplicate product matches
   * in the union of all the per-product results.
   */
  public static final String GROUPED_RESULTS_FIELD_NAME = "productGroupedResults";

  public static final Schema SCHEMA = Schema.recordOf("product-search-result-record",
    Schema.Field.of(INDEX_TIME_FIELD_NAME, Schema.of(Schema.Type.STRING)),
    Schema.Field.of(RESULTS_FIELD_NAME, Schema.arrayOf(Result.SCHEMA)),
    Schema.Field.of(GROUPED_RESULTS_FIELD_NAME, Schema.arrayOf(GroupedResult.SCHEMA))
  );

  /**
   * Information about a product.
   */
  public static class Result {

    /**
     * The resource name of the image from the product that is the closest match to the query.
     */
    public static final String IMAGE_FIELD_NAME = "image";

    /**
     * A confidence level on the match, ranging from 0 (no confidence) to 1 (full confidence).
     */
    public static final String SCORE_FIELD_NAME = "score";

    /**
     * The Product.
     */
    public static final String PRODUCT_FIELD_NAME = "product";

    public static final Schema SCHEMA = Schema.recordOf("result-record",
      Schema.Field.of(IMAGE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(PRODUCT_FIELD_NAME, Schema.arrayOf(Product.SCHEMA))
    );
  }

  /**
   * A Product contains ReferenceImages.
   */
  public static class Product {

    /**
     * The resource name of the product. Format is: projects/PROJECT_ID/locations/LOC_ID/products/PRODUCT_ID.
     */
    public static final String NAME_FIELD_NAME = "name";

    /**
     * The user-provided name for this Product.
     */
    public static final String DISPLAY_NAME_FIELD_NAME = "displayName";

    /**
     * User-provided metadata to be stored with this product.
     */
    public static final String DESCRIPTION_FIELD_NAME = "description";

    /**
     * The category for the product identified by the reference image.
     * This should be either "homegoods", "apparel", or "toys".
     */
    public static final String PRODUCT_CATEGORY_FIELD_NAME = "productCategory";

    /**
     * Key-value pairs that can be attached to a product. Multiple values can be assigned to the same key.
     * One product may have up to 100 productLabels.
     */
    public static final String PRODUCT_LABELS_FIELD_NAME = "productLabels";

    public static final Schema SCHEMA = Schema.recordOf("product-record",
      Schema.Field.of(NAME_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DISPLAY_NAME_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PRODUCT_CATEGORY_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PRODUCT_LABELS_FIELD_NAME, Schema.arrayOf(KeyValue.SCHEMA))
    );
  }

  /**
   * A product label represented as a key-value pair.
   */
  public static class KeyValue {

    /**
     * The key of the label attached to the product.
     */
    public static final String KEY_FIELD_NAME = "key";

    /**
     * The value of the label attached to the product.
     */
    public static final String VALUE_FIELD_NAME = "value";

    public static final Schema SCHEMA = Schema.recordOf("key-value-record",
      Schema.Field.of(KEY_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(VALUE_FIELD_NAME, Schema.of(Schema.Type.STRING))
    );
  }

  /**
   * Information about the products similar to a single product in a query image.
   */
  public static class GroupedResult {

    /**
     * The bounding polygon around the product detected in the query image.
     */
    public static final String POSITION_FIELD_NAME = "position";

    /**
     * A confidence level on the match, ranging from 0 (no confidence) to 1 (full confidence).
     */
    public static final String RESULTS_FIELD_NAME = "results";

    public static final Schema SCHEMA = Schema.recordOf("grouped-result-record",
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(VertexSchema.SCHEMA)),
      Schema.Field.of(RESULTS_FIELD_NAME, Schema.arrayOf(Result.SCHEMA))
    );
  }
}
