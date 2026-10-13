
# Translating Stendhal

___NOTE:__ localization is still a work-in-progress_

PolanieOnLine currently supports `pl` and `en`. Polish is the default. The choice is stored on the character and shared by
the Java and browser clients through the existing `language` action. Other files are retained as legacy translation data,
but are not selectable. English coverage is incomplete; missing or blank entries leave the original text visible.

The same UTF-8 `original=translation` dictionaries are bundled with the server and clients. Add Polish source text to
`en.txt` when translating current game content into English. Keep internal item names, quest states, action codes and
player messages unchanged. NPC speech is translated for display in each receiving client, not on the shared NPC object.


## Locale File Format

The file format is simple. It is a plain text file with each line representing either a translatable string or a comment
(a line beginning with "#"). Comments and empty lines are ignored by the translation interface. Each line for a
translatable string is made up three parts:

1. The original string (usually in English).
2. A "=" separator.
3. The locale's translation of the original string.

Example of translating the item name "dress" to Spanish:

```
dress=vestido
```


## New Translations

1. Make sure the desired locale file does not already exist.
2. Copy the template (`data/languages/template.txt`) to a new file with the desired locale code.

    Example for Spanish: `data/languages/template.txt` -> `data/languages/es.txt`

3. Open the new locale file in a text editor.
4. Add the locale's equivalent translations to each string following the "=" separator.

___NOTE:__ if a string should not be translated, simply delete the line_


## Updating Translations

1. Open the appropriate translation file in a text editor (example: `data/languages/es.txt`).
2. Find the string to be translated and update locale translation following the "=" separator.
3. If the string does not exist in the file simply add it followed by a "=" separator and the locale translation.
