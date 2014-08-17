DisUnity
=========

An experimental command-line toolset for Unity asset and asset bundle files, mostly designed for extraction.

Download
--------

The latest build can be found on the [releases page](https://github.com/ata4/disunity/releases).

Usage
-----

    disunity <command> <file>
    
**Note:** depending on the platform, you may need to run disunity.bat (Windows) or disunity.sh (Linux/MacOS). In case the launch script fails, try `java -jar disunity.jar`.

### Available commands

| Command        | Purpose
| :------------- | :-------------
| dump           | Converts binary object data to human-readable plain text, similar to the binary2text tool shipped with the Unity editor.
| dump-struct    | Like *dump*, but just for the structure information.
| extract        | Extracts asset objects to regular files (.txt, .wav, .tga, etc.). See SUPPORT.md for a list of supported asset types.
| extract-raw    | Extracts raw serialized object data. Could be useful for manual extraction if *extract* doesn't support the wanted asset type.
| extract-txt    | Like *dump*, but writes the output to text files instead of the console.
| extract-struct | Like *extract-txt*, but just for the structure information.
| learn          | Learns the structure information from the submitted files and stores any new structs in the database file structdb.dat. The database is required to deserialize standalone asset files, which usually don't contain any structure information.
| info           | Outputs various information about assets and asset bundle files.
| info-stats     | Outputs class usage statistics for asset files.
| bundle-extract | Extracts all packed files from asset bundles.
| bundle-inject  | Injects files previously extracted with the *bundle-extract* back into the asset bundle.
| bundle-list    | Lists all files contained in asset bundles.
| split          | Attempts to split an asset file into multiple smaller asset files.
| list           | Lists all asset objects in a tabular form.

### Other parameters

Run disunity with the `-h` parameter for further usage.

### Examples

Extract all supported assets from a bundle file:

    disunity extract Web.unity3d

Extract all packed files from two bundle files:

    disunity bundle-extract episode1.unity3d episode2.unity3d

Extract textures from the asset file sharedassets0.assets:

    disunity extract -f texture2d sharedassets0.assets

Dump web player configuration from the file named Web.unity3d:

    disunity dump -f playersettings Web.unity3d

Show information about all asset files in the directory "assets":

    disunity info assets\*.asset
