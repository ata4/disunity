DisUnity
=========

An experimental command-line toolset for Unity asset and asset bundle files, mostly designed for extraction.

Download
--------

The latest build can be found on the [releases page](https://github.com/ata4/disunity/releases).

Usage
-----

    disunity -c <command> <file>
    
**Note:** depending on the platform, you may need to run disunity.bat (Windows) or disunity.sh (Linux/MacOS). In case the launch script fails, try `java -jar disunity.jar`.

### Commands

`dump` - Converts the asset file to plain text, similar to the binary2text tool shipped with the Unity editor.

`dump-struct` - Like `dump`, but just for the data structure.

`extract` - Extracts supported asset content and stores it in regular files. Default command if the `-c` parameter is omitted.

`extract-raw` - Extracts the raw serialized binary data from the asset.

`learn` - Learns the structure from an Unity webplayer bundle (*.unity3d) and stores any new structs in the database file structdb.dat. Its data is required to deserialize standalone asset files, which usually don't contain any structure data.

`info` - Outputs various information for assets and asset bundle files.

`info-stats` - Outputs class usage statistics for asset files.

`unbundle` - Extracts Unity webplayer bundles (*.unity3d).

`fixrefs` - Fixes shared asset references in extracted scene files by converting relative to absolute paths so they can be opened with the Unity editor correctly.

**Note:** If the shared assets are moved to a different folder, the scene needs to be fixed again.

`split` - Attempts to split an asset file into multiple smaller asset files.

`list` - Lists all objects and files in a tabular form.

### Other parameters

Run disunity with the `-h` parameter for further usage.


Dependencies
------------

* [ioutils](https://github.com/ata4/ioutils)
* [lzmajio](https://github.com/ata4/lzmajio)
* [apache-commons-cli-1.2](http://commons.apache.org/proper/commons-cli/)
* [apache-commons-io-2.4](http://commons.apache.org/proper/commons-io/)
* [apache-commons-lang3-3.1](http://commons.apache.org/proper/commons-lang/)