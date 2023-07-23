/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package shadows.placebo.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class bridges the gap between the FML config GUI classes and the Forge Configuration classes.
 */
public class ConfigElement implements IConfigElement {
    private Property prop;
    private Property.Type type;
    private boolean isProperty;
    private ConfigCategory category;
    private boolean categoriesFirst = true;

    public ConfigElement(ConfigCategory category) {
        this.category = category;
        this.isProperty = false;
    }

    public ConfigElement(Property prop) {
        this.prop = prop;
        this.type = prop.getType();
        this.isProperty = true;
    }

    public ConfigElement listCategoriesFirst(boolean categoriesFirst) {
        this.categoriesFirst = categoriesFirst;
        return this;
    }

    @Override
    public List<IConfigElement> getChildElements() {
        if (!this.isProperty) {
            List<IConfigElement> elements = new ArrayList<>();
            Iterator<ConfigCategory> ccI = this.category.getChildren().iterator();
            Iterator<Property> pI = this.category.getOrderedValues().iterator();
            if (this.categoriesFirst) while (ccI.hasNext()) {
                ConfigElement temp = new ConfigElement(ccI.next());
                if (temp.showInGui()) // don't bother adding elements that shouldn't show
                    elements.add(temp);
            }

            while (pI.hasNext()) {
                ConfigElement temp = new ConfigElement(pI.next());
                if (temp.showInGui()) elements.add(temp);
            }

            if (!this.categoriesFirst) while (ccI.hasNext()) {
                ConfigElement temp = new ConfigElement(ccI.next());
                if (temp.showInGui()) elements.add(temp);
            }

            return elements;
        }
        return null;
    }

    @Override
    public String getName() {
        return this.isProperty ? this.prop.getName() : this.category.getName();
    }

    @Override
    public boolean isProperty() {
        return this.isProperty;
    }

    @Override
    public String getQualifiedName() {
        return this.isProperty ? this.prop.getName() : this.category.getQualifiedName();
    }

    @Override
    public ConfigGuiType getType() {
        return this.isProperty ? getType(this.prop) : ConfigGuiType.CONFIG_CATEGORY;
    }

    public static ConfigGuiType getType(Property prop) {
        return prop.getType() == Property.Type.BOOLEAN ? ConfigGuiType.BOOLEAN
            : prop.getType() == Property.Type.DOUBLE ? ConfigGuiType.DOUBLE
                : prop.getType() == Property.Type.INTEGER ? ConfigGuiType.INTEGER : prop.getType() == Property.Type.COLOR ? ConfigGuiType.COLOR : prop.getType() == Property.Type.MOD_ID ? ConfigGuiType.MOD_ID : ConfigGuiType.STRING;
    }

    @Override
    public boolean isList() {
        return this.isProperty && this.prop.isList();
    }

    @Override
    public boolean isListLengthFixed() {
        return this.isProperty && this.prop.isListLengthFixed();
    }

    @Override
    public int getMaxListLength() {
        return this.isProperty ? this.prop.getMaxListLength() : -1;
    }

    @Override
    public String getComment() {
        return this.isProperty ? this.prop.getComment() : this.category.getComment();
    }

    @Override
    public boolean isDefault() {
        return !this.isProperty || this.prop.isDefault();
    }

    @Override
    public void setToDefault() {
        if (this.isProperty) this.prop.setToDefault();
    }

    @Override
    public boolean requiresWorldRestart() {
        return this.isProperty ? this.prop.requiresWorldRestart() : this.category.requiresWorldRestart();
    }

    @Override
    public boolean showInGui() {
        return this.isProperty ? this.prop.showInGui() : this.category.showInGui();
    }

    @Override
    public boolean requiresMcRestart() {
        return this.isProperty ? this.prop.requiresMcRestart() : this.category.requiresMcRestart();
    }

    @Override
    public String[] getValidValues() {
        return this.isProperty ? this.prop.getValidValues() : null;
    }

    @Override
    public String[] getValidValuesDisplay() {
        return new String[0];
    }

    @Override
    public String getLanguageKey() {
        return this.isProperty ? this.prop.getLanguageKey() : this.category.getLanguagekey();
    }

    @Override
    public Object getDefault() {
        return this.isProperty ? this.prop.getDefault() : null;
    }

    @Override
    public Object[] getDefaults() {
        if (this.isProperty) {
            String[] aVal = this.prop.getDefaults();
            if (this.type == Property.Type.BOOLEAN) {
                Boolean[] ba = new Boolean[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ba[i] = Boolean.valueOf(aVal[i]);
                return ba;
            }
            else if (this.type == Property.Type.DOUBLE) {
                Double[] da = new Double[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    da[i] = Double.valueOf(aVal[i].toString());
                return da;
            }
            else if (this.type == Property.Type.INTEGER) {
                Integer[] ia = new Integer[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ia[i] = Integer.valueOf(aVal[i].toString());
                return ia;
            }
            else return aVal;
        }
        return null;
    }

    @Override
    public Pattern getValidationPattern() {
        return this.isProperty ? this.prop.getValidationPattern() : null;
    }

    @Override
    public boolean hasSlidingControl() {
        return false;
    }

    @Override
    public Object get() {
        return this.isProperty ? this.prop.getString() : null;
    }

    @Override
    public Object[] getList() {
        if (this.isProperty) {
            String[] aVal = this.prop.getStringList();
            if (this.type == Property.Type.BOOLEAN) {
                Boolean[] ba = new Boolean[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ba[i] = Boolean.valueOf(aVal[i]);
                return ba;
            }
            else if (this.type == Property.Type.DOUBLE) {
                Double[] da = new Double[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    da[i] = Double.valueOf(aVal[i].toString());
                return da;
            }
            else if (this.type == Property.Type.INTEGER) {
                Integer[] ia = new Integer[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ia[i] = Integer.valueOf(aVal[i].toString());
                return ia;
            }
            else return aVal;
        }
        return null;
    }

    @Override
    public void set(Object value) {
        if (this.isProperty) {
            if (this.type == Property.Type.BOOLEAN) this.prop.set(Boolean.parseBoolean(value.toString()));
            else if (this.type == Property.Type.DOUBLE) this.prop.set(Double.parseDouble(value.toString()));
            else if (this.type == Property.Type.INTEGER) this.prop.set(Integer.parseInt(value.toString()));
            else this.prop.set(value.toString());
        }
    }

    @Override
    public void set(Object[] aVal) {
        if (this.isProperty) {
            if (this.type == Property.Type.BOOLEAN) {
                boolean[] ba = new boolean[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ba[i] = Boolean.valueOf(aVal[i].toString());
                this.prop.set(ba);
            }
            else if (this.type == Property.Type.DOUBLE) {
                double[] da = new double[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    da[i] = Double.valueOf(aVal[i].toString());
                this.prop.set(da);
            }
            else if (this.type == Property.Type.INTEGER) {
                int[] ia = new int[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    ia[i] = Integer.valueOf(aVal[i].toString());
                this.prop.set(ia);
            }
            else {
                String[] is = new String[aVal.length];
                for (int i = 0; i < aVal.length; i++)
                    is[i] = aVal[i].toString();
                this.prop.set(is);
            }
        }
    }

    @Override
    public Object getMinValue() {
        return this.isProperty ? this.prop.getMinValue() : null;
    }

    @Override
    public Object getMaxValue() {
        return this.isProperty ? this.prop.getMaxValue() : null;
    }
}
