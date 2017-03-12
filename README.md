DisUnity
========

An experimental command-line toolset for Unity asset and asset bundle files written in Java, mostly designed for extraction.

### Download

The latest build can be found on the [releases page](https://github.com/ata4/disunity/releases).

### A note about the versions

0.3 works best with Unity 3.x and has most of the original extraction features.

0.4 is a somewhat incomplete and untested upgrade to support Unity 4 and 5 and has some of the extraction features of 0.3.

0.5 is a code rewrite to properly support all Unity games from 2 to 5 that also comes with unit tests. Right now, it only
supports raw file reading and writing without any object deserialization, therefore it also can't extract any asset data directly.

### Usage

    disunity <command> [options] <file>
    
**Note:** depending on the platform, you may need to run disunity.bat (Windows) or disunity.sh (Linux/MacOS). In case the launch script fails, try `java -jar disunity.jar`.

### Available commands (v0.5)

## Asset commands

| Command           | Purpose
| :---------------- | :----------------
| asset blocks      | List data block offsets and sizes. Could be useful for manual extraction.
| asset externals   | List asset file dependencies.
| asset header      | Display some information from the file header.
| asset objectids   | List object identifiers (Unity 5 and higher only).
| asset objects     | List object data entries.
| asset types       | Display embedded runtime type information.
| asset unpack      | Unpacks raw data blocks from a file. Could be useful for manual extraction.

## Asset bundle commands

| Command           | Purpose
| :---------------- | :----------------
| bundle list       | List bundled files.
| bundle info       | Display some information from the file header.
| bundle pack       | Pack files into a bundle. Requires a bundle property file.
| bundle unpack     | Unpack files from a bundle.
