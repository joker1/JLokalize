# JLokalize

This is a customized version of JLokalize.

Original project: http://jlokalize.sourceforge.net/

# Changelog

## Changes in v1.4

Add support for opening files on program startup using command line arguments.
```bash
java -jar JLokalize.jar <file-to-open.properties>
java -jar JLokalize.jar messages_en.properties
```

Enhancements:

- Open file using command line argument

## Changes in v1.3

Implement support for `UTF-8` encoded .properties files. Encoding can be specified using Java system property `jlokalize.encoding=`. Unicode escape of non-ascii characters when saving is now optional and can be specified by `jlokalize.escape` system property. If escaping is not used, characters are saved using character set specified by `jlokalize.encoding=` property.

JLokalize now uses `UTF-8` encoding by default and does not escape non-ascii characters.

```bash
java -Djlokalize.encoding=UTF-8 -Djlokalize.escape=false -jar JLokalize.jar
java -Djlokalize.encoding=ISO-8859-1 -Djlokalize.escape=true -jar JLokalize.jar
```

This release also fixes weird `NullPointerException` that happens when running on Linux (not sure if the fix is correct but at least it seems to work).

Enhancements:

- Add support for `jlokalize.encoding=` system property
- Add support for `jlokalize.escape=` system property
- Use `UTF-8` encoding by default
- Do not escape non-ascii characters by default

Bugfixes:

- Fixed `NullPointerException` when running on Linux

## Changes in v1.2-ing

New configuration options:

- `properties.comments.suffix`: Suffix to use for comments. Defaults to `.comment`.
- `properties.sort`: If set to `true`, properties will be sorted alphabetically (default). If set to `false`, properties will be shown in the same order as found in the properties file.
- `properties.lineSeparator`: Allows control over the line separator in generated files. Possible values are `unix` and `windows`. If not set, the platform-specific line separator is used.

Enhancements:

- Allow removal of comments (just delete the text)

Bugfixes:

- Fixed change detection logic for comments
- Fixed chain loading of internal language files

