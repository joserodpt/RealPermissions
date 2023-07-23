package pt.josegamerpt.realpermissions.permission;

public class Permission {
    private String permissionString, associatedRank;

    public Permission(String permission, String associatedRank) {
        this.permissionString = permission;
        this.associatedRank = associatedRank;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }
}
