Building with maven
=================

If you haven't initialized the submodules yet, do so: `git submodule update --init --recursive`

To build with maven, run the following after updating the submodules:

```
./maven-setup-submodules.sh
mvn package
```

Afterwards, the packaged disunity .jar should be in `target/`, along with dependencies in `target/lib/`.
