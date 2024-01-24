
package joserodpt.realpermissions.api.permission;

import joserodpt.realpermissions.api.utils.ReflectionHelper;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.ServerOperator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionBase extends PermissibleBase {
    private Tree starPermissions = null;

    public PermissionBase(ServerOperator opable) {
        super(opable);

        Map<String, PermissionAttachmentInfo> permissions = new LinkedHashMap<String, PermissionAttachmentInfo>() {
            @Override
            public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v) {
                PermissionAttachmentInfo existing = this.get(k);
                if (existing != null) {
                    return existing;
                }
                return super.put(k, v);
            }
        };

        ReflectionHelper.setField(PermissibleBase.class, this, permissions, "permissions");
    }
   
    @Override
    public boolean hasPermission(String permission) {
        if(super.hasPermission(permission)) return true;
        return starPermissions != null && starPermissions.canReachEnd(permission);
    }
   
    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }
   
    @Override
    public void recalculatePermissions() {
        super.recalculatePermissions();
       
        this.starPermissions = new Tree();
        for(String perm : super.getEffectivePermissions()
                .stream()
                .map(PermissionAttachmentInfo::getPermission)
                .collect(Collectors.toSet())) {
            if(perm.endsWith("*")) {
                starPermissions.addString(perm.substring(0, perm.length() - 1));
            }
        }
    }
   
    private class Tree {
        private Node root;

        public void addString(String text) {
            if(root == null) {
                root = new Node();
            }

            Node currentNode = root;

            for(int i = 0; i < text.length(); ++i) {
                char character = text.charAt(i);
                boolean created = false;
                if(currentNode.children == null) {
                    currentNode.children = new HashMap<>();
                    created = true;
                }

                if(!currentNode.children.containsKey(character)) {
                    if(!currentNode.isLeaf() || created) {
                        Node n = new Node();
                        currentNode.children.put(character, n);
                        currentNode = n;
                    } else {
                        return;
                    }
                } else {
                    currentNode = currentNode.children.get(character);
                }

                // if we're at the end, remove any remaining nodes as this is a more broad definition
                if(i+1 == text.length()) {
                    currentNode.children = new HashMap<>();
                }
            }

            if(text.isEmpty()) {
                root.children = new HashMap<>();
            }
        }

        public boolean canReachEnd(String text) {
            Node currentNode = root;
           
            if(currentNode == null) {
                return false;
            }
           
            for(int i = 0; i < text.length(); ++i) {
                char character = text.charAt(i);
                if(currentNode.isLeaf()) {
                    return true;
                } else if(!currentNode.children.containsKey(character)) {
                    return false;
                }
                currentNode = currentNode.children.get(character);
            }
           
            return currentNode.isLeaf();
        }
       
        private class Node {
            public HashMap<Character, Node> children;
            public boolean isLeaf() {
                return this.children == null || this.children.isEmpty();
            }
        }
    }
}