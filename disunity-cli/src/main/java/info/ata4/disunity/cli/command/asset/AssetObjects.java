/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.util.Formatters;
import info.ata4.disunity.cli.util.TableBuilder;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.disunity.cli.util.TextTableFormat;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.junity.serialize.SerializedFileMetadata;
import info.ata4.junity.serialize.objectinfo.ObjectInfo;
import info.ata4.junity.serialize.objectinfo.ObjectInfoV2;
import info.ata4.junity.serialize.objectinfo.ObjectInfoV3;
import info.ata4.junity.serialize.typetree.Type;
import info.ata4.junity.serialize.typetree.TypeRoot;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List serialized objects."
)
public class AssetObjects extends AssetTableCommand {

    @Override
    protected TableModel tableModel(SerializedFile serialized) {
        SerializedFileMetadata metadata = serialized.metadata();

        TableBuilder table = new TableBuilder();
        table.row("Path ID", "Offset", "Length", "Type ID", "Class ID");

        Class<ObjectInfo> factory = metadata.objectInfoTable().elementFactory();

        boolean typeTreePresent = metadata.typeTree().embedded();
        boolean v2 = ObjectInfoV2.class.isAssignableFrom(factory);
        boolean v3 = ObjectInfoV3.class.isAssignableFrom(factory);

        if (typeTreePresent) {
            table.append("Class Name");
        }

        if (v2) {
            table.append("Script Type ID");
        }

        if (v3) {
            table.append("Stripped");
        }

        metadata.objectInfoTable().infoMap().entrySet().stream().forEach(e -> {
            ObjectInfo info = e.getValue();
            table.row(e.getKey(), info.offset(), info.length(), info.typeID(),
                    info.classID());

            if (typeTreePresent) {
                TypeRoot<Type> baseClass = metadata.typeTree().typeMap().get(info.typeID());
                String className = baseClass.nodes().data().typeName();
                table.append(className);
            }

            if (v2) {
                table.append(((ObjectInfoV2) info).scriptTypeIndex());
            }

            if (v3) {
                table.append(((ObjectInfoV3) info).isStripped());
            }
        });

        TableModel model = new TableModel("Objects", table.get());
        TextTableFormat format = model.format();
        format.columnFormatter(1, Formatters::hex);

        return model;
    }
}
