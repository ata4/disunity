/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import info.ata4.junity.serialize.fileidentifier.FileIdentifier;
import info.ata4.junity.serialize.fileidentifier.FileIdentifierTable;
import info.ata4.junity.serialize.fileidentifier.FileIdentifierV1;
import info.ata4.junity.serialize.fileidentifier.FileIdentifierV2;
import info.ata4.junity.serialize.objectidentifier.ObjectIdentifierTable;
import info.ata4.junity.serialize.objectinfo.ObjectInfo;
import info.ata4.junity.serialize.objectinfo.ObjectInfoTable;
import info.ata4.junity.serialize.objectinfo.ObjectInfoTableV1;
import info.ata4.junity.serialize.objectinfo.ObjectInfoTableV2;
import info.ata4.junity.serialize.objectinfo.ObjectInfoV1;
import info.ata4.junity.serialize.objectinfo.ObjectInfoV2;
import info.ata4.junity.serialize.objectinfo.ObjectInfoV3;
import info.ata4.junity.serialize.typetree.Type;
import info.ata4.junity.serialize.typetree.TypeTree;
import info.ata4.junity.serialize.typetree.TypeTreeV1;
import info.ata4.junity.serialize.typetree.TypeTreeV2;
import info.ata4.junity.serialize.typetree.TypeTreeV3;
import info.ata4.junity.serialize.typetree.TypeV1;
import info.ata4.junity.serialize.typetree.TypeV2;
import info.ata4.log.LogUtils;
import info.ata4.util.io.DataBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SerializedFileMetadata implements Struct {

    private static final Logger L = LogUtils.getLogger();

    private final DataBlock typeTreeBlock = new DataBlock();
    private final DataBlock objectInfoBlock = new DataBlock();
    private final DataBlock objectIDBlock = new DataBlock();
    private final DataBlock externalsBlock = new DataBlock();

    private TypeTree typeTree;
    private ObjectInfoTable objectInfoTable;
    private ObjectIdentifierTable objectIDTable;
    private FileIdentifierTable externals;

    private int version;

    public DataBlock typeTreeBlock() {
        return typeTreeBlock;
    }

    public DataBlock objectInfoBlock() {
        return objectInfoBlock;
    }

    public DataBlock objectIDBlock() {
        return objectIDBlock;
    }

    public DataBlock externalsBlock() {
        return externalsBlock;
    }

    public List<DataBlock> dataBlocks() {
        List<DataBlock> blocks = new ArrayList<>();
        blocks.add(typeTreeBlock());
        blocks.add(objectInfoBlock());
        blocks.add(objectIDBlock());
        blocks.add(externalsBlock());
        return blocks;
    }

    public int version() {
        return version;
    }

    public void version(int version) {
        this.version = version;
    }

    public <T extends Type> TypeTree<T> typeTree() {
        return typeTree;
    }

    public <T extends Type> void typeTree(TypeTree<T> typeTree) {
        this.typeTree = Objects.requireNonNull(typeTree);
    }

    public <T extends ObjectInfo> ObjectInfoTable<T> objectInfoTable() {
        return objectInfoTable;
    }

    public <T extends ObjectInfo> void objectInfoTable(ObjectInfoTable<T> objInfoTable) {
        this.objectInfoTable = Objects.requireNonNull(objInfoTable);
    }

    public ObjectIdentifierTable objectIDTable() {
        return objectIDTable;
    }

    public void objectIDTable(ObjectIdentifierTable objectIDTable) {
        this.objectIDTable = Objects.requireNonNull(objectIDTable);
    }

    public <T extends FileIdentifier> FileIdentifierTable<T> externals() {
        return externals;
    }

    public <T extends FileIdentifier> void externals(FileIdentifierTable<T> externals) {
        this.externals = Objects.requireNonNull(externals);
    }

    @Override
    public void read(DataReader in) throws IOException {
        // load type tree
        if (version > 13) {
            typeTree = new TypeTreeV3(TypeV2.class);
        } else if (version > 6) {
            typeTree = new TypeTreeV2(TypeV1.class);
        } else {
            typeTree = new TypeTreeV1(TypeV1.class);
        }

        typeTreeBlock.markBegin(in);
        in.readStruct(typeTree);
        typeTreeBlock.markEnd(in);
        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);

        // load object info table
        if (version > 14) {
            objectInfoTable = new ObjectInfoTableV2(ObjectInfoV3.class);
        } else if (version > 13) {
            objectInfoTable = new ObjectInfoTableV2(ObjectInfoV2.class);
        } else {
            objectInfoTable = new ObjectInfoTableV1(ObjectInfoV1.class);
        }

        objectInfoBlock.markBegin(in);
        in.readStruct(objectInfoTable);
        objectInfoBlock.markEnd(in);
        L.log(Level.FINER, "objectInfoBlock: {0}", objectInfoBlock);

        // load object identifiers (Unity 5+ only)
        objectIDTable = new ObjectIdentifierTable();
        if (version > 10) {
            objectIDBlock.markBegin(in);
            in.readStruct(objectIDTable);
            objectIDBlock.markEnd(in);
            L.log(Level.FINER, "objectIDBlock: {0}", objectIDBlock);
        }

        // load external references
        if (version > 5) {
            externals = new FileIdentifierTable(FileIdentifierV2.class);
        } else {
            externals = new FileIdentifierTable(FileIdentifierV1.class);
        }

        externalsBlock.markBegin(in);
        in.readStruct(externals);
        externalsBlock.markEnd(in);
        L.log(Level.FINER, "externalsBlock: {0}", externalsBlock);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        typeTreeBlock.markBegin(out);
        out.writeStruct(typeTree);
        typeTreeBlock.markEnd(out);
        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);

        objectInfoBlock.markBegin(out);
        out.writeStruct(objectInfoTable);
        objectInfoBlock.markEnd(out);
        L.log(Level.FINER, "objectInfoBlock: {0}", objectInfoBlock);

        if (version > 10) {
            objectIDBlock.markBegin(out);
            out.writeStruct(objectIDTable);
            objectIDBlock.markEnd(out);
            L.log(Level.FINER, "objectIDBlock: {0}", objectIDBlock);
        }

        externalsBlock.markBegin(out);
        out.writeStruct(externals);
        externalsBlock.markEnd(out);
        L.log(Level.FINER, "externalsBlock: {0}", externalsBlock);
    }
}
