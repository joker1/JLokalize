JLokalize
=========

This is a customized version of JLokalize.

Original project: http://jlokalize.sourceforge.net/

Changes from 1.1
----------------

New configuration options:

- `properties.comments.suffix`: Suffix to use for comments. Defaults to `.comment`.
- `properties.sort`: If set to `true`, properties will be sorted alphabetically (default). If set to `false`, properties will be shown in the same order as found in the properties file.
- `properties.lineSeparator`: Allows control over the line separator in generated files. Possible values are `unix` and `windows`. If not set, the platform-specific line separator is used.

Enhancements:

- Allow removal of comments (just delete the text)

Bugfixes:

- Fixed change detection logic for comments
- Fixed chain loading of internal language files

