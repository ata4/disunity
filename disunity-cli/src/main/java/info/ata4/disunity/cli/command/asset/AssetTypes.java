/*
 ** 2015 December 20
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import info.ata4.disunity.cli.OutputFormatDelegate;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.junity.serialize.typetree.Type;
import info.ata4.junity.serialize.typetree.TypeTree;
import info.ata4.util.collection.Node;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List embedded runtime types."
)
public class AssetTypes extends AssetCommand {

    @ParametersDelegate
    private final OutputFormatDelegate outputFormat = new OutputFormatDelegate();

    @Override
    protected void runSerializedFile(Path file, SerializedFile serialized) {
        TypeTree<? extends Type> typeTree = serialized.metadata().typeTree();

        switch (outputFormat.get()) {
            case JSON:
                printJson(file, typeTree);
                break;

            default:
                printText(file, typeTree);
        }
    }

    private void printText(Path file, TypeTree<? extends Type> typeTree) {
        output().println(file);

        if (!typeTree.embedded()) {
            output().println("File doesn't contain type information");
            return;
        }

        typeTree.typeMap().forEach((path, typeRoot) -> {
            output().printf("pathID: %d, classID: %d%n", path, typeRoot.classID());
            printTypeNodeText(typeRoot.nodes(), 0);
            output().println();
        });
    }

    private void printTypeNodeText(Node<? extends Type> node, int level) {
        String indent = StringUtils.repeat("  ", level);
        Type type = node.data();
        output().printf("% 4d: %s%s %s (metaFlag: %x)%n", type.index(), indent,
                type.typeName(), type.fieldName(), type.metaFlag());
        node.forEach(t -> printTypeNodeText(t, level + 1));
    }

    private void printJson(Path file, TypeTree<? extends Type> typeTree) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonTypeTree = new JsonObject();
        jsonTypeTree.addProperty("file", file.toString());

        if (typeTree.embedded()) {
            JsonArray jsonTypes = new JsonArray();

            typeTree.typeMap().forEach((path, typeRoot) -> {
                JsonObject jsonTypeNode = new JsonObject();
                jsonTypeNode.addProperty("pathID", path);
                jsonTypeNode.addProperty("classID", typeRoot.classID());
                if (typeRoot.scriptID() != null) {
                    jsonTypeNode.addProperty("scriptID", typeRoot.scriptID().toString());
                }
                if (typeRoot.oldTypeHash() != null) {
                    jsonTypeNode.addProperty("oldTypeHash", typeRoot.oldTypeHash().toString());
                }

                jsonTypeNode.add("nodes", typeNodeToJson(typeRoot.nodes(), gson));

                jsonTypes.add(jsonTypeNode);
            });

            jsonTypeTree.add("types", jsonTypes);
        }

        gson.toJson(jsonTypeTree, output());
    }

    private JsonObject typeNodeToJson(Node<? extends Type> node, Gson gson) {
        JsonObject jsonNode = new JsonObject();

        jsonNode.add("data", gson.toJsonTree(node.data()));

        if (!node.isEmpty()) {
            JsonArray jsonChildren = new JsonArray();
            node.forEach(childNode -> {
                jsonChildren.add(typeNodeToJson(childNode, gson));
            });
            jsonNode.add("children", jsonChildren);
        }

        return jsonNode;
    }
}
