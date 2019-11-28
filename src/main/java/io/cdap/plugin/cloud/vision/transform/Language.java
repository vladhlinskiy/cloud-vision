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

package io.cdap.plugin.cloud.vision.transform;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The list of languages (with associated languageHint codes) supported by
 * {@link com.google.cloud.vision.v1.Feature.Type#TEXT_DETECTION} and
 * {@link com.google.cloud.vision.v1.Feature.Type#DOCUMENT_TEXT_DETECTION}.
 */
public enum Language {

  AFRIKAANS("Afrikaans", "af"),
  ALBANIAN("Albanian", "sq"),
  ARABIC("Arabic", "ar"),
  ARMENIAN("Armenian", "hy"),
  BELORUSSIAN("Belorussian", "be"),
  BENGALI("Bengali", "bn"),
  BULGARIAN("Bulgarian", "bg"),
  CATALAN("Catalan", "ca"),
  CHINESE("Chinese", "zh"),
  CROATIAN("Croatian", "hr"),
  CZECH("Czech", "cs"),
  DANISH("Danish", "da"),
  DUTCH("Dutch", "nl"),
  ENGLISH("English", "en"),
  ESTONIAN("Estonian", "et"),
  FILIPINO("Filipino", "fil"),
  FINNISH("Finnish", "fi"),
  FRENCH("French", "fr"),
  GERMAN("German", "de"),
  GREEK("Greek", "el"),
  GUJARATI("Gujarati", "gu"),
  HEBREW("Hebrew", "iw"),
  HINDI("Hindi", "hi"),
  HUNGARIAN("Hungarian", "hu"),
  ICELANDIC("Icelandic", "is"),
  INDONESIAN("Indonesian", "id"),
  ITALIAN("Italian", "it"),
  JAPANESE("Japanese", "ja"),
  KANNADA("Kannada", "kn"),
  KHMER("Khmer", "km"),
  KOREAN("Korean", "ko"),
  LAO("Lao", "lo"),
  LATVIAN("Latvian", "lv"),
  LITHUANIAN("Lithuanian", "lt"),
  MACEDONIAN("Macedonian", "mk"),
  MALAY("Malay", "ms"),
  MALAYALAM("Malayalam", "ml"),
  MARATHI("Marathi", "mr"),
  NEPALI("Nepali", "ne"),
  NORWEGIAN("Norwegian", "no"),
  PERSIAN("Persian", "fa"),
  POLISH("Polish", "pl"),
  PORTUGUESE("Portuguese", "pt"),
  PUNJABI("Punjabi", "pa"),
  ROMANIAN("Romanian", "ro"),
  RUSSIAN("Russian", "ru"),
  RUSSIAN_OLD_ORTHOGRAPHY("Russian(Old Orthography)", "ru-PETR1708"),
  SERBIAN("Serbian", "sr"),
  SERBIAN_LATIN("Serbian(Latin)", "sr-Latn"),
  SLOVAK("Slovak", "sk"),
  SLOVENIAN("Slovenian", "sl"),
  SPANISH("Spanish", "es"),
  SWEDISH("Swedish", "sv"),
  TAMIL("Tamil", "ta"),
  TELUGU("Telugu", "te"),
  THAI("Thai", "th"),
  TURKISH("Turkish", "tr"),
  UKRAINIAN("Ukrainian", "uk"),
  VIETNAMESE("Vietnamese", "vi"),
  YIDDISH("Yiddish", "yi");

  private static final Map<String, Language> byDisplayName = Arrays.stream(values())
    .collect(Collectors.toMap(Language::getDisplayName, Function.identity()));

  private final String displayName;
  private final String code;

  Language(String displayName, String code) {
    this.displayName = displayName;
    this.code = code;
  }

  @Nullable
  public static Language fromDisplayName(String displayName) {
    return byDisplayName.get(displayName);
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCode() {
    return code;
  }
}
