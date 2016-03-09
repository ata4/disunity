/*
 ** 2015 April 15
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import info.ata4.junity.UnityStruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class for objects that hold the runtime type information of an asset file.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity RTTIClassHierarchyDescriptor, RTTIBaseClassDescriptor2, TypeTree
 */
public abstract class TypeTree<T extends Type> extends UnityStruct<T> {

    protected Map<Integer, TypeRoot<T>> typeMap = new LinkedHashMap<>();
    protected boolean embedded;

    public TypeTree(Class<T> elementFactory) {
        super(elementFactory);
    }

    public Map<Integer, TypeRoot<T>> typeMap() {
        return typeMap;
    }

    public void typeMap(Map<Integer, TypeRoot<T>> typeMap) {
        this.typeMap = Objects.requireNonNull(typeMap);
    }

    public boolean embedded() {
        return embedded;
    }

    public void embedded(boolean embedded) {
        this.embedded = embedded;
    }
}
