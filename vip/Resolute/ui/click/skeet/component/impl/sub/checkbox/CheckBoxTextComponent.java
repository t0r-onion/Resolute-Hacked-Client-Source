package vip.Resolute.ui.click.skeet.component.impl.sub.checkbox;

import vip.Resolute.ui.click.skeet.component.impl.sub.text.TextComponent;
import vip.Resolute.ui.click.skeet.framework.Component;
import vip.Resolute.ui.click.skeet.framework.PredicateComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;
public final class CheckBoxTextComponent extends Component implements PredicateComponent {
    private final CheckBoxComponent checkBox;
    private final TextComponent textComponent;
    private static final int CHECK_BOX_SIZE = 5;
    private static final int TEXT_OFFSET = 3;

    public CheckBoxTextComponent(Component parent, String text, final Supplier<Boolean> isChecked, final Consumer<Boolean> onChecked, final Supplier<Boolean> isVisible, float x, float y) {
        super(parent, x, y, 0.0F, 5.0F);
        this.checkBox = new CheckBoxComponent(this, 0.0F, 0.0F, 5.0F, 5.0F) {
            public boolean isChecked() {
                return (Boolean)isChecked.get();
            }

            public void setChecked(boolean checked) {
                onChecked.accept(checked);
            }

            @Override
            public boolean isVisible() {
                return (Boolean)isVisible.get();
            }
        };
        this.textComponent = new TextComponent(this, text, 8.0F, 1.0F);
        this.addChild(this.checkBox);
        this.addChild(this.textComponent);
    }

    public CheckBoxTextComponent(Component parent, String text, Supplier<Boolean> isChecked, Consumer<Boolean> onChecked, final Supplier<Boolean> isVisible) {
        this(parent, text, isChecked, onChecked, isVisible, 0.0F, 0.0F);
    }

    public CheckBoxTextComponent(Component parent, String text, Supplier<Boolean> isChecked, Consumer<Boolean> onChecked) {
        this(parent, text, isChecked, onChecked, () -> true);
    }


    public float getWidth() {
        return 8.0F + this.textComponent.getWidth();
    }

    @Override
    public boolean isVisible() {
        return this.checkBox.isVisible();
    }
}
