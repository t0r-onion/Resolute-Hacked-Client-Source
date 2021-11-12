package disabler.opennbt.conversion.builtin;

import disabler.opennbt.conversion.TagConverter;
import disabler.opennbt.tag.builtin.StringTag;

/**
 * A converter that converts between StringTag and String.
 */
public class StringTagConverter implements TagConverter<StringTag, String> {
    @Override
    public String convert(StringTag tag) {
        return tag.getValue();
    }

    @Override
    public StringTag convert(String name, String value) {
        return new StringTag(name, value);
    }
}