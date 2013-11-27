DisUnity
=========

An experimental toolset for Unity asset and asset bundle files, mostly designed for extraction.

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

`info` - Outputs various information for asset and asset bundle files.

`info-stats` - Outputs class usage statistics for asset files.

`unbundle` - Extracts Unity webplayer bundles (*.unity3d).

`fixrefs` - Converts the relative paths for shared assets in compiled scene files to absolute paths. This allows the scenes to be opened in the Unity editor properly.

**Note:** If the file and its dependencies are moved to a different folder, disunity needs to be run again with this command.

`split` - Attempts to split an asset file to multiple smaller asset files, usually one for each object.

### Other parameters

Run disunity with the `-h` parameter for further usage.

Support
-------

### Tested engine versions

* 2.6
* 3.1
* 3.3
* 3.4
* 3.5
* 4.1
* 4.2

### Asset extraction

Type | Status
--- | --- 
AudioClip | Ok
Cubemap | Wrong texture flags
Font | Ok
MovieTexture | 2.6 only
Shader | Ok
SubstanceArchive | Ok
TextAsset | Ok
Texture2D | Missing support for PVR, ATC and some exotic color formats

Dependencies
------------

* [ioutils](https://github.com/ata4/ioutils)
* [apache-commons-cli-1.2](http://commons.apache.org/proper/commons-cli/)
* [apache-commons-io-2.4](http://commons.apache.org/proper/commons-io/)
* [apache-commons-lang3-3.1](http://commons.apache.org/proper/commons-lang/)