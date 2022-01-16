package club.thom.tem.constants;

import org.apache.commons.lang3.StringUtils;

public class ArmourColour {
    String helmet, chestplate, leggings, boots;
    boolean full = false;

    public ArmourColour(String helmet, String chestplate, String leggings, String boots) {
        this.helmet = formatHex(helmet);
        this.chestplate = formatHex(chestplate);
        this.leggings = formatHex(leggings);
        this.boots = formatHex(boots);
    }

    public ArmourColour(String full) {
        this(full, full, full, full);
        this.full = true;
    }

    public ArmourColour(String helmet, String boots) {
        this(helmet, "", "", "boots");
    }

    public ArmourColour(String chestplate, String leggings, String boots) {
        this("", chestplate, leggings, boots);
    }

    public String getFull() {
        if (full) {
            return this.helmet;
        }
        return null;
    }

    public String getHelmet() {
        if (!helmet.equals("")) {
            return this.helmet;
        }
        return null;
    }

    public String getChestplate() {
        if (!chestplate.equals("")) {
            return this.chestplate;
        }
        return null;
    }

    public String getLeggings() {
        if (!leggings.equals("")) {
            return this.leggings;
        }
        return null;
    }

    public String getBoots() {
        if (!boots.equals("")) {
            return this.boots;
        }
        return null;
    }

    private String formatHex(String hex) {
        if (hex.length() == 1) {
            return StringUtils.repeat(hex, 6);
        } else if (hex.length() == 3) {
            StringBuilder rrggbb = new StringBuilder();
            for (int i = 1; i < hex.length(); i++) {
                rrggbb.append(hex.charAt(i)).append(hex.charAt(i));
            }
            return String.valueOf(rrggbb);
        }
        return hex;
    }
}