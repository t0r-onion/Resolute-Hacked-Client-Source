package vip.Resolute.ui.click.skeet.component.impl.sub.comboBox;

import vip.Resolute.ui.click.skeet.component.impl.sub.text.TextComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.ui.click.skeet.framework.ExpandableComponent;
import vip.Resolute.ui.click.skeet.framework.PredicateComponent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ComboBoxTextComponent extends Component implements ExpandableComponent, PredicateComponent {
    private static final int COMBO_BOX_HEIGHT = 10;
    private static final int COMBO_BOX_Y_OFFSET = 1;
    private final TextComponent textComponent;
    private final ComboBoxComponent comboBoxComponent;

    public ComboBoxTextComponent(Component parent, String name, final Supplier<List<String>> getModes, final Consumer<String> setSelected, final List<String> getMode, final Supplier<Boolean> isVisible, float x, float y) {
        super(parent, x, y, 40.166668F, 16.0F);
        this.comboBoxComponent = new ComboBoxComponent(this, 0.0F, 6.0F, this.getWidth(), 10.0F) {
            @Override
            public boolean isVisible() {
                return isVisible.get();
            }

            public List<String> getMode() {
                return getMode;
            }

            public void setSelected(String mode) {
                setSelected.accept(mode);
            }

            public List<String> getModes() {
                return getModes.get();
            }
        };

        this.textComponent = new TextComponent(this, name, 1.0F, 0.0F);
        this.addChild(this.comboBoxComponent);
        this.addChild(this.textComponent);
    }

    public ComboBoxTextComponent(Component parent, String name, Supplier<List<String>> getModes, Consumer<String> setSelected, List<String> getMode, final Supplier<Boolean> isVisible) {
        this(parent, name, getModes, setSelected, getMode, isVisible, 0.0F, 0.0F);
    }

    @Override
    public boolean isVisible() {
        return this.comboBoxComponent.isVisible();
    }

    public float getExpandedX() {
        return this.comboBoxComponent.getExpandedX();
    }

    public float getExpandedY() {
        return this.getY() + this.textComponent.getHeight();
    }

    public float getExpandedWidth() {
        return this.comboBoxComponent.getExpandedWidth();
    }

    public float getExpandedHeight() {
        return this.comboBoxComponent.getExpandedHeight();
    }

    public void setExpanded(boolean expanded) {
        this.comboBoxComponent.setExpanded(expanded);
    }

    public boolean isExpanded() {
        return this.comboBoxComponent.isExpanded();
    }
}
